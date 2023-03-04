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
/*
 * Contributor(s): theanuradha@netbeans.org
 */

package org.netbeans.modules.maven.indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;

public class RemoteIndexTransferListener implements TransferListener, Cancellable {

    private static final Logger LOG = Logger.getLogger(RemoteIndexTransferListener.class.getName());

    private final @NonNull ProgressHandle handle;
    private final RepositoryInfo info;
    private long lastunit;/*last work unit*/
    private long units;
    private ResourceFetcher fetcher;

    private final AtomicBoolean canceled = new AtomicBoolean();
    private final AtomicBoolean unpacking = new AtomicBoolean();

    private static final Map<Thread, Integer> transfers = new HashMap<>();
    private static final Object TRANSFERS_LOCK = new Object();

    @SuppressWarnings("LeakingThisInConstructor")
    @Messages({"# {0} - repo name", "LBL_Transfer=Transferring Maven repository index: {0}"})
    public RemoteIndexTransferListener(RepositoryInfo info) {
        this.info = info;
        Cancellation.register(this);
        handle = ProgressHandle.createHandle(Bundle.LBL_Transfer(info.getName()), this);
        handle.start();
    }

    void setFetcher(ResourceFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public @Override void transferInitiated(TransferEvent e) {
        String u = e.getWagon().getRepository().getUrl() + e.getResource();
        LOG.log(Level.FINE, "initiated transfer: {0}", u);
        handle.progress(u);
        checkCancel();
    }

    public @Override void transferStarted(TransferEvent e) {
        checkCancel();
        long contentLength = e.getResource().getContentLength();
        LOG.log(Level.FINE, "contentLength: {0}", contentLength);
        // #189806: could be resumed due to FNFE in DefaultIndexUpdater (*.gz -> *.zip)
        this.units = contentLength / 1024;        
        handle.switchToDeterminate(100);
    }

    public @Override boolean cancel() {
        handle.finish();
        if (fetcher != null) {
            try {
                fetcher.disconnect();
            } catch (IOException x) {
                LOG.log(Level.INFO, "closing " + info.getId(), x);
            }
        }
        return canceled.compareAndSet(false, true);
    }

    private void checkCancel() throws Cancellation {
        if (canceled.get()) {
            throw new Cancellation();
        }
    }

    public @Override void transferProgress(TransferEvent e, byte[] buffer, int length) {
        checkCancel();
        LOG.log(Level.FINER, "progress: {0}", length);
        int work = length / 1024;
        if(units > 0) {
            lastunit += work;
            handle.progress(Math.min(100, (int)(((double) lastunit / units) * 100)));
        }
    }

    public @Override void transferCompleted(TransferEvent e) {
        LOG.fine("completed");
        handle.switchToIndeterminate();
    }

    public @Override void transferError(TransferEvent e) {
        LOG.log(Level.FINE, "error transferring", e.getException());
        handle.switchToIndeterminate();
    }

    public @Override void debug(String message) {
        checkCancel();
    }

    static void addToActive (Thread t) {
        synchronized (TRANSFERS_LOCK) {
            Integer count = transfers.get(t);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            transfers.put(t, count);
        }
    }

    static void removeFromActive (Thread t) {
        synchronized (TRANSFERS_LOCK) {
            Integer count = transfers.get(t);
            if (count == null) {
                return;
            }
            if (count <= 1) {
                transfers.remove(t);
            } else {
                count--;
                transfers.put(t, count);
            }
        }
    }

    static Set<Thread> getActiveTransfersOrScans () {
        synchronized (TRANSFERS_LOCK) {
            return transfers.keySet();
        }
    }

    @Messages({"# {0} - repo name", "LBL_unpacking=Unpacking index for {0}"})
    void unpackingProgress(String label) {
        checkCancel();
        if (unpacking.compareAndSet(false, true)) {
            handle.setDisplayName(Bundle.LBL_unpacking(info.getName()));
        }
        handle.progress(label);
    }

    void close() {
        handle.finish();
    }

    long getUnits() {
        return units;
    }
    
}
