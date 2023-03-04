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
