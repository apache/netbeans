/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 * Commits Class Indexes during scanFinished.
 *
 * @author sdedic
 */
//@NotThreadSafe
public final class PersistentIndexTransaction extends TransactionContext.Service {

    private static final Logger LOG = Logger.getLogger(PersistentIndexTransaction.class.getName());

    private final URL root;

    private ClassIndexImpl.Writer indexWriter;
    private boolean closedTx;
    private boolean brokenIndex;

    private PersistentIndexTransaction(@NonNull final URL root) {
        this.root = root;
    }

    @NonNull
    public static PersistentIndexTransaction create(@NonNull final URL root) {
        return new PersistentIndexTransaction(root);
    }

    @Override
    protected void commit() throws IOException {
        closeTx();
        if (indexWriter != null) {
            if (!brokenIndex) {
                try {
                    indexWriter.commit();
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    } else {
                        LOG.log(
                            Level.WARNING,
                            "Broken index for root: {0} reason: {1}, recovering.",  //NOI18N
                            new Object[] {
                                root,
                                t.getMessage()
                            });
                        brokenIndex = true;
                    }
                }
            } else {
                rollBackImpl();
            }
            if (brokenIndex) {
                handleBrokenRoot();
            }
        }
    }

    @Override
    protected void rollBack() throws IOException {
        closeTx();
        if (indexWriter != null) {            
            rollBackImpl();
            if (brokenIndex) {
                handleBrokenRoot();
            }
        }
    }
    
    public void setIndexWriter(@NonNull ClassIndexImpl.Writer writer) {
        assert this.indexWriter == null;
        assert writer != null;
        this.indexWriter = writer;
    }

    public void setBroken() {
        brokenIndex = true;
    }
    
    @CheckForNull
    public ClassIndexImpl.Writer getIndexWriter() {
        return this.indexWriter;
    }

    private void closeTx() {
        if (closedTx) {
            throw new IllegalStateException("Already commited or rolled back transaction.");    //NOI18N
        }
        closedTx = true;
    }

    private void handleBrokenRoot() throws IOException {
        indexWriter.clear();
        IndexingManager.getDefault().refreshIndex(root, null, true, false);
    }

    private void rollBackImpl() {
        try {
            indexWriter.rollback();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else {
                LOG.log(
                    Level.WARNING,
                    "Broken index for root: {0} reason: {1}, recovering.",  //NOI18N
                    new Object[] {
                        root,
                        t.getMessage()
                    });
                brokenIndex = true;
            }
        }
    }
}
