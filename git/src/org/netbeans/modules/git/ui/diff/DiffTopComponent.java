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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.diff;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class DiffTopComponent extends TopComponent {

    private final MultiDiffPanelController controller;

    DiffTopComponent (MultiDiffPanelController controller) {
        setLayout(new BorderLayout());
        this.controller = controller;
        JPanel panel = controller.getPanel();
        panel.putClientProperty(TopComponent.class, this);
        add(panel, BorderLayout.CENTER);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
    }

    @Override
    public boolean canClose () {
        return controller.canClose();
    }

    @Override
    public UndoRedo getUndoRedo () {
        return controller.getUndoRedo();
    }

    @Override
    public int getPersistenceType (){
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed () {
        controller.componentClosed();
        super.componentClosed();
    }

    @Override
    protected String preferredID () {
        return "PERSISTENCE_NEVER-DiffTopComponent";    // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentActivated () {
        super.componentActivated();
        controller.setFocused(true);
    }

    @Override
    protected void componentDeactivated () {
        super.componentDeactivated();
        controller.setFocused(false);
    }
    
}
