/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
