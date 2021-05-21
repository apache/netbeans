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
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** SoapClientEditorDrop
 *
 * @author Ayub Khan, Nam Nguyen
 */
public class SoapClientEditorDrop implements ActiveEditorDrop {

    private WsdlSaasMethod method;
    private FileObject targetFO;
    private RequestProcessor.Task generatorTask;

    public SoapClientEditorDrop(WsdlSaasMethod method) {
        this.method = method;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        if(SaasClientCodeGenerationManager.canAccept(method, targetComponent.getDocument()))
            return doHandleTransfer(targetComponent);
        return false;
    }
    
    private boolean doHandleTransfer(final JTextComponent targetComponent) {
        final Document targetDoc = targetComponent.getDocument();
        FileObject targetSource = NbEditorUtilities.getFileObject(targetComponent.getDocument());
        WSOperation op = method.getWsdlOperation();
        final String displayName = op.getName();
        
        targetFO = getTargetFile(targetComponent);

        if (targetFO == null) {
            return false;
        }
        
        final List<Exception> errors = new ArrayList<Exception>();
       
        final ProgressDialog dialog = new ProgressDialog(
                NbBundle.getMessage(SoapClientEditorDrop.class, "LBL_CodeGenProgress", 
                displayName));

        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    SaasClientCodeGenerator codegen =  (SaasClientCodeGenerator) 
                            SaasClientCodeGenerationManager.lookup(method, targetDoc);
                    codegen.init(method, targetDoc);
                    codegen.setDropLocation(targetComponent);
                
                    SoapClientSaasBean bean = (SoapClientSaasBean) codegen.getBean();
                    List<ParameterInfo> allParams = new ArrayList<ParameterInfo>(bean.getHeaderParameters());
                    if (bean.getInputParameters() != null) {
                        allParams.addAll(bean.getInputParameters());
                    }
                    boolean response = Util.showDialog(displayName, allParams, targetDoc);
                    if(response)
                        Util.doGenerateCode(codegen, dialog, errors);
                } catch (Exception ioe) {
                    errors.add(ioe);
                } finally {
                    dialog.close();
                }
            }
        });

        generatorTask.schedule(50);

        dialog.open();

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
        EditorCookie ec = d.getCookie(EditorCookie.class);
        if (ec == null || ec.getOpenedPanes() == null) {
            return null;
        }
        return d.getPrimaryFile();
    }
    
}
