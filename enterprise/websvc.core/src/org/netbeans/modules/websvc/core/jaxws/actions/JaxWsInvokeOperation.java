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

package org.netbeans.modules.websvc.core.jaxws.actions;

import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.webservices.ui.panels.ClientExplorerPanel;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsInvokeOperation implements InvokeOperationCookie {

    private FileObject targetSource;
    /** Creates a new instance of JaxWsAddOperation.
     * @param targetSource filae object
     */
    public JaxWsInvokeOperation(FileObject targetSource) {
        this.targetSource = targetSource;
    }

    @Override
    public void invokeOperation(Lookup sourceNodeLookup, JTextComponent targetComponent) {
        try {
            DataObject dObj = DataObject.find(targetSource);
            JaxWsCodeGenerator.insertMethodCall(getTargetSourceType(dObj),
                    dObj,
                    sourceNodeLookup);
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public InvokeOperationCookie.ClientSelectionPanel getDialogDescriptorPanel() {
        return new ClientExplorerPanel(targetSource);
    }

    private InvokeOperationCookie.TargetSourceType getTargetSourceType(DataObject dObj) {
        EditorCookie cookie = dObj.getCookie(EditorCookie.class);
        if (cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TargetSourceType.JSP;
        } else if (cookie!=null && "text/x-java".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TargetSourceType.JAVA;
        }
        return InvokeOperationCookie.TargetSourceType.UNKNOWN;
    }

}
