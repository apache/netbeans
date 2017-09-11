/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.callhierarchy;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.ui.JavaRefactoringGlobalAction;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action which shows CallHierarchy component.
 *
 * @author Jan Pokorsky
 */
@ActionID(id = "org.netbeans.modules.refactoring.java.callhierarchy.CallHierarchyAction", category = "Window")
@ActionRegistration(displayName = "#CTL_CallHierarchyAction", lazy = false)
@ActionReference(path = "Editors/text/x-java/Popup" , name = "CallHierarchyAction", position = 1420)
public final class CallHierarchyAction extends JavaRefactoringGlobalAction {

    public CallHierarchyAction() {
        super(NbBundle.getMessage(CallHierarchyAction.class, "CTL_CallHierarchyAction"),ImageUtilities.loadImageIcon(CallHierarchyTopComponent.ICON_PATH, true));
    }

    @Override
    public void performAction(final Lookup context) {
        CallHierarchyTopComponent win = CallHierarchyTopComponent.findInstance();
        win.open();
        win.requestActive();
        win.reset();
        win.setModel(CallHierarchyModel.create(context, win.getScopes(), win.getHierarchyType()));
    }

    @Override
    protected boolean enable(Lookup context) {
        return new RefactoringActionsProvider().canFindUsages(context);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
