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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac, Martin Matula
 */
public class WSITSafeDeleteRefactoringPlugin extends WSITRefactoringPlugin<SafeDeleteRefactoring> {
    public WSITSafeDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        super(refactoring);
    }

    protected RefactoringElementImplementation createMethodRE(String methodName, WSDLModel model) {
        return new MethodRE(methodName, model);
    }

    protected RefactoringElementImplementation createClassRE(WSDLModel model) {
        return new ClassRE(model);
    }
    
    private static class ClassRE extends AbstractRefactoringElement {
        private BackupFacility.Handle id;

        public ClassRE(WSDLModel model) {
            super(model);
        }
        
        public void performChange() {
            FileObject parentFile = getParentFile();
            try {
                id = BackupFacility.getDefault().backup(parentFile);
                parentFile.delete();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        public void undoChange() {
            try {
                id.restore();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt()};
            return MessageFormat.format(NbBundle.getMessage(ClassRE.class, "TXT_WsitXmlClassSafeDelete"), args);
        }
    }
    
   /**
     * Rename refactoring element for wsit-*.xml
     */
    private static class MethodRE extends AbstractRefactoringElement {
        private final String methodName;
        
        public MethodRE(String methodName, WSDLModel model) {
            super(model);
            this.methodName = methodName;
        }
        
        public void performChange() {
            Definitions d = model.getDefinitions();
            Binding b = (Binding) d.getBindings().toArray()[0];
            Collection<Message> messages = d.getMessages();
            Collection<BindingOperation> bOperations = b.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            model.startTransaction();

            for (BindingOperation bOperation : bOperations) {
                if (methodName.equals(bOperation.getName())) {
                    b.removeBindingOperation(bOperation);
                }
            }
            
            for (Operation o : operations) {
                if (methodName.equals(o.getName())) {
                    portType.removeOperation(o);
                }
            }

            for (Message m : messages) {
                if (methodName.equals(m.getName()) || (methodName + "Response").equals(m.getName())) {
                    d.removeMessage(m);
                }
            }

            model.endTransaction();
        }

        public void undoChange() { 
            // [TODO] implement me
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {methodName, getParentFile().getNameExt()};
            return MessageFormat.format(NbBundle.getMessage(MethodRE.class, "TXT_WsitXmlMethodSafeDelete"), args);
        }
    }
    
}
