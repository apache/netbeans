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
package org.netbeans.modules.cnd.refactoring.actions;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Activates change function signature refactoring. It is possible to do it
 * on methods and constructors. (based on org.netbeans.modules.refactoring.java.ui.ChangeParametersAction)
 *
 */
public class ChangeParametersAction extends CsmRefactoringGlobalAction {

    /** Creates a new instance of ChangeParametersAction
     */
    public ChangeParametersAction() {
        super(RefactoringKind.CHANGE_FUNCTION_PARAMETERS.getKey(), null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(NAME, RefactoringKind.CHANGE_FUNCTION_PARAMETERS.getKey());
        String displayText = NbBundle.getMessage(ChangeParametersAction.class, "LBL_ChangeMethodSignatureAction"); // NOI18N
        putValue(SHORT_DESCRIPTION,displayText);
        putValue(POPUP_TEXT,displayText);
        putValue(MENU_TEXT,displayText);
    }
    
    @Override
    public final void performAction(Lookup context) {
        CsmActionsImplementationFactory.doChangeParameters(context);
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean applicable(Lookup context) {
        return CsmActionsImplementationFactory.canChangeParameters(context);
    }
}
