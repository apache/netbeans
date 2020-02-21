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
package org.netbeans.modules.remotefs.versioning.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileObjectIndexingBridgeProvider {
    private static final Logger LOG = Logger.getLogger(FileObjectIndexingBridgeProvider.class.getName());
    private static FileObjectIndexingBridgeProvider instance;
    
    /**
     * Gets the singleton instance of <code>FileObjectIndexingBridgeProvider</code>.
     *
     * @return The <code>FileObjectIndexingBridgeProvider</code> instance.
     */
    public static synchronized FileObjectIndexingBridgeProvider getInstance() {
        if (instance == null) {
            instance = new FileObjectIndexingBridgeProvider();
        }
        return instance;
    }
    
    private FileObjectIndexingBridgeProvider() {
    }
    
    /**
     * Runs the <code>operation</code> without interfering with indexing. The indexing
     * is blocked while the operation is running and all events that would normally trigger
     * reindexing will be processed after the operation is finished. <br>
     * The filesystem will be asynchronously refreshed for all parent folders from the given files
     * after the operation has finished
     *
     * @param operation The operation to run.
     * @param files Files or folders affected by the operation.
     * @return Whatever value is returned from the <code>operation</code>.
     *
     * @throws Exception Any exception thrown by the <code>operation</code> is going to be rethrown from
     *   this method.
     */
    public <T> T runWithoutIndexing(final Callable<T> operation, VCSFileProxy ... files) throws Exception {
        if(LOG.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer();
            if(files != null) {
                for (VCSFileProxy file : files) {
                    sb.append("\n"); // NOI18N
                    sb.append(file.getPath());
                }
            }
            LOG.fine("running vcs operaton without scheduling for files:" + sb.toString()); // NOI18N
        }
        final List<FileObject> fos = new ArrayList<>();
        for (VCSFileProxy f : files) {
            FileObject fo = f.normalizeFile().toFileObject();
            if (fo != null) {
                fos.add(fo);
            }
        }
        return IndexingManager.getDefault().runProtected(new Callable<T>() {
            @Override
            public T call() throws Exception {
                // Schedule the refresh task, which will then absorb all other tasks generated
                // by filesystem events caused by the operation
                IndexingManager.getDefault().refreshAllIndices(false, false, fos.toArray(new FileObject[fos.size()]));
                return operation.call();
            }
        });
    }


    /**
     * Determines if projects are being indexed or not.
     *
     * @return <code>true</code> if projects are being scanned, <code>false</code> otherwise.
     * @since 1.7
     */
    public boolean isIndexingInProgress() {
        return IndexingManager.getDefault().isIndexing();
    }
}
