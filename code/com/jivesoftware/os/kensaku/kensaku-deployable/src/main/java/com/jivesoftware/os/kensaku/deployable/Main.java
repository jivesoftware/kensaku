package com.jivesoftware.os.kensaku.deployable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jivesoftware.os.amza.service.AmzaService;
import com.jivesoftware.os.amza.service.AmzaServiceInitializer;
import com.jivesoftware.os.amza.service.AmzaServiceInitializer.AmzaServiceConfig;
import com.jivesoftware.os.amza.service.discovery.AmzaDiscovery;
import com.jivesoftware.os.amza.shared.AmzaInstance;
import com.jivesoftware.os.amza.shared.RingHost;
import com.jivesoftware.os.amza.shared.TableName;
import com.jivesoftware.os.amza.shared.TableStorage;
import com.jivesoftware.os.amza.shared.TableStorageProvider;
import com.jivesoftware.os.amza.storage.FileBackedTableStorage;
import com.jivesoftware.os.amza.storage.RowTableFile;
import com.jivesoftware.os.amza.storage.binary.BinaryRowChunkMarshaller;
import com.jivesoftware.os.amza.storage.binary.BinaryRowMarshaller;
import com.jivesoftware.os.amza.storage.binary.BinaryRowReader;
import com.jivesoftware.os.amza.storage.binary.BinaryRowWriter;
import com.jivesoftware.os.amza.storage.chunks.Filer;
import com.jivesoftware.os.amza.transport.http.replication.HttpChangeSetSender;
import com.jivesoftware.os.amza.transport.http.replication.HttpChangeSetTaker;
import com.jivesoftware.os.amza.transport.http.replication.endpoints.AmzaReplicationRestEndpoints;
import com.jivesoftware.os.jive.utils.base.service.ServiceHandle;
import com.jivesoftware.os.jive.utils.ordered.id.OrderIdProvider;
import com.jivesoftware.os.jive.utils.ordered.id.OrderIdProviderImpl;
import com.jivesoftware.os.kensaku.service.KensakuIndexProvider;
import com.jivesoftware.os.kensaku.service.KensakuRestEndpoints;
import com.jivesoftware.os.kensaku.service.KensakuService;
import com.jivesoftware.os.kensaku.service.KensakuTableStateChanges;
import com.jivesoftware.os.kensaku.service.poc.POCAllFieldsBooleanQueryBuilder;
import com.jivesoftware.os.kensaku.service.poc.POCAllFieldsDocumentBuilder;
import com.jivesoftware.os.kensaku.service.poc.POCDocIdBuilder;
import com.jivesoftware.os.kensaku.service.poc.POCResultBuilderProvider;
import com.jivesoftware.os.server.http.jetty.jersey.server.InitializeRestfulServer;
import com.jivesoftware.os.server.http.jetty.jersey.server.JerseyEndpoints;
import java.io.File;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    public void run(String[] args) throws Exception {
        String hostname = args[0];
        int port = Integer.parseInt(System.getProperty("amza.port", "1175"));
        String multicastGroup = System.getProperty("amza.discovery.group", "225.4.5.6");
        int multicastPort = Integer.parseInt(System.getProperty("amza.discovery.port", "1123"));
        String clusterName = (args.length > 1 ? args[1] : null);

        RingHost ringHost = new RingHost(hostname, port);
        final OrderIdProvider orderIdProvider = new OrderIdProviderImpl(new Random().nextInt(512)); // todo need a better way to create writter id.

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        final AmzaServiceConfig amzaServiceConfig = new AmzaServiceConfig();

        TableStorageProvider tableStorageProvider = new TableStorageProvider() {
            @Override
            public <K, V> TableStorage<K, V> createTableStorage(File workingDirectory, String tableDomain, TableName<K, V> tableName) throws Exception {
                File directory = new File(workingDirectory, tableDomain);
                File file = new File(directory, tableName.getTableName() + ".kvt");
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }

                Filer filer = Filer.open(file, "rw");
                BinaryRowReader reader = new BinaryRowReader(filer);
                BinaryRowWriter writer = new BinaryRowWriter(filer);
                //BinaryRowChunkMarshaller rowMarshaller = new BinaryRowChunkMarshaller(directory, tableName);
                BinaryRowMarshaller rowMarshaller = new BinaryRowMarshaller(tableName);
                RowTableFile<K, V, byte[]> rowTableFile = new RowTableFile<>(orderIdProvider, rowMarshaller, reader, writer);


                /*
                 StringRowReader reader = new StringRowReader(file);
                 StringRowWriter writer = new StringRowWriter(file);

                 //RowMarshaller<K, V, String> rowMarshaller = new StringRowMarshaller<>(mapper, tableName);
                 RowMarshaller<K, V, String> rowMarshaller = new StringRowValueChunkMarshaller(directory, mapper, tableName);
                 RowTableFile<K, V, String> rowTableFile = new RowTableFile<>(orderIdProvider, rowMarshaller, reader, writer);
                 */
                return new FileBackedTableStorage(rowTableFile);
            }
        };

        KensakuIndexProvider kensakuLuceneProvider = new KensakuIndexProvider(new File("./index"),
                new POCDocIdBuilder(),
                new POCAllFieldsDocumentBuilder(),
                new POCAllFieldsBooleanQueryBuilder(),
                new POCResultBuilderProvider());
        KensakuTableStateChanges kensakuPartitionStateChanges = new KensakuTableStateChanges(kensakuLuceneProvider);

        AmzaService amzaService = new AmzaServiceInitializer().initialize(amzaServiceConfig,
                orderIdProvider,
                tableStorageProvider,
                tableStorageProvider,
                tableStorageProvider,
                new HttpChangeSetSender(),
                new HttpChangeSetTaker(),
                kensakuPartitionStateChanges);

        amzaService.start(ringHost, amzaServiceConfig.resendReplicasIntervalInMillis,
                amzaServiceConfig.applyReplicasIntervalInMillis,
                amzaServiceConfig.takeFromNeighborsIntervalInMillis,
                amzaServiceConfig.compactTombstoneIfOlderThanNMillis);

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|      Amza Service Online");
        System.out.println("-----------------------------------------------------------------------");


        Executor executor = Executors.newFixedThreadPool(128);

        KensakuService kensakuService = new KensakuService(kensakuLuceneProvider, amzaService, executor);

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|      Kensaku Service Online");
        System.out.println("-----------------------------------------------------------------------");

        JerseyEndpoints jerseyEndpoints = new JerseyEndpoints()
                .addEndpoint(AmzaReplicationRestEndpoints.class)
                .addInjectable(AmzaInstance.class, amzaService)
                .addEndpoint(KensakuRestEndpoints.class)
                .addInjectable(kensakuService);


        InitializeRestfulServer initializeRestfulServer = new InitializeRestfulServer(port, "KensakuNode", 128, 10000);
        initializeRestfulServer.addContextHandler("/", jerseyEndpoints);
        ServiceHandle serviceHandle = initializeRestfulServer.build();
        serviceHandle.start();

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("|      Jetty Service Online");
        System.out.println("-----------------------------------------------------------------------");

        if (clusterName != null) {
            AmzaDiscovery amzaDiscovery = new AmzaDiscovery(amzaService, ringHost, clusterName, multicastGroup, multicastPort);
            amzaDiscovery.start();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|      Amza Service Discovery Online");
            System.out.println("-----------------------------------------------------------------------");
        } else {
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("|     Amze Service is in manual Discovery mode.  No cluster name was specified");
            System.out.println("-----------------------------------------------------------------------");
        }
    }
}