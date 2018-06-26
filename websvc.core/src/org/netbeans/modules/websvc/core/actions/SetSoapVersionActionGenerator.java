/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
