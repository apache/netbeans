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
