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
package org.netbeans.modules.maven.jaxws.actions;

import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.maven.jaxws._RetoucheUtil;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class AddOperationAction extends NodeAction  {

    @Override
    public String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_OperationAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        FileObject implClassFo = activatedNodes[0].getLookup().lookup(FileObject.class);
        return implClassFo != null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        FileObject implClassFo = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (isFromWsdl(implClassFo)) {
            DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    NbBundle.getMessage(AddOperationAction.class, "LBL_CannotRunOnWsdl")));
            return;
        }
        AddOperationCookie addOperationCookie = new JaxWsAddOperation(implClassFo);
        addOperationCookie.addOperation();
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    private boolean isFromWsdl(FileObject inplClass) {
        final boolean[] fromWsdl = new boolean[1];
        JavaSource javaSource = JavaSource.forFileObject(inplClass);
        if (javaSource != null) {

            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

                @Override
                public void run(CompilationController controller) throws java.io.IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                    if ( typeElement == null ){
                        return;
                    }
                    AnnotationMirror annMirror = _RetoucheUtil.getAnnotation(
                            controller, typeElement, "javax.jws.WebService");   //NOI18N
                    if ( annMirror == null ){
                        return;
                    }
                    Map<? extends ExecutableElement, ? extends AnnotationValue> 
                        expressions = annMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, 
                            ? extends AnnotationValue> entry : expressions.entrySet()) 
                    {
                        if (entry.getKey().getSimpleName().contentEquals("wsdlLocation")) { //NOI18N
                            fromWsdl[0] = true;
                            return;
                        }
                    }
                }

                @Override
                public void cancel() {
                }

            };

            try {
                javaSource.runUserActionTask(task, true);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }

        return fromWsdl[0];
    }
}

