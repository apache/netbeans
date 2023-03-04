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

package org.netbeans.modules.websvc.core.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class SetSoapVersionActionGenerator implements CodeGenerator {

    private FileObject targetSource;

    SetSoapVersionActionGenerator(FileObject targetSource) {
        this.targetSource = targetSource;
    }
    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            CompilationController controller = context.lookup(CompilationController.class);
            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            if (controller != null) {
                try {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    FileObject targetSource = controller.getFileObject();
                    if (targetSource != null) {
                        AddOperationCookie addOperationCookie = WebServiceActionProvider.getAddOperationAction(targetSource);
                        if (addOperationCookie != null && isEnabledInEditor(context)) {
                            ret.add(new SetSoapVersionActionGenerator(targetSource));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return ret;
        }

        private boolean isEnabledInEditor(Lookup nodeLookup) {
            CompilationController controller = nodeLookup.lookup(CompilationController.class);
            if (controller != null) {
                TypeElement classEl = SourceUtils.getPublicTopLevelElement(controller);
                if (classEl != null) {
                    return isJaxWsImplementationClass(classEl, controller);
                }
            }
            return false;
        }

        private boolean isJaxWsImplementationClass(TypeElement classEl, CompilationController controller) {
            TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
            if (wsElement != null) {
                List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
                for (AnnotationMirror anMirror : annotations) {
                    if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    public String getDisplayName() {
        String name = "LBL_SetSoap12"; // NOI18N
        if (targetSource != null) {
            if (JaxWsUtils.isSoap12(targetSource)) {
                name = "LBL_SetSoap11"; // NOI18N
            }
        }
        return NbBundle.getMessage(SetSoapVersionActionGenerator.class, name);
    }

    public void invoke() {
        JaxWsUtils.setSOAP12Binding(targetSource, !JaxWsUtils.isSoap12(targetSource));
    }

}
