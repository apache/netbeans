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
package org.netbeans.modules.refactoring.java.ui;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Jan Pokorsky
 */
@ActionID(id = "org.netbeans.modules.refactoring.java.api.ui.ExtractSuperclassAction", category = "Refactoring")
@ActionRegistration(displayName = "#LBL_ExtractSC_Action", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/x-java/RefactoringActions", name = "ExtractSuperclassAction", position = 800),
    @ActionReference(path = "Shortcuts", name = "CS-Y S")
})
public final class ExtractSuperclassAction extends JavaRefactoringGlobalAction {

    public ExtractSuperclassAction() {
        super(NbBundle.getMessage(ExtractInterfaceAction.class, "LBL_ExtractSC_Action"), null); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public final void performAction(Lookup context) {
        JavaActionsImplementationFactory.doExtractSuperclass(context);
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
        return JavaActionsImplementationFactory.canExtractSuperclass(context);
    }
}
