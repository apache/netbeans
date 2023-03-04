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

package org.netbeans.modules.websvc.core.jaxws.nodes;

import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;

/** Implementation of ActiveEditorDrop
 *
 * @author mkuchtiak
 */
public class OperationEditorDrop implements ActiveEditorDrop {
    
    OperationNode operationNode;
    
    public OperationEditorDrop(OperationNode operationNode) {
        this.operationNode=operationNode;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Object mimeType = targetComponent.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeType!=null && ("text/x-java".equals(mimeType) || "text/x-jsp".equals(mimeType) )) { //NOI18N
            
            try {
                Node clientNode = operationNode.getParentNode().getParentNode().getParentNode();
                FileObject srcRoot = clientNode.getLookup().lookup(FileObject.class);
                Project clientProject = FileOwnerQuery.getOwner(srcRoot);
                FileObject targetFo = NbEditorUtilities.getFileObject(targetComponent.getDocument());
                if (JaxWsUtils.addProjectReference(clientProject, targetFo)) {
                    JaxWsCodeGenerator.insertMethod(targetComponent.getDocument(), targetComponent.getCaret().getDot(), operationNode);

                    // logging usage of action
                    Object[] params = new Object[2];
                    params[0] = LogUtils.WS_STACK_JAXWS;
                    params[1] = "DRAG & DROP WS OPERATION"; // NOI18N
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
