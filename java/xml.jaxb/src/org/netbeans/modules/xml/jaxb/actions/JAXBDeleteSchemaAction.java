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

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.NodeAction;


/**
 * @author gmpatil
 * @author lgao
 */
public class JAXBDeleteSchemaAction extends NodeAction {
    
    public JAXBDeleteSchemaAction() {
    }

    public void performAction(Node[] nodes) {
        JAXBWizardSchemaNode schemaNode = 
                nodes[0].getLookup().lookup(JAXBWizardSchemaNode.class);
        if (schemaNode != null){
            Schema schema = schemaNode.getSchema();
            final Project prj = schemaNode.getProject();
            ProjectHelper.deleteSchemaFromModel(prj, schema);
            ProjectHelper.cleanupLocalSchemaDir(prj, schema);
            ProjectHelper.cleanCompileXSDs(prj, false, new TaskListener() {
                
                @Override
                public void taskFinished( Task arg0 ) {
                    ProjectHelper.checkAndDeregisterScript(prj);
                }
            });
        }        
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_DeleteSchema");//NOI18N
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
        
    @Override
    protected boolean enable(Node[] node) {
        return true;
    }
}
