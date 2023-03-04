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
package org.netbeans.modules.java.editor.fold;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public class JavaElementFoldManagerTaskFactory extends EditorAwareJavaSourceTaskFactory {

    public JavaElementFoldManagerTaskFactory() {
        super(Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        final CancellableTask<CompilationInfo> t = JavaElementFoldManager.JavaElementFoldTask.getTask(file);

        return new CancellableTask<CompilationInfo>() {
            @Override public void cancel() {
                t.cancel();
            }
            @Override public void run(CompilationInfo parameter) throws Exception {
                t.run(parameter);
            }
        };
    }

    public static void doRefresh(FileObject file) {
        for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof JavaElementFoldManagerTaskFactory) {
                ((JavaElementFoldManagerTaskFactory) f).reschedule(file);
            }
        }
    }
}
