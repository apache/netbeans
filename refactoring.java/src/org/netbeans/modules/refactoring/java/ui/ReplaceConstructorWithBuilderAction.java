/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.modules.refactoring.java.ui.ContextAnalyzer;
import org.netbeans.modules.refactoring.java.ui.JavaRefactoringGlobalAction;
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
@ActionID(id = "org.netbeans.modules.java.hints.jackpot.refactoring.ReplaceConstructorWithBuilderAction", category = "Refactoring")
@ActionRegistration(displayName = "#LBL_ReplaceConstructorWithBuilderAction")
@ActionReferences({
    @ActionReference(path = "Editors/text/x-java/RefactoringActions" , name = "ReplaceConstructorWithBuilderAction", position = 1825),
    @ActionReference(path = "Shortcuts", name = "OCS-B")
})
public final class ReplaceConstructorWithBuilderAction extends JavaRefactoringGlobalAction {

    public ReplaceConstructorWithBuilderAction() {
        super(NbBundle.getMessage(ReplaceConstructorWithFactoryAction.class, "LBL_ReplaceConstructorWithBuilderAction"), null); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Lookup context) {
        return ContextAnalyzer.canRefactorSingle(context, true, false);
    }

    @Override
    public void performAction(Lookup context) {
        Runnable task = ContextAnalyzer.createTask(context, ReplaceConstructorWithBuilderUI.factory());
        UIUtilities.runWhenScanFinished(task, getName());
    }
}
