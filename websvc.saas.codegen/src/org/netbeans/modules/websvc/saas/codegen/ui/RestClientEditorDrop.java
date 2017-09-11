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
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** RestClientEditorDrop
 *
 * @author Ayub Khan, Nam Nguyen
 */
public class RestClientEditorDrop implements ActiveEditorDrop {

    private WadlSaasMethod method;
    private FileObject targetFO;
    private RequestProcessor.Task generatorTask;

    public RestClientEditorDrop(WadlSaasMethod method) {
        this.method = method;
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        if(SaasClientCodeGenerationManager.canAccept(method, targetComponent.getDocument()))
            return doHandleTransfer(targetComponent);
        return false;
    }
    
    private boolean doHandleTransfer(final JTextComponent targetComponent) {
        final Document targetDoc = targetComponent.getDocument();
        final FileObject targetSource = NbEditorUtilities.getFileObject(targetDoc);
        Project targetProject = FileOwnerQuery.getOwner(targetSource);
        Method m = method.getWadlMethod();
        if(m == null)
            Exceptions.printStackTrace(new IOException("Wadl method not found"));
        final String displayName = m.getName();
        
        targetFO = getTargetFile(targetDoc);

        if (targetFO == null) {
            return false;
        }

        final List<Exception> errors = new ArrayList<Exception>();
       
        final ProgressDialog dialog = new ProgressDialog(
                NbBundle.getMessage(RestClientEditorDrop.class, "LBL_CodeGenProgress", 
                displayName));

        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    SaasClientCodeGenerator codegen = (SaasClientCodeGenerator) 
                            SaasClientCodeGenerationManager.lookup(method, targetDoc);
                    codegen.init(method, targetDoc);
                    codegen.setDropLocation(targetComponent);
                
                    RestClientSaasBean bean = (RestClientSaasBean) codegen.getBean();
                    List<ParameterInfo> allParams = bean.filterParametersByAuth(
                            bean.filterParameters(new ParamFilter[]{ParamFilter.FIXED}));
                    boolean response = Util.showDialog(displayName, allParams, targetDoc);
                    if(response)
                        Util.doGenerateCode(codegen, dialog, errors);
                } catch (Exception ioe) {
                    if(Constants.UNSUPPORTED_DROP.equals(ioe.getMessage())) {
                        Util.showUnsupportedDropMessage(new Object[] {
                            targetSource.getNameExt(), "Java Client"});
                        return;
                    }
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
    
    public static FileObject getTargetFile(Document doc) {
        if (doc == null) {
            return null;
        }
        DataObject d = NbEditorUtilities.getDataObject(doc);
        if (d == null) {
            return null;
        }
        EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
        if (ec == null || ec.getOpenedPanes() == null) {
            return null;
        }
        return d.getPrimaryFile();
    }
    
}
