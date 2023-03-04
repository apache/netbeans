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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import javax.swing.Action;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class AddActionGroup extends EJBActionGroup {

    public String getName() {
        return NbBundle.getMessage(AddActionGroup.class, "LBL_AddActionGroup");
    }

    /** List of system actions to be displayed within this one's toolbar or submenu. */
    protected Action[] grouped() {
        return new Action[] {
            new AddBusinessMethodAction(NbBundle.getMessage(AddActionGroup.class, "LBL_BusinessMethodAction")),
            new AddCreateMethodAction(NbBundle.getMessage(AddActionGroup.class, "LBL_CreateMethodAction")),
            new AddFinderMethodAction(NbBundle.getMessage(AddActionGroup.class, "LBL_FinderMethodAction")),
            new AddHomeMethodAction(NbBundle.getMessage(AddActionGroup.class, "LBL_HomeMethodAction")),
            new AddSelectMethodAction(NbBundle.getMessage(AddActionGroup.class, "LBL_SelectMethodAction"))
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
