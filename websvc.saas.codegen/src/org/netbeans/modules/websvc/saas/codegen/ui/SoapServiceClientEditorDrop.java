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
        EditorCookie ec = (EditorCookie) d.getLookup().lookup(EditorCookie.class);
        if (ec == null || ec.getOpenedPanes() == null) {
            return null;
        }
        return d.getPrimaryFile();
    }
}
