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
package org.netbeans.modules.java.lsp.server.refactoring;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Refactoring",
        id = "org.netbeans.modules.java.lsp.server.refactoring.ChangeMethodParametersRefactoringAction"
)
@ActionRegistration(
        iconBase = "org/netbeans/modules/java/lsp/server/refactoring/newHTML.png",
        displayName = "#CTL_ChangeMethodParametersRefactoringAction"
)
@ActionReference(path = "Menu/Refactoring", position = 1120)
@Messages("CTL_ChangeMethodParametersRefactoringAction=Change Method Parameters Refactoring")
public final class ChangeMethodParametersRefactoringAction implements ActionListener {
    private final FileObject file;

    public ChangeMethodParametersRefactoringAction(FileObject file) {
        this.file = file;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        JavaSource js = JavaSource.forFileObject(file);
        if (js != null) {
            try {
                js.runUserActionTask(ci -> {
                    ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ExecutableElement method = null;
                    METHOD: for (Element type : ci.getTopLevelElements()) {
                        for (Element e : type.getEnclosedElements()) {
                            System.err.println("e: " + e);
                            if (e.getKind() == ElementKind.METHOD) {
                                method = (ExecutableElement) e;
                                break METHOD;
                            }
                        }
                    }
                    if (method != null) {
                        ElementHandle<ExecutableElement> handle = ElementHandle.create(method);
                        Pages.showChangeMethodParametersUI(ci, null, file, handle, method);
                    }
                }, true);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
