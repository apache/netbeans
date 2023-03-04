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
package org.netbeans.jellytools.modules.freeform.nodes;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.freeform.actions.RedeployFreeformAction;
import org.netbeans.jellytools.modules.freeform.actions.RunFreeformAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Web Freeform Project root node class. It represents root node of a project
 * in Projects view.
 * @author Martin.Schovanek@sun.com
 */
public class FreeformProjectNode extends ProjectRootNode {
    
    static final RunFreeformAction runFreeformAction = new RunFreeformAction();
    static final RedeployFreeformAction redeployFreeformAction =
            new RedeployFreeformAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        super.verifyPopup();
        verifyPopup(new Action[]{
            runFreeformAction,
            redeployFreeformAction
        });
    }
    
    /** Creates new FreeformProjectNode instance.
     * @param projectName display name of the project
     */
    public FreeformProjectNode(String projectName) {
        super(ProjectsTabOperator.invoke().tree(), projectName);
    }
    
    /** run project */    
    public void run() {
        runFreeformAction.perform(this);
    }
    
    /** build project */    
    public void redeploy() {
        redeployFreeformAction.perform(this);
    }
    
    /** perform a custom action */
    public void customAction(String action) {
        new ActionNoBlock(null, action).perform(this);
    }
}
