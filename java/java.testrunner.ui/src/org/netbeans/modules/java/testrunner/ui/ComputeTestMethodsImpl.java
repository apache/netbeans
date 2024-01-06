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
package org.netbeans.modules.java.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods.Factory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=JavaSourceTaskFactory.class)
public class ComputeTestMethodsImpl extends EditorAwareJavaSourceTaskFactory {

    private static final RequestProcessor WORKER = new RequestProcessor(ComputeTestMethodsImpl.class.getName(), 1, false, false);

    public ComputeTestMethodsImpl() {
        super(Phase.ELEMENTS_RESOLVED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new TaskImpl();
    }

    private static class TaskImpl implements CancellableTask<CompilationInfo> {

        private final AtomicReference<ComputeTestMethods> currentProvider = new AtomicReference<>();
        private final AtomicBoolean cancel = new AtomicBoolean();

        @Override
        public void cancel() {
            cancel.set(true);

            ComputeTestMethods provider = currentProvider.get();
            if (provider != null) {
                provider.cancel();
            }
        }

        @Override
        public void run(CompilationInfo info) throws Exception {
            cancel.set(false);
            Document doc = info.getDocument();

            if (doc == null) {
                return ;
            }

            List<TestMethod> methods = new ArrayList<>();

            for (Factory factory : Lookup.getDefault().lookupAll(Factory.class)) {
                try {
                    ComputeTestMethods ctm = factory.create();

                    currentProvider.set(ctm);

                    if (cancel.get()) {
                        return ;
                    }

                    List<TestMethod> currentMethods = ctm.computeTestMethods(info);

                    if (currentMethods != null) {
                        methods.addAll(currentMethods);
                    }
                } finally {
                    currentProvider.set(null);
                }

                if (!cancel.get()) {
                    List<TestMethod> updateMethods = new ArrayList<>(methods);
                    WORKER.post(() -> TestMethodController.setTestMethods(doc, updateMethods));
                }
            }
        }
    }

}
