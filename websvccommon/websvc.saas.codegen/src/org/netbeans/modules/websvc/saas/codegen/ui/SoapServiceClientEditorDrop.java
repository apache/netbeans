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
package org.netbeans.modules.websvc.saas.codegen.ui;

import org.netbeans.modules.websvc.saas.codegen.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** SoapServiceClientEditorDrop
 *
 * @author Ayub Khan, Nam Nguyen
 */
public class SoapServiceClientEditorDrop implements ActiveEditorDrop {
    private final WsdlSaas service;
    private FileObject targetFO;
    private RequestProcessor.Task generatorTask;
    private List<WsdlSaasMethod> methods;

    public SoapServiceClientEditorDrop(WsdlSaas service) {
        this.service = service;
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        doHandleTransfer(targetComponent);
        return true; //TODO allow correct nodes only

    }

    private List<WsdlSaasMethod> getMethods() {
        if (methods == null) {
            methods = new ArrayList<WsdlSaasMethod>();
            List<WsdlSaasPort> ports = service.getPorts();
            for (WsdlSaasPort port : ports) {
                methods.addAll(port.getWsdlMethods());
            }
        }
        return methods;
    }

    private boolean doHandleTransfer(final JTextComponent targetComponent) {
        final Document targetDoc = targetComponent.getDocument();

        targetFO = getTargetFile(targetComponent);

        if (targetFO == null) {
            return false;
        }

        final List<Exception> errors = new ArrayList<Exception>();

        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            ProgressDialog[] dialog = new ProgressDialog[1];
            @Override
            public void run() {
                for (final WsdlSaasMethod method : getMethods()) {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable(){
                            @Override
                            public void run(){
                                dialog[0] = new ProgressDialog(
                                    NbBundle.getMessage(SoapServiceClientEditorDrop.class, "LBL_CodeGenProgress", // NOI18N
                                    method.getName()));
                            }
                        });
                    } catch (InterruptedException iex) {
                    } catch (InvocationTargetException itex) {
                    }
                    
                    try {
                        String displayName = method.getName();
                        
                        SaasClientCodeGenerator codegen = (SaasClientCodeGenerator) SaasClientCodeGenerationManager.lookup(method, targetDoc);
                        codegen.init(method, targetDoc);
                        codegen.setDropLocation(targetComponent);

                        SoapClientSaasBean bean = (SoapClientSaasBean) codegen.getBean();
                        List<ParameterInfo> allParams = new ArrayList<ParameterInfo>(bean.getHeaderParameters());
                        if (bean.getInputParameters() != null) {
                            allParams.addAll(bean.getInputParameters());
                        }
                        if (!allParams.isEmpty()) {
                            boolean showParamTypes = Util.isJava(targetDoc) || Util.isJsp(targetDoc);
                            CodeSetupPanel panel = new CodeSetupPanel(allParams, showParamTypes);

                            DialogDescriptor desc = new DialogDescriptor(panel,
                                    NbBundle.getMessage(SoapServiceClientEditorDrop.class,
                                    "LBL_CustomizeSaasService", displayName));
                            Object response = DialogDisplayer.getDefault().notify(desc);

                            if (response.equals(NotifyDescriptor.CANCEL_OPTION)) {
                                // cancel
                                return;
                            }
                        }

                        try {
                            codegen.initProgressReporting(dialog[0].getProgressHandle());
                            codegen.generate();
                        } catch (IOException ex) {
                            if (!ex.getMessage().equals(Util.SCANNING_IN_PROGRESS)) {
                                errors.add(ex);
                            }

                        }
                    } catch (Exception ioe) {
                        errors.add(ioe);
                    } finally {
                        dialog[0].close();
                    }
                }
                dialog[0].open();
            }
        });

        generatorTask.schedule(50);



        if (errors.size() > 0) {
            Exceptions.printStackTrace(errors.get(0));
            return false;
        }
        return true;
    }

    public static FileObject getTargetFile(JTextComponent targetComponent) {
        if (targetComponent == null) {
            return null;
        }
        DataObject d = NbEditorUtilities.getDataObject(targetComponent.getDocument());
        if (d == null) {
            return null;
        }
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        if (ec == null || ec.getOpenedPanes() == null) {
            return null;
        }
        return d.getPrimaryFile();
    }
}
