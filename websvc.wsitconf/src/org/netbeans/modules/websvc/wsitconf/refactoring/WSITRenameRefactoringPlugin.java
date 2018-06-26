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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac, Martin Matula
 */
public final class WSITRenameRefactoringPlugin extends WSITRefactoringPlugin<RenameRefactoring> {
    /**
     * Creates a new instance of WSITRenameRefactoringPlugin
     */
    public WSITRenameRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
    }

    protected RefactoringElementImplementation createClassRE(WSDLModel model) {
        return new ClassRE(refactoring.getNewName(), model);
    }

    protected RefactoringElementImplementation createMethodRE(String methodName, WSDLModel model) {
        return new MethodRE(methodName, refactoring.getNewName(), model);
    }

   /**
     * Rename refactoring element for wsit-*.xml
     */
    private static class ClassRE extends AbstractRefactoringElement {
        private final String oldConfigName;
        private final String newConfigName;
        private final String extension;
                
        /**
         * Creates a new instance of WSITXmlClassRenameRefactoringElement
         * 
         * @param oldName the fully qualified old name of the implementation class
         * @param newName the fully qualified new name of the implementation class
         */
        public ClassRE(String newName, WSDLModel model) {
            super(model);
            this.oldConfigName = getParentFile().getName();
            this.newConfigName = oldConfigName.substring(0, oldConfigName.lastIndexOf('.') + 1) + newName;
            this.extension = getParentFile().getExt();
        }
        
        public void performChange() {
            FileLock lock = null;
            FileObject parentFile = getParentFile();
            try {
                lock = parentFile.lock();
                parentFile.rename(lock, newConfigName, extension);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (lock != null) lock.releaseLock();
            }
        }

        public void undoChange() {
            FileLock lock = null;
            FileObject parentFile = getParentFile();
            try {
                lock = parentFile.lock();
                parentFile.rename(lock, oldConfigName, extension);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (lock != null) lock.releaseLock();
            }
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {oldConfigName, newConfigName};
            return MessageFormat.format(NbBundle.getMessage(ClassRE.class, "TXT_WsitXmlClassRename"), args);
        }
    }
    
   /**
     * Rename refactoring element for wsit-*.xml
     */
    private static class MethodRE extends AbstractRefactoringElement {
        private final String oldName;
        private final String newName;
        
        public MethodRE(String oldName, String newName, WSDLModel model) {
            super(model);
            this.oldName = oldName;
            this.newName = newName;
        }
        
        public void performChange() {
            Definitions d = model.getDefinitions();
            Binding b = (Binding) d.getBindings().toArray()[0];
            Collection<BindingOperation> bOperations = b.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            
            model.startTransaction();
            try {
                for (BindingOperation bOperation : bOperations) {
                    if (oldName.equals(bOperation.getName())) {
                        bOperation.setName(newName);
                    }
                }

                for (Operation o : operations) {
                    if (oldName.equals(o.getName())) {
                        o.setName(newName);
                        Input i = o.getInput();
                        if (i != null) {
                            QName qname = i.getMessage().getQName();
                            Message msg = model.findComponentByName(qname, Message.class);
                            String oMsgName = msg.getName();
                            if (oMsgName != null) {
                                String nMsgName = oMsgName.replaceAll(oldName, newName);
                                msg.setName(nMsgName);
                            }
                            i.setMessage(i.createReferenceTo(msg, Message.class));
                        }
                        Output out = o.getOutput();
                        if (out != null) {
                            QName qname = out.getMessage().getQName();
                            Message msg = model.findComponentByName(qname, Message.class);
                            String oMsgName = msg.getName();
                            if (oMsgName != null) {
                                String nMsgName = oMsgName.replaceAll(oldName, newName);
                                msg.setName(nMsgName);
                            }
                            out.setMessage(out.createReferenceTo(msg, Message.class));
                        }
                    }
                }
            } finally {
                try {
                    model.endTransaction();
                }
                catch(IllegalStateException e ){
                    LOGGER.log(Level.WARNING, null, e);
                }
            }
        }

        public void undoChange() {
            Definitions d = model.getDefinitions();
            Binding b = (Binding) d.getBindings().toArray()[0];
            Collection<BindingOperation> bOperations = b.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            
            model.startTransaction();
            try {
                for (BindingOperation bOperation : bOperations) {
                    if (newName.equals(bOperation.getName())) {
                        bOperation.setName(oldName);
                    }
                }
                for (Operation o : operations) {
                    if (newName.equals(o.getName())) {
                        o.setName(oldName);
                        Input i = o.getInput();
                        if (i != null) {
                            QName qname = i.getMessage().getQName();
                            Message msg = model.findComponentByName(qname, Message.class);
                            String oMsgName = msg.getName();
                            if (oMsgName != null) {
                                String nMsgName = oMsgName.replaceAll(newName, oldName);
                                msg.setName(nMsgName);
                            }
                            i.setMessage(i.createReferenceTo(msg, Message.class));
                        }
                        Output out = o.getOutput();
                        if (out != null) {
                            QName qname = out.getMessage().getQName();
                            Message msg = model.findComponentByName(qname, Message.class);
                            String oMsgName = msg.getName();
                            if (oMsgName != null) {
                                String nMsgName = oMsgName.replaceAll(newName, oldName);
                                msg.setName(nMsgName);
                            }
                            out.setMessage(out.createReferenceTo(msg, Message.class));
                        }
                    }
                }
            } finally {
                try {
                    model.endTransaction();
                }
                catch(IllegalStateException e ){
                    LOGGER.log(Level.WARNING, null, e);
                }
            }
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(MethodRE.class, "TXT_WsitXmlMethodRename"), args);
        }
    }
}
