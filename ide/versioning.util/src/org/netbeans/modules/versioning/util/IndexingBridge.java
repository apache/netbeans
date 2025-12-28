/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * This class provides means for tight integration between VCS modules and the indexing
 * infrastructure.
 *
 * @author Vita Stejskal
 * @since 1.6
 */
public final class IndexingBridge {

    private static final Logger LOG = Logger.getLogger(IndexingBridge.class.getName());
    private static IndexingBridge instance = null;
    
    private int delayBeforeRefresh = -1;
    private static final int DEFAULT_DELAY = 100;
    
    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Gets the signleton instance of <code>IndexingBridge</code>.
     *
     * @return The <code>IndexingBridge</code> instance.
     */
    public static synchronized IndexingBridge getInstance() {
        if (instance == null) {
            instance = new IndexingBridge();
        }
        return instance;
    }

    /**
     * Runs the <code>operation</code> without interfering with indexing. The indexing
     * is blocked while the operation is runing and all events that would normally trigger
     * reindexing will be processed after the operation is finished. <br>
     * The filesytem will be asynchronously refreshed for all parent folders from the given files
     * after the operation has finished
     *
     * @param operation The operation to run.
     * @param files Files or folders affected by the operation.
     * @return Whatever value is returned from the <code>operation</code>.
     *
     * @throws Exception Any exception thrown by the <code>operation</code> is going to be rethrown from
     *   this method.
     * @see FileUtil#refreshFor(java.io.File[]) 
     */
    public <T> T runWithoutIndexing(Callable<T> operation, File... files) throws Exception {
        return runWithoutIndexing(operation, true, files);
    }

    /**
     * Runs the <code>operation</code> without interfering with indexing. The indexing
     * is blocked while the operation is runing and all events that would normally trigger
     * reindexing will be processed after the operation is finished. <br>
     * Depenpending on the <code>refreshFS</code> paramter the filesytem will be asynchronously  refreshed
     * for all parent folders from the given files after the operation has finished.
     *
     * @param operation The operation to run.
     * @param refreshFS determines if the filesystem will be refreshed after the operation.
     * @param files Files or folders affected by the operation.
     * @return Whatever value is returned from the <code>operation</code>.
     *
     * @throws Exception Any exception thrown by the <code>operation</code> is going to be rethrown from
     *   this method.
     */
    public <T> T runWithoutIndexing(Callable<T> operation, boolean refreshFS, File... files) throws Exception {
        IndexingBridgeProvider ibp = Lookup.getDefault().lookup(IndexingBridgeProvider.class);
        if (ibp != null) {
            if(LOG.isLoggable(Level.FINE)) {
                StringBuffer sb = new StringBuffer();
                if(files != null) {
                    for (File file : files) {
                        sb.append("\n");                                        // NOI18N
                        sb.append(file.getAbsolutePath());
                    }
                }
                LOG.fine("running vcs operaton without scheduling for files:" + sb.toString()); // NOI18N
            }
            if (files != null) {
                // normalize all files
                for (int i = 0; i < files.length; ++i) {
                    files[i] = FileUtil.normalizeFile(files[i]);
                }
            }
            return ibp.runWithoutIndexing(operation, files);
        } else {
            try {
                return operation.call();
            } finally {
                final Set<File> parents = new HashSet<File>();
                for (File f : files) {
                    File parent = f.getParentFile();
                    if (parent != null) {
                        parents.add(parent);
                        LOG.fine("scheduling for fs refresh: [" + parent + "]"); // NOI18N
                    }
                }

                if (refreshFS && parents.size() > 0) {
                    // let's give the filesystem some time to wake up and to realize that the file has really changed
                    Utils.postParallel(new Runnable() {
                        @Override
                        public void run() {
                            long t = System.currentTimeMillis();
                            try {
                                FileUtil.refreshFor(parents.toArray(new File[0]));
                            } finally {                                
                                LOG.fine(" refreshing " + parents.size() + " parents took " + (System.currentTimeMillis() - t) + " millis.");
                            }
                        }
                    }, getDelay()); 
                }
            }
        }
    }

    /**
     * Determines if projects are being indexed or not.
     *
     * @return <code>true</code> if projects are being scanned, <code>false</code> otherwise.
     * @since 1.7
     */
    public boolean isIndexingInProgress() {
        IndexingBridgeProvider ibp = Lookup.getDefault().lookup(IndexingBridgeProvider.class);
        if (ibp != null) {
            return ibp.isIndexingInProgress();
        } else {
            return false;
        }
    }

    /**
     * This interface is supposed to be implemented by the actual bridge module
     * that connect the versioning and indexing infrastructure. Ordinary VCS support
     * modules do not need to implement this interface.
     * <p>Implementations of this interface ought to be registered via {@link ServiceProvider} annotation
     * in the default <code>Lookup</code>.
     */
    public static interface IndexingBridgeProvider {
        /**
         * This method is te actual integration point between versioning and indexing.
         * A typical implementation should do something like this:
         * <ul>
         * <li>call the indexing infrastructure and turn off the immediate processing of events</li>
         * <li>call <code>operation.call()</code></li>
         * <li>call the indexing infrastructure to process all the events gatherd while</li>
         * the operation was running and turn on the immediate processing of events
         * </ul>
         *
         * @param operation The operation to run.
         * @param files Files or folders affected by the operation.
         * @return Whatever value is returned from the <code>operation</code>.
         *
         * @throws Exception Any exception thrown by the <code>operation</code> is going to rethrown from
         *   this method.
         */
        <T> T runWithoutIndexing(Callable<T> operation, File... files) throws Exception;

        /**
         * Determines if projects are being indexed or not.
         *
         * @return <code>true</code> if projects are being scanned, <code>false</code> otherwise.
         * @since 1.7
         */
        boolean isIndexingInProgress();
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------
    
    private IndexingBridge() {
        // no-op
    }

    private int getDelay() {
        if (delayBeforeRefresh == -1) {
            String delayProp = System.getProperty("vcs.fsrefresh.delay", Integer.toString(DEFAULT_DELAY)); //NOI18N
            int delay = DEFAULT_DELAY;
            try {
                delay = Integer.parseInt(delayProp);
            } catch (NumberFormatException e) {
                LOG.log(Level.FINE, null, e);
            }
            delayBeforeRefresh = delay;
        }
        return delayBeforeRefresh;
    }

}
