/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.classview.actions;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.classview.ClassViewTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.*;

/**
 * Shows/Hides class view pane
 */
@ActionID(id = "org.netbeans.modules.cnd.classview.ClassViewAction", category = "View")
@ActionRegistration(lazy = true, displayName = "#CTL_ClassViewAction", iconBase=ClassViewTopComponent.ICON_PATH)
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "D-9"),
    @ActionReference(path = "Menu/Window", position = 300)})
public class ShowHideClassViewAction extends CallableSystemAction {

    public ShowHideClassViewAction() {
        putValue(NAME, NbBundle.getMessage(ShowHideClassViewAction.class, "CTL_ClassViewAction")); // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ShowHideClassViewAction.class, "HINT_ClassViewAction")); // NOI18N
    }

    @Override
    public String getName() {
        return (String) getValue(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }

    @Override
    public void performAction() {
        TopComponent tc = ClassViewTopComponent.findDefault();
        if (!tc.isOpened()) {
            tc.open();
            Preferences ps = NbPreferences.forModule(ClassViewTopComponent.class);
            ps.putBoolean(ClassViewTopComponent.OPENED_PREFERENCE, true); // NOI18N
        }
        tc.requestActive();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected String iconResource() {
        return ClassViewTopComponent.ICON_PATH;
    }
}
