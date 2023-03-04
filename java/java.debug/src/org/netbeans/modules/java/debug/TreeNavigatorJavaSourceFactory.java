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
package org.netbeans.modules.java.debug;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public final class TreeNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
    
    private CancellableTask<CompilationInfo> task;
    
    static TreeNavigatorJavaSourceFactory getInstance() {
        for (JavaSourceTaskFactory f :  Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof TreeNavigatorJavaSourceFactory) {
                return (TreeNavigatorJavaSourceFactory) f;
            }
        }
        return null;
    }
    
    public TreeNavigatorJavaSourceFactory() {
        super(Phase.UP_TO_DATE, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    public synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        //XXX: should not be necessary to do the wrapper task, but for some reason it is necessary:
        return new WrapperTask(task);
    }

    @Override
    public List<FileObject> getFileObjects() {
        List<FileObject> result = super.getFileObjects();

        if (result.size() == 1)
            return result;

        return Collections.emptyList();
    }

    public FileObject getFile() {
        List<FileObject> result = super.getFileObjects();
        
        if (result.size() == 1)
            return result.get(0);
        
        return null;
    }

    public synchronized void setLookup(Lookup l, CancellableTask<CompilationInfo> task) {
        this.task = task;
        super.setLookup(l);
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class CaretAwareFactoryImpl extends CaretAwareJavaSourceTaskFactory {

        static CaretAwareFactoryImpl getInstance() {
            return Lookup.getDefault().lookup(CaretAwareFactoryImpl.class);
        }

        private CancellableTask<CompilationInfo> task;

        public CaretAwareFactoryImpl() {
            super(Phase.UP_TO_DATE, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        protected synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new WrapperTask(task);
        }

        @Override
        public List<FileObject> getFileObjects() {
            List<FileObject> result = super.getFileObjects();

            if (result.size() == 1)
                return result;

            return Collections.emptyList();
        }

        public FileObject getFile() {
            List<FileObject> result = super.getFileObjects();

            if (result.size() == 1)
                return result.get(0);

            return null;
        }

        public synchronized void setTask(CancellableTask<CompilationInfo> task) {
            this.task = task;
            FileObject file = getFile();
            if (file != null) {
                reschedule(file);
            }
        }
    }

    static class WrapperTask implements CancellableTask<CompilationInfo> {
        
        private final CancellableTask<CompilationInfo> delegate;
        
        public WrapperTask(CancellableTask<CompilationInfo> delegate) {
            this.delegate = delegate;
        }

        public void cancel() {
            if (delegate != null)
                delegate.cancel();
        }

        public void run(CompilationInfo parameter) throws Exception {
            if (delegate != null)
                delegate.run(parameter);
        }
        
    }
}
