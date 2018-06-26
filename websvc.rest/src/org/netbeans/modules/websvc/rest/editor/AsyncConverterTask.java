/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
