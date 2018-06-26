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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class CallWsOperationGenerator implements CodeGenerator {
    private FileObject targetSource;
    private JTextComponent targetComponent;
    private InvokeOperationCookie invokeOperationCookie;

    CallWsOperationGenerator(FileObject targetSource, JTextComponent targetComponent, InvokeOperationCookie invokeOperationCookie) {
        this.targetSource = targetSource;
        this.targetComponent = targetComponent;
        this.invokeOperationCookie = invokeOperationCookie;
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
                        JTextComponent targetComponent = context.lookup(JTextComponent.class);
                        InvokeOperationCookie invokeOperationCookie = WebServiceActionProvider.getInvokeOperationAction(targetSource);
                        if (invokeOperationCookie != null) {
                            ret.add(new CallWsOperationGenerator(targetSource, targetComponent, invokeOperationCookie));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return ret;
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CallWsOperationGenerator.class, "LBL_CallWebServiceOperation");
    }

    public void invoke() {
        InvokeOperationCookie.ClientSelectionPanel innerPanel = invokeOperationCookie.getDialogDescriptorPanel();
        final DialogDescriptor descriptor = new DialogDescriptor(innerPanel,
                NbBundle.getMessage(CallWsOperationGenerator.class, "TTL_SelectOperation"));
        descriptor.setValid(false);
        innerPanel.addPropertyChangeListener(
                InvokeOperationCookie.ClientSelectionPanel.PROPERTY_SELECTION_VALID,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        descriptor.setValid(((Boolean)evt.getNewValue()));
                    }

                });
        DialogDisplayer.getDefault().notify(descriptor);
        if (DialogDescriptor.OK_OPTION.equals(descriptor.getValue())) {
            Lookup selectedClient = innerPanel.getSelectedClient();
            invokeOperationCookie.invokeOperation(selectedClient, targetComponent);

            // logging usage of action
            Object[] params = new Object[2];
            String cookieClassName = invokeOperationCookie.getClass().getName();
            if (cookieClassName.contains("jaxrpc")) { // NOI18N
                params[0] = LogUtils.WS_STACK_JAXRPC;
            } else {
                params[0] = LogUtils.WS_STACK_JAXWS;
            }
            params[1] = "CALL WS OPERATION"; // NOI18N
            LogUtils.logWsAction(params);
        }
    }
}
