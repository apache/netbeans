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
package org.netbeans.modules.web.beans.analysis;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.navigation.actions.WebBeansActionHelper;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
abstract class CancellableAnalysysTask implements CancellableTask<CompilationInfo>{
    
    CancellableAnalysysTask(FileObject javaFile,  
            CdiEditorAwareJavaSourceTaskFactory factory ) 
    {
        myFileObject = javaFile;
        myTask = new AtomicReference<AbstractAnalysisTask>();
        myFactory = factory;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.Task#run(java.lang.Object)
     */
    @Override
    public void run( CompilationInfo compInfo ) throws Exception {
        if ( !WebBeansActionHelper.isEnabled() ){
            return;
        }
        AbstractAnalysisTask task = createTask();
        myTask.set( task );
        task.run( compInfo );
        myTask.compareAndSet( task, null);
        HintsController.setErrors(myFileObject, getLayerName(), task.getProblems()); 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.CancellableTask#cancel()
     */
    @Override
    public void cancel() {
        AbstractAnalysisTask task = myTask.getAndSet(null);
        if ( task != null ){
            task.stop();
        }
    }
    
    protected abstract String getLayerName();
    
    protected abstract AbstractAnalysisTask createTask();
    
    protected FileObject getFileObject(){
        return myFileObject;
    }
    
    protected CdiEditorAwareJavaSourceTaskFactory getFactory(){
        return myFactory;
    }

    private FileObject myFileObject;
    private AtomicReference<AbstractAnalysisTask> myTask;
    private CdiEditorAwareJavaSourceTaskFactory myFactory;
}
