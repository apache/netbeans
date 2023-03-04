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
package org.netbeans.modules.j2ee.persistence.jpqleditor.ui;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.jpqleditor.JPQLEditorController;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which shows JPQLEditor component.
 * 
 */
public class JPQLEditorAction extends NodeAction {
    
    public JPQLEditorAction() {
        super();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        new JPQLEditorController().init(activatedNodes);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
                if (data != null) {
                    FileObject pXml = data.getPrimaryFile();
                    Project project = pXml != null ? FileOwnerQuery.getOwner(pXml) : null;
                    PersistenceEnvironment pe = project!=null ? project.getLookup().lookup(PersistenceEnvironment.class) : null;
                    if( pe != null ) {
                        return true;//!Util.isSupportedJavaEEVersion(project);//so far support only non-container managed projects
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(JPQLEditorAction.class, "CTL_JPQLEditorAction");
    }
}
