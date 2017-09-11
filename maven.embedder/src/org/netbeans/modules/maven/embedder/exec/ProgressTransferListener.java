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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.embedder.exec;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import static org.netbeans.modules.maven.embedder.exec.Bundle.*;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transfer.TransferResource;

public class ProgressTransferListener implements TransferListener {

    private static final ThreadLocal<ProgressTransferListener> activeListener = new ThreadLocal<ProgressTransferListener>() {
        protected @Override ProgressTransferListener initialValue() {
            return new ProgressTransferListener();
        }
    };
    private ProgressTransferListener() {}
    public static ProgressTransferListener activeListener() {
        return activeListener.get();
    }

    private int length;
    private ProgressContributor contrib;
    private ProgressContributor pomcontrib;
    private int pomCount;
    private final Stack<ProgressContributor> contribStack = new Stack<ProgressContributor>();
    private AggregateProgressHandle handle;
    public AtomicBoolean cancel;
    private static final int POM_MAX = 20;

    public static void setAggregateHandle(AggregateProgressHandle hndl) {
        ProgressTransferListener ptl = activeListener();
        ptl.handle = hndl;
        ProgressContributor pc = AggregateProgressFactory.createProgressContributor("Pom files");
        hndl.addContributor(pc);
        pc.start(POM_MAX);
        ptl.pomcontrib = pc;
    }
    
    public static void clearAggregateHandle() {
        activeListener.remove();
    }

    /**
     * Produces a token which may be passed to {@link AggregateProgressFactory#createHandle}
     * in order to permit progress to be canceled.
     * If an event is received after a cancel request has been made, {@link ThreadDeath} will
     * be thrown (which you probably also want to catch and handle gracefully).
     * Due to AETHER-95, {@link IllegalStateException} with a cause of {@link ThreadDeath} might also be thrown.
     * Must be called by the same thread as will call {@link #setAggregateHandle} and runs the process.
     * @return a cancellation token
     */
    public static Cancellable cancellable() {
        final AtomicBoolean b = new AtomicBoolean();
        activeListener().cancel = b;
        return new Cancellable() {
            public @Override boolean cancel() {
                return b.compareAndSet(false, true);
            }
        };
    }

    private void checkCancel() {
        if (cancel != null && cancel.get()) {
            throw new ThreadDeath();
        }
    }

    private String getResourceName(TransferResource res) {
        int lastSlash = res.getResourceName().lastIndexOf('/');
        return lastSlash > -1 ? res.getResourceName().substring(lastSlash + 1) : res.getResourceName();
    }
    
    @Messages({
        "# {0} - downloaded resource name",
        "TXT_Download=Downloading {0}",
        "# {0} - uploaded resource name",
        "TXT_Uploading=Uploading {0}",
        "# {0} - transferred resource name",
        "TXT_Started={0} - Transfer Started..."
    })
    @Override
    public void transferInitiated(TransferEvent te) throws TransferCancelledException {
        if (handle == null) {
            //maybe log?
            return;
        }
        
        TransferResource res = te.getResource();
        String resName = getResourceName(res);
        if (!resName.endsWith(".pom")) { //NOI18N
            ProgressContributor pc = !contribStack.empty() ? contribStack.pop() : null;
            if (pc == null) {
                String name = (te.getRequestType() == TransferEvent.RequestType.GET
                        ? TXT_Download(resName)
                        : TXT_Uploading(resName));
                pc = AggregateProgressFactory.createProgressContributor(name);
                handle.addContributor(pc);
            }
            contrib = pc;
        } else {
            String name = (te.getRequestType() == TransferEvent.RequestType.GET
                    ? TXT_Download(resName)
                    : TXT_Uploading(resName));
            ProgressContributor pc = AggregateProgressFactory.createProgressContributor(name);
            contribStack.add(pc);
            handle.addContributor(pc);
            if (pomCount < POM_MAX - 1) {
                pomcontrib.progress(TXT_Started(resName), ++pomCount);
            } else {
                pomcontrib.progress(TXT_Started(resName));
            }
        }
    }

    @Override
    public void transferStarted(TransferEvent te) throws TransferCancelledException {
        ProgressContributor c = contrib;
        if (c == null || handle == null) {
            return;
        }
        TransferResource res = te.getResource();
        int total = (int)Math.min((long)Integer.MAX_VALUE, res.getContentLength());
        if (total < 0) {
            c.start(0);
        } else {
            c.start(total);
        }
        length = total;
        c.progress(TXT_Started(getResourceName(res)));
    }

    @Messages({
        "# {0} - transferring resource name",
        "TXT_Transferring={0} - Transferring...",
        "# {0} - transferring resource name",
        "# {1} - transferred amount",
        "TXT_Transferred={0} - Transferred {1}"
    })
    @Override
    public void transferProgressed(TransferEvent te) throws TransferCancelledException {
        checkCancel();
        ProgressContributor c = contrib;
        if (c == null) {
            return;
        }
        long cnt = te.getTransferredBytes();
        cnt = Math.min((long)Integer.MAX_VALUE, cnt);
        if (length < 0) {
            c.progress(TXT_Transferring(getResourceName(te.getResource())));
        } else {
            cnt = Math.min(cnt, (long)length);
            c.progress(TXT_Transferred(getResourceName(te.getResource()), cnt), (int)cnt);
        }
    }

    @Override
    public void transferCorrupted(TransferEvent te) throws TransferCancelledException {
        ProgressContributor c = contrib;
        contrib = null;
        if (c != null) {
            c.finish();
        }
    }

    @Override
    public void transferSucceeded(TransferEvent te) {
        ProgressContributor c = contrib;
        contrib = null;
        if (c != null) {
            c.finish();
        }
    }

    @Override
    public void transferFailed(TransferEvent te) {
        ProgressContributor c = contrib;
        contrib = null;
        if (c != null) {
            c.finish();
        }
    }

}
