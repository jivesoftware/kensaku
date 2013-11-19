package com.jivesoftware.os.kensaku.service.poc;

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.kensaku.service.plugins.KensakuDocIdBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuDocumentBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuIndex;
import com.jivesoftware.os.kensaku.service.plugins.KensakuQueryBuilder;
import com.jivesoftware.os.kensaku.service.plugins.KensakuResultBuilderProvider;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

public class POCLuceneIndex implements KensakuIndex<Term> {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final IndexWriter _indexWriter;
    private final TrackingIndexWriter _trackingIndexWriter;
    private final ReferenceManager<IndexSearcher> _indexSearcherReferenceManager;
    private final ControlledRealTimeReopenThread<IndexSearcher> _indexSearcherReopenThread;
    private final KensakuDocIdBuilder<Term> kensakuDocIdBuilder;
    private final KensakuDocumentBuilder<Document> kensakuDocumentBuilder;
    private final KensakuQueryBuilder<Query> kensakuQueryBuilder;
    private final KensakuResultBuilderProvider kensakuResultBuilderProvider;

    private long _reopenToken;      // index update/delete methods returned token

    public POCLuceneIndex(final Directory luceneDirectory,
            final Analyzer analyzer,
            KensakuDocIdBuilder<Term> kensakuDocIdBuilder,
            KensakuDocumentBuilder<Document> kensakuDocumentBuilder,
            KensakuQueryBuilder<Query> kensakuQueryBuilder,
            KensakuResultBuilderProvider kensakuResultBuilderProvider) {

        LOG.info("Opening index for "+luceneDirectory);

        try {
            this.kensakuDocIdBuilder = kensakuDocIdBuilder;
            this.kensakuDocumentBuilder = kensakuDocumentBuilder;
            this.kensakuQueryBuilder = kensakuQueryBuilder;
            this.kensakuResultBuilderProvider = kensakuResultBuilderProvider;

            // [1]: Create the indexWriter
            _indexWriter = new IndexWriter(luceneDirectory,
                    new IndexWriterConfig(Version.LUCENE_45,
                            analyzer));

            // [2a]: Create the TrackingIndexWriter to track changes to the delegated previously created IndexWriter
            _trackingIndexWriter = new TrackingIndexWriter(_indexWriter);

            // [2b]: Create an IndexSearcher ReferenceManager to safelly share IndexSearcher instances across
            //       multiple threads
            _indexSearcherReferenceManager = new SearcherManager(_indexWriter,
                    true,
                    null);

            // [3]: Create the ControlledRealTimeReopenThread that reopens the index periodically having into
            //      account the changes made to the index and tracked by the TrackingIndexWriter instance
            //      The index is refreshed every 60sc when nobody is waiting
            //      and every 100 millis whenever is someone waiting (see search method)
            //      (see http://lucene.apache.org/core/4_3_0/core/org/apache/lucene/search/NRTManagerReopenThread.html)
            _indexSearcherReopenThread = new ControlledRealTimeReopenThread<>(_trackingIndexWriter,
                    _indexSearcherReferenceManager,
                    60.00, // when there is nobody waiting
                    0.1);    // when there is someone waiting
            _indexSearcherReopenThread.start(); // start the refresher thread
        } catch (IOException ioEx) {
            throw new IllegalStateException("Lucene index could not be created: " + ioEx.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    synchronized public void close() {
        try {
            // stop the index reader re-open thread
            _indexSearcherReopenThread.interrupt();
            _indexSearcherReopenThread.close();

            // Close the indexWriter, commiting everithing that's pending
            _indexWriter.commit();
            _indexWriter.close();

        } catch (IOException ioEx) {
            LOG.error("Error while closing lucene index: {}", ioEx.getMessage(),
                    ioEx);
        }
    }

    @Override
   synchronized public void index(KensakuDocument kensakuDocument) throws Exception {
        try {
            _reopenToken = _trackingIndexWriter.addDocument(kensakuDocumentBuilder.build(kensakuDocument));
            LOG.debug("document indexed in lucene");
        } catch (Exception ioEx) {
            LOG.error("Error while in Lucene index operation: {}", ioEx.getMessage(), ioEx);
            throw ioEx;

        } finally {
            try {
                _indexWriter.commit();
            } catch (IOException ioEx) {
                LOG.error("Error while commiting changes to Lucene index: {}", ioEx.getMessage(), ioEx);
            }
        }
    }

    @Override
    synchronized public void reIndex(KensakuDocument kensakuDocument) throws Exception {
        try {
            Term recordIdTerm = kensakuDocIdBuilder.build(kensakuDocument);
            Document doc = kensakuDocumentBuilder.build(kensakuDocument);
            _reopenToken = _trackingIndexWriter.updateDocument(recordIdTerm,
                    doc);
            LOG.debug("{} document re-indexed in lucene", recordIdTerm.text());
        } catch (Exception ioEx) {
            LOG.error("Error in lucene re-indexing operation: {}", ioEx.getMessage(), ioEx);
            throw ioEx;
        } finally {
            try {
                _indexWriter.commit();
            } catch (IOException ioEx) {
                LOG.error("Error while commiting changes to Lucene index: {}", ioEx.getMessage(), ioEx);
            }
        }
    }

    /**
     * Unindex a lucene document
     *
     * @param idTerm term used to locate the document to be unindexed IMPORTANT! the term must filter only the document and only the document otherwise all
     * matching docs will be unindexed
     */
    @Override
    synchronized public void unIndex(final Term idTerm) throws Exception {
        try {
            _reopenToken = _trackingIndexWriter.deleteDocuments(idTerm);
            LOG.debug("{}={} term matching records un-indexed from lucene", idTerm.field(), idTerm.text());
        } catch (IOException ioEx) {
            LOG.error("Error in un-index lucene operation: {}", ioEx.getMessage(),
                    ioEx);
            throw ioEx;
        } finally {
            try {
                _indexWriter.commit();
            } catch (IOException ioEx) {
                LOG.error("Error while commiting changes to Lucene index: {}", ioEx.getMessage(),
                        ioEx);
            }
        }
    }

    /**
     * Delete all lucene index docs
     */
    @Override
    synchronized public void truncate() {
        try {
            _reopenToken = _trackingIndexWriter.deleteAll();
            LOG.warn("lucene index truncated");
        } catch (IOException ioEx) {
            LOG.error("Error truncating lucene index: {}", ioEx.getMessage(),
                    ioEx);
        } finally {
            try {
                _indexWriter.commit();
            } catch (IOException ioEx) {
                LOG.error("Error truncating lucene index: {}", ioEx.getMessage(),
                        ioEx);
            }
        }
    }

    @Override
    synchronized public long count(KensakuQuery kensakuQuery) throws Exception {
        Query qry = kensakuQueryBuilder.buildQuery(kensakuQuery);
        long outCount = 0;
        try {
            _indexSearcherReopenThread.waitForGeneration(_reopenToken);     // wait untill the index is re-opened
            IndexSearcher searcher = _indexSearcherReferenceManager.acquire();
            try {
                TopDocs docs = searcher.search(qry, 0);
                if (docs != null) {
                    outCount = docs.totalHits;
                }
                LOG.debug("count-search executed against lucene index returning {}", outCount);
            } finally {
                _indexSearcherReferenceManager.release(searcher);
            }
        } catch (IOException ioEx) {
            LOG.error("Error re-opening the index {}", ioEx.getMessage(), ioEx);
            throw ioEx;
        } catch (InterruptedException intEx) {
            LOG.error("The index writer periodically re-open thread has stopped", intEx.getMessage(), intEx);
        }
        return outCount;
    }

    @Override
    synchronized public KensakuResults search(KensakuQuery kensakuQuery) throws Exception {
        Query query = kensakuQueryBuilder.buildQuery(kensakuQuery);
        Set<SortField> sortFields = null;
        final int firstResultOffest = kensakuQuery.firstResult;
        final int numberOfResults = kensakuQuery.numberOfResults;
        POCResultBuilder kensakuResulterizer = kensakuResultBuilderProvider.create(kensakuQuery);

        KensakuResults outDocs = null;
        try {
            _indexSearcherReopenThread.waitForGeneration(_reopenToken);
            IndexSearcher searcher = _indexSearcherReferenceManager.acquire();
            try {
                // sort crieteria
                Sort theSort = null;
                if (sortFields != null && !sortFields.isEmpty()) {
                    theSort = new Sort(sortFields.toArray(new SortField[sortFields.size()]));
                }
                // number of results to be returned
                int theNumberOfResults = firstResultOffest + numberOfResults;

                // Exec the search (if the sort criteria is null, they're not used)
                TopDocs scoredDocs = theSort != null ? searcher.search(query,
                        theNumberOfResults,
                        theSort)
                        : searcher.search(query,
                                theNumberOfResults);
                LOG.info("query {} {} executed against lucene index: returned {} total items, {} in this page", query.toString(),
                        (theSort != null ? theSort.toString() : ""),
                        scoredDocs != null ? scoredDocs.totalHits : 0,
                        scoredDocs != null ? scoredDocs.scoreDocs.length : 0);

                outDocs = kensakuResulterizer.create(searcher,
                        scoredDocs,
                        firstResultOffest,
                        numberOfResults);
            } finally {
                _indexSearcherReferenceManager.release(searcher);
            }
        } catch (IOException ioEx) {
            LOG.error("Error freeing the searcher {}", ioEx.getMessage(),
                    ioEx);
        } catch (InterruptedException intEx) {
            LOG.error("The index writer periodically re-open thread has stopped", intEx.getMessage(),
                    intEx);
        }
        return outDocs;
    }

    @Override
    synchronized public void optimize() {
        try {
            _indexWriter.forceMerge(1);
            LOG.debug("Lucene index merged into one segment");
        } catch (IOException ioEx) {
            LOG.error("Error optimizing lucene index {}", ioEx.getMessage(), ioEx);
        }
    }
}