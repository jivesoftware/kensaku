package com.jivesoftware.os.kensaku.service;

import com.jivesoftware.os.amza.shared.TableName;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.kensaku.service.plugins.KensakuDocIdBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuDocumentBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuIndex;
import com.jivesoftware.os.kensaku.service.plugins.KensakuQueryBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuResultBuilderProvider;
import com.jivesoftware.os.kensaku.service.poc.POCLuceneIndex;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class KensakuIndexProvider {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final File workingDirectory;
    private final KensakuDocIdBuilder kensakuDocIdBuilder;
    private final KensakuDocumentBuilder kensakuDocumentBuilder;
    private final KensakuQueryBuilder kensakuQueryBuilder;
    private final KensakuResultBuilderProvider kensakuResultBuilderProvider;
    private final ConcurrentSkipListMap<TableName<?, ?>, KensakuIndex> indexs = new ConcurrentSkipListMap<>();

    public KensakuIndexProvider(File workingDirectory,
        KensakuDocIdBuilder kensakuDocIdBuilder,
        KensakuDocumentBuilder kensakuDocumentBuilder,
        KensakuQueryBuilder kensakuQueryBuilder,
        KensakuResultBuilderProvider kensakuResultBuilderProvider) {
        this.workingDirectory = workingDirectory;
        this.kensakuDocIdBuilder = kensakuDocIdBuilder;
        this.kensakuDocumentBuilder = kensakuDocumentBuilder;
        this.kensakuQueryBuilder = kensakuQueryBuilder;
        this.kensakuResultBuilderProvider = kensakuResultBuilderProvider;
    }

    public KensakuIndex getKensakuIndex(TableName<?, ?> partitionName) throws IOException {
        synchronized (indexs) {
            KensakuIndex indexer = indexs.get(partitionName);
            if (indexer != null) {
                LOG.info("Reusing open index for partitionName:" + partitionName);
                return indexer;
            }
            LOG.info("Opening index for partitionName:" + partitionName);
            File indexDirectory = new File(workingDirectory, partitionName.getTableName());
            indexDirectory.mkdirs();
            Directory dir = FSDirectory.open(indexDirectory);
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
            indexer = new POCLuceneIndex(dir, analyzer,
                kensakuDocIdBuilder, kensakuDocumentBuilder, kensakuQueryBuilder, kensakuResultBuilderProvider);
            indexs.put(partitionName, indexer);
            return indexer;
        }
    }
}
