/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    private static Map<Thread, Integer> transfers = new HashMap<Thread, Integer>();
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
                count = count + 1;
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
                count = count - 1;
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
