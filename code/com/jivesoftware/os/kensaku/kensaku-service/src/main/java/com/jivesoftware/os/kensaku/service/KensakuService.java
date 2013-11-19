/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jivesoftware.os.kensaku.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.amza.service.AmzaService;
import com.jivesoftware.os.amza.service.AmzaTable;
import com.jivesoftware.os.amza.shared.RingHost;
import com.jivesoftware.os.amza.shared.TableName;
import com.jivesoftware.os.jive.utils.http.client.HttpClient;
import com.jivesoftware.os.jive.utils.http.client.HttpClientConfig;
import com.jivesoftware.os.jive.utils.http.client.HttpClientConfiguration;
import com.jivesoftware.os.jive.utils.http.client.HttpClientFactory;
import com.jivesoftware.os.jive.utils.http.client.HttpClientFactoryProvider;
import com.jivesoftware.os.jive.utils.http.client.rest.RequestHelper;
import com.jivesoftware.os.kensaku.service.plugins.KensakuIndex;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResult;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class KensakuService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final KensakuIndexProvider kensakuIndexerProvider;
    private final AmzaService amzaService;
    private final int numberOfPartitions = 10;
    private final int numberOfReplicas = 3;
    private final Executor executor;
    private final Map<RingHost, KensakuClient> clients = new ConcurrentHashMap<>();
    private final Random rand = new Random();

    public KensakuService(KensakuIndexProvider kensakuIndexerProvider, AmzaService amzaService, Executor executor) {
        this.kensakuIndexerProvider = kensakuIndexerProvider;
        this.amzaService = amzaService;
        this.executor = executor;
    }

    KensakuClient getClient(RingHost ringHost) {
        KensakuClient client = clients.get(ringHost);
        if (client != null) {
            return client;
        }

        HttpClientConfig httpClientConfig = HttpClientConfig.newBuilder().build();
        HttpClientFactory httpClientFactory = new HttpClientFactoryProvider()
                .createHttpClientFactory(Arrays.<HttpClientConfiguration>asList(httpClientConfig));
        HttpClient httpClient = httpClientFactory.createClient(ringHost.getHost(), ringHost.getPort());
        RequestHelper requestHelper = new RequestHelper(httpClient, mapper);
        KensakuClient kensakuClient = new KensakuClient(requestHelper);
        clients.put(ringHost, kensakuClient);
        return kensakuClient;
    }

    public String getRingPartition(String tenantId, long documentOrPartitionId) {
        return "index-" + tenantId + "/" + (documentOrPartitionId % numberOfPartitions);
    }

    public void add(List<KensakuDocument> documents) throws Exception {

        for (KensakuDocument document : documents) {
            String ringPartitionName = getRingPartition(document.tenantId, document.documentId);
            if (amzaService.getRing(ringPartitionName).isEmpty()) {
                amzaService.buildRandomSubRing(ringPartitionName, numberOfReplicas);
            }

            TableName<String, String> tableName = new TableName<>(ringPartitionName, ringPartitionName, String.class, null, null, String.class);
            AmzaTable<String, String> amzaTable = amzaService.getTable(tableName);
            amzaTable.set(Long.toString(document.documentId), mapper.writeValueAsString(document));
        }

    }

    public void remove(List<KensakuDocument> documents) throws Exception {
        for (KensakuDocument document : documents) {
            String ringPartitionName = getRingPartition(document.tenantId, document.documentId);
            TableName<String, String> partitionName = new TableName<>(ringPartitionName, ringPartitionName, String.class, null, null, String.class);
            AmzaTable<String, String> partition = amzaService.getTable(partitionName);
            partition.remove(Long.toString(document.documentId));
        }
    }

    public KensakuResults searchLocally(int partition, KensakuQuery kensakuQuery) throws Exception {
        String ringPartitionName = getRingPartition(kensakuQuery.tenantId, partition);
        TableName<String, String> tableName = new TableName<>(ringPartitionName, ringPartitionName, String.class, null, null, String.class);
        KensakuIndex kensakuIndex = kensakuIndexerProvider.getKensakuIndex(tableName);
        if (kensakuIndex == null) {
            return null;
        }
        KensakuResults kensakuResults = kensakuIndex.search(kensakuQuery);
        // Could fetch document from amza?
//        AmzaTable<String, String> table = amzaService.getTable(tableName);
//        for (KensakuResult kensakuResult : kensakuResults.results) {
//            String fullDocument = table.get(kensakuResult.fields.get("docId"));
//            kensakuResult.fields.put("body", fullDocument);
//        }
        return kensakuResults;
    }

    public KensakuResults search(KensakuQuery kensakuQuery) throws Exception {
        int askUpToNReplicas = 2;

        String tenant = kensakuQuery.tenantId;
        List<Callable<KensakuResults>> partitionScatter = new ArrayList<>();
        for (int p = 0; p < numberOfPartitions; p++) {
            String ringPartitionName = getRingPartition(tenant, p);
            List<RingHost> ring = amzaService.getRing(ringPartitionName);
            Collections.shuffle(ring, rand);

            List<Callable<KensakuResults>> replicaScatters = new ArrayList<>();
            for (int r = 0; r < askUpToNReplicas && r < ring.size(); r++) {
                RingHost ringHost = ring.get(r);
                if (ringHost.equals(amzaService.ringHost())) {
                    replicaScatters.add(new LocalReplica(this, p, kensakuQuery));
                } else {
                    replicaScatters.add(new ReplicaScatter(getClient(ringHost), p, kensakuQuery));
                }
            }
            partitionScatter.add(new PartitionScatter(executor, replicaScatters));
        }

        KensakuMergeResults mergeResults = new KensakuMergeResults(kensakuQuery);
        solveAll(executor, partitionScatter, mergeResults);
        return mergeResults.getResults();
    }

    void solveAll(Executor executor, Collection<Callable<KensakuResults>> partitionScatters, KensakuMergeResults kensakuMergeResults)
            throws InterruptedException, ExecutionException {
        CompletionService<KensakuResults> completionService = new ExecutorCompletionService<>(executor);
        for (Callable<KensakuResults> s : partitionScatters) {
            completionService.submit(s);
        }
        int n = partitionScatters.size();
        for (int i = 0; i < n; ++i) {
            KensakuResults r = completionService.take().get();
            if (r != null) {
                kensakuMergeResults.merge(r);
            }
        }
    }

    static class PartitionScatter implements Callable<KensakuResults> {

        private final Executor executor;
        private final Collection<Callable<KensakuResults>> replicaScatters;

        public PartitionScatter(Executor executor, Collection<Callable<KensakuResults>> replicaScatters) {
            this.executor = executor;
            this.replicaScatters = replicaScatters;
        }

        @Override
        public KensakuResults call() throws Exception {
            return solveFastest(replicaScatters);
        }

        KensakuResults solveFastest(Collection<Callable<KensakuResults>> solvers)
                throws InterruptedException {
            CompletionService<KensakuResults> completionService = new ExecutorCompletionService<>(executor);
            int n = solvers.size();
            List<Future<KensakuResults>> futures = new ArrayList<>(n);
            KensakuResults result = null;
            try {
                for (Callable<KensakuResults> s : solvers) {
                    futures.add(completionService.submit(s));
                }
                for (int i = 0; i < n; ++i) {
                    try {
                        KensakuResults r = completionService.take().get();
                        if (r != null) {
                            result = r;
                            break;
                        }
                    } catch (ExecutionException ignore) {
                    }
                }
            } finally {
                for (Future<KensakuResults> f : futures) {
                    f.cancel(true);
                }
            }

            return result;
        }
    }

    static class LocalReplica implements Callable<KensakuResults> {

        private final KensakuService kensakuService;
        private final int partition;
        private final KensakuQuery kensakuQuery;

        public LocalReplica(KensakuService kensakuService, int partition, KensakuQuery kensakuQuery) {
            this.kensakuService = kensakuService;
            this.partition = partition;
            this.kensakuQuery = kensakuQuery;
        }

        @Override
        public KensakuResults call() throws Exception {
            return kensakuService.searchLocally(partition, kensakuQuery);
        }

    }

    static class ReplicaScatter implements Callable<KensakuResults> {

        private final KensakuClient kensakuClient;
        private final int partitionId;
        private final KensakuQuery kensakuQuery;

        public ReplicaScatter(KensakuClient kensakuClient, int partitionId, KensakuQuery kensakuQuery) {
            this.kensakuClient = kensakuClient;
            this.partitionId = partitionId;
            this.kensakuQuery = kensakuQuery;
        }

        @Override
        public KensakuResults call() throws Exception {
            return kensakuClient.searchLocalPartition(partitionId, kensakuQuery);
        }

    }
}