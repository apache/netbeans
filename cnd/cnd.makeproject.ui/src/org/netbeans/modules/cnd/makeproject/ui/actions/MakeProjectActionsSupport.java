/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.util.NbBundle;

/**
 *
 */
public class MakeProjectActionsSupport {
    private static final ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);

    public static Action buildAction() {
        return ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null); // NOI18N
    }
    
    public static Action batchBuildAction() {
        return ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null); // NOI18N
    }
    
    public static Action buildPackageAction() {
        return ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, bundle.getString("LBL_BuildPackagesAction_Name"), null); // NOI18N        
    }
    
    public static Action preBuildAction() {
        return ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_PRE_BUILD, bundle.getString("LBL_PreBuildAction_Name"), null); // NOI18N        
    }
    
    public static Action runAction() {
        return ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null); // NOI18N
    }
    
    public static Action debugAction() {
        return ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null);//NOI18N
    }
    
    public static Action stepIntoAction() {
        return ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString("LBL_DebugAction_Step_Name"), null);//NOI18N
    }    
    
    public static Action testAction() {
        return ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null);//NOI18N
    }
    
}
