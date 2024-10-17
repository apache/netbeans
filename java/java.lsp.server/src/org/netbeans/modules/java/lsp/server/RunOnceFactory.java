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
package org.netbeans.modules.java.lsp.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=JavaSourceTaskFactory.class),
    @ServiceProvider(service=RunOnceFactory.class)
})
public class RunOnceFactory extends JavaSourceTaskFactory {

    private static final Logger LOG = Logger.getLogger(RunOnceFactory.class.getName());

    private List<Pair<FileObject, CancellableTask<CompilationInfo>>> work = new LinkedList<>();
    private FileObject currentFile;
    private CancellableTask<CompilationInfo> task;

    public RunOnceFactory() {
        super(Phase.RESOLVED, Priority.BELOW_NORMAL);
//        INSTANCE = this;
    }

    protected synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        final CancellableTask<CompilationInfo> task = this.task;
        return new CancellableTask<CompilationInfo>() {
            public void cancel() {
                task.cancel();
            }
            public void run(CompilationInfo parameter) throws Exception {
                task.run(parameter);
                next();
            }
        };
    }

    protected synchronized Collection<FileObject> getFileObjects() {
        if (currentFile == null)
            return Collections.<FileObject>emptyList();

        return Collections.<FileObject>singletonList(currentFile);
    }

    private synchronized void addImpl(FileObject file, CancellableTask<CompilationInfo> task) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "addImpl({0}, {1})", new Object[] {FileUtil.getFileDisplayName(file), task.getClass().getName()});
        }

        work.add(Pair.<FileObject, CancellableTask<CompilationInfo>>of(file, task));

        if (currentFile == null)
            next();
    }

    private synchronized void next() {
        LOG.fine("next, phase 1");

        if (currentFile != null) {
            currentFile = null;
            task = null;
            fileObjectsChanged();
        }

        LOG.fine("next, phase 1 done");

        if (work.isEmpty())
            return ;

        LOG.fine("next, phase 2");

        Pair<FileObject, CancellableTask<CompilationInfo>> p = work.remove(0);

        currentFile = p.first();
        task = p.second();

        fileObjectsChanged();

        LOG.fine("next, phase 2 done");
    }


    public static void add(FileObject file, CancellableTask<CompilationInfo> task) {
        RunOnceFactory factory = Lookup.getDefault().lookup(RunOnceFactory.class);

        if (factory == null)
            return ;

        factory.addImpl(file, task);
    }
}
