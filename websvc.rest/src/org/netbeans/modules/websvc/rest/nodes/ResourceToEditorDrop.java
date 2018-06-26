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

package org.netbeans.modules.websvc.rest.nodes;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;

/** Implementation of ActiveEditorDrop
 *
 * @author mkuchtiak
 */
public class ResourceToEditorDrop implements ActiveEditorDrop {
    
    RestServiceNode resourceNode;
    
    public ResourceToEditorDrop(RestServiceNode resourceNode) {
        this.resourceNode=resourceNode;
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        RestServiceDescription serviceDescription = resourceNode.getLookup().lookup(RestServiceDescription.class);
        if (serviceDescription != null &&
            mimeType!=null &&
            "text/x-java".equals(mimeType)) { //NOI18N
            
            try {
                FileObject targetFo = NbEditorUtilities.getFileObject(targetComponent.getDocument());
                if (targetFo != null) {
                    // Generate Jersey Client
                    ClientJavaSourceHelper.generateJerseyClient(resourceNode, targetFo, serviceDescription.getName()+"_JerseyClient");
                    // logging usage of action
                    Object[] params = new Object[2];
                    params[0] = LogUtils.WS_STACK_JAXRS;
                    params[1] = "DRAG & DROP REST RESOURCE"; // NOI18N
                    LogUtils.logWsAction(params);
                    
                    return true;
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
        return false;
    }   
}
