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

package org.netbeans.modules.xml.jaxb.actions;

import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author lgao
 */
public class JAXBWizardOpenXSDIntoEditorAction extends NodeAction {
    
    public JAXBWizardOpenXSDIntoEditorAction() {
    }

    protected void performAction(Node[] nodes) {
        Node node = nodes[ 0 ];
        
        FileObject fo = node.getLookup().lookup(FileObject.class );
        try {
        if ( fo != null ) {
            DataObject dataObject = DataObject.find( fo );
            if ( dataObject != null ) {
                EditCookie ec = dataObject.getCookie(EditCookie.class );
                if ( ec != null ) {
                    ec.edit();
                }
            }
        }
        } catch ( DataObjectNotFoundException donfe ) {
            donfe.printStackTrace();
        }
    }

    public String getName() {
        return NbBundle.getMessage(this.getClass(), 
                                    "LBL_OpenSchemaFile"); // No I18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}
