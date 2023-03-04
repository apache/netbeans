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
package org.netbeans.modules.websvc.rest.editor;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class RestConfigurationTask implements CancellableTask<CompilationInfo> {

    RestConfigurationTask( RestConfigurationEditorAwareTaskFactory factory,
            FileObject fileObject )
    {
        this.factory = factory;
        this.fileObject = fileObject;
        task = new AtomicReference<RestScanTask>();
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.Task#run(java.lang.Object)
     */
    @Override
    public void run( CompilationInfo info ) throws Exception {
        RestScanTask scanTask = new RestScanTask(factory, fileObject, info );
        task.set(scanTask);
        scanTask.run();
        task.compareAndSet(scanTask, null);
        HintsController.setErrors(fileObject, "REST Configuration",         // NOI18N 
                scanTask.getHints()); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.CancellableTask#cancel()
     */
    @Override
    public void cancel() {
        RestScanTask scanTask = task.getAndSet(null);
        if ( scanTask != null ){
            scanTask.stop();
        }
    }
    
    private AtomicReference<RestScanTask> task;
    private FileObject fileObject;
    private RestConfigurationEditorAwareTaskFactory factory;

}
