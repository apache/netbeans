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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmStandaloneFileProviderImpl.NativeProjectImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class FakeRegistrationWorker {
    private final RequestProcessor FAKE_REGISTRATION_WORKER;
    private final ProjectBase project;
    private final AtomicBoolean disposing;

    FakeRegistrationWorker(ProjectBase project, AtomicBoolean disposing) {
        this.project = project;
        this.disposing = disposing;
        this.FAKE_REGISTRATION_WORKER = new RequestProcessor("Fake Registration Worker " + project.getDisplayName(), CndUtils.getNumberCndWorkerThreads()); // NOI18N
    }

    void fixFakeRegistration(boolean libsAlreadyParsed){
        long time = System.currentTimeMillis();
        Collection<CsmUID<CsmFile>> files = project.getAllFilesUID();
        int size = files.size();
        int threads = CndUtils.getNumberCndWorkerThreads()*3;
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        int chunk = (size/threads) + 1;
        Iterator<CsmUID<CsmFile>> it = files.iterator();
        for (int i = 0; i < threads; i++) {
            ArrayList<CsmUID<CsmFile>> list = new ArrayList<>(chunk);
            for(int j = 0; j < chunk; j++) {
                if (it.hasNext()) {
                    list.add(it.next());
                } else {
                    break;
                }
            }
            FixRegistrationRunnable r = new FixRegistrationRunnable(countDownLatch, list, libsAlreadyParsed, disposing);
            if (project.getPlatformProject() instanceof NativeProjectImpl) {
                // parallel execution for standalone project causes deadlock
                // see Bug 198949 - Test failed with "Timeout occurred"
                r.run();
            } else {
                FAKE_REGISTRATION_WORKER.post(r);
            }
        }
        try {
            countDownLatch.await();
            time = System.currentTimeMillis() - time;
            if (libsAlreadyParsed) {
                project.cleanAllFakeFunctionAST();
            }
            if (TraceFlags.TIMING) {
                Logger.getLogger(FakeRegistrationWorker.class.getSimpleName()).log(Level.INFO, "FAKE REGISTRATION {0} took {1}ms\n", new Object[] {project.getName(), time});
            }
        } catch (InterruptedException ex) {
        }
    }

    private static class FixRegistrationRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private final List<CsmUID<CsmFile>> files;
        private final boolean libsAlreadyParsed;
        private final AtomicBoolean cancelled;
        private FixRegistrationRunnable(CountDownLatch countDownLatch, List<CsmUID<CsmFile>> files, boolean libsAlreadyParsed, AtomicBoolean cancelled){
            this.countDownLatch = countDownLatch;
            this.files = files;
            this.libsAlreadyParsed = libsAlreadyParsed;
            this.cancelled = cancelled;
        }
        @Override
        public void run() {
            try {
                for(CsmUID<CsmFile> file : files) {
                    if (cancelled.get()) {
                        return;
                    }
                    if (file == null){
                        return;
                    }
                    FileImpl impl = (FileImpl) UIDCsmConverter.UIDtoFile(file);
                    CndUtils.assertTrueInConsole(impl != null, "no deref file for ", file); // NOI18N
                    // situation is possible for standalone files which were already replaced
                    // by real files
                    if (impl == null) {
                        return;
                    }
                    String oldName = Thread.currentThread().getName();
                    try {
                      Thread.currentThread().setName("Fix registration "+file); // NOI18N
                      impl.onProjectParseFinished(libsAlreadyParsed);
                    } finally {
                      Thread.currentThread().setName(oldName);
                    }
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }


}
