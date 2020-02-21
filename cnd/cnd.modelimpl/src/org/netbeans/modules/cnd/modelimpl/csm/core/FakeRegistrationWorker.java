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
