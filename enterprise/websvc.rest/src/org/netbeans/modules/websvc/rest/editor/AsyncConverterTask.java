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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.MethodTree;


/**
 * @author ads
 *
 */
public class AsyncConverterTask extends AsyncConverter implements CancellableTask<CompilationInfo> {

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.Task#run(java.lang.Object)
     */
    @Override
    public void run( CompilationInfo compilationInfo ) throws Exception {
        FileObject fileObject = compilationInfo.getFileObject();
        
        if( !isApplicable(fileObject)){
            return;
        }
        
        AsyncHintsTask task = new AsyncHintsTask(compilationInfo);
        runTask.set(task);
        task.run();
        runTask.compareAndSet(task, null);
        HintsController.setErrors(fileObject, "REST Async Converter",         // NOI18N 
                task.getDescriptions()); 
    }
    
    @Override
    protected Logger getLogger() {
        return Logger.getLogger(AsyncConverterTask.class.getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.java.source.CancellableTask#cancel()
     */
    @Override
    public void cancel() {
        AsyncHintsTask scanTask = runTask.getAndSet(null);
        if ( scanTask != null ){
            scanTask.stop();
        }
    }
    
    private class AsyncHintsTask {

        private AsyncHintsTask( CompilationInfo info ) {
            myInfo = info;
            descriptions = new LinkedList<ErrorDescription>();
        }
        
        void run(){
            List<? extends TypeElement> classes = myInfo.getTopLevelElements();
            for (TypeElement clazz : classes) {
                if ( stop ){
                    return;
                }
                String fqn = clazz.getQualifiedName().toString();
                List<ExecutableElement> methods = ElementFilter.methodsIn(
                        clazz.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    if ( stop ){
                        return;
                    }
                    if( !isApplicable(method)){
                        continue;
                    }
                    if (!checkRestMethod(fqn, method, myInfo.getFileObject())){
                        continue;
                    }
                    if ( isAsync(method)){
                        continue;
                    }
                    MethodTree tree = myInfo.getTrees().getTree(method);
                    if (tree == null) {
                        continue;
                    }
                    List<Integer> position = RestScanTask.
                            getElementPosition(myInfo, tree);
                    Fix fix = new AsyncHint(myInfo.getFileObject(), 
                            ElementHandle.<Element>create(method));
                    List<Fix> fixes = Collections.singletonList(fix);
                    ErrorDescription description = ErrorDescriptionFactory
                            .createErrorDescription(Severity.HINT,
                                    NbBundle.getMessage(AsyncConverterTask.class,
                                            "TXT_ConvertMethod"),    // NOI18N
                                            fixes , 
                                            myInfo.getFileObject(), position.get(0),
                                            position.get(1));
                    getDescriptions().add(description);
                }
            }
        }
        
        Collection<ErrorDescription> getDescriptions(){
            return descriptions;
        }
        
        void stop(){
            stop = true;
        }
        
        private final Collection<ErrorDescription> descriptions;
        private volatile boolean stop;
        private final CompilationInfo myInfo;
    }
    
    private class AsyncHint implements Fix {
        
        AsyncHint(FileObject fileObject , ElementHandle<Element> handle){
            myFileObject = fileObject;
            myHandle = handle;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            convertMethod(myHandle, myFileObject);
            return null;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(AsyncConverterTask.class,
                    "TXT_ConvertMethod");    // NOI18N
        }
        
        private final FileObject myFileObject;
        private final ElementHandle<Element> myHandle;
    }

    private final AtomicReference<AsyncHintsTask> runTask = new AtomicReference<AsyncHintsTask>();
}
