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
package org.netbeans.modules.refactoring.spi.impl;

import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Jan Becicka
 */
@ActionID(id = "org.netbeans.modules.refactoring.api.ui.RenameAction", category = "Refactoring")
@ActionRegistration(displayName = "#LBL_RenameAction")
@ActionReferences({
    @ActionReference(path = "Menu/Refactoring" , name = "RenameAction", position = 100),
    @ActionReference(path = "Shortcuts", name = "C-R")
})

public class RenameAction extends RefactoringGlobalAction {

    /**
     * Creates a new instance of RenameAction
     */
    public RenameAction() {
        super(NbBundle.getMessage(RenameAction.class, "LBL_RenameAction"), null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    public final void performAction(Lookup context) {
        ActionsImplementationFactory.doRename(context);
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
    protected boolean enable(Lookup context) {
        return true;
    }

    @Override
    protected boolean applicable(Lookup context) {
        return ActionsImplementationFactory.canRename(context);
    }
}
