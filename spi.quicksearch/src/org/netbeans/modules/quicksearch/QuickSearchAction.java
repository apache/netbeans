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
package org.netbeans.modules.quicksearch;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * QuickSearch Action provides toolbar presenter
 * @author  Jan Becicka
 */
@ActionID(id = "org.netbeans.modules.quicksearch.QuickSearchAction", category = "Edit")
@ActionRegistration(displayName = "#CTL_QuickSearchAction", lazy=false)
@ActionReference(name = "D-I", path = "Shortcuts")
public final class QuickSearchAction extends CallableSystemAction {

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID());
    AbstractQuickSearchComboBar comboBar;
   
    public void performAction() {
        if (comboBar == null) {
            comboBar = isAqua
                        ? new AquaQuickSearchComboBar((KeyStroke) this.getValue(Action.ACCELERATOR_KEY))
                        : new QuickSearchComboBar((KeyStroke) this.getValue(Action.ACCELERATOR_KEY));
        }
        comboBar.displayer.explicitlyInvoked();
        if (comboBar.getCommand().isFocusOwner()) {
            // repetitive action invocation, reset search to all categories
            comboBar.evaluate(null);
        } else {
            comboBar.requestFocus();
        }
    }

    public String getName() {
        return NbBundle.getMessage(QuickSearchAction.class, "CTL_QuickSearchAction");
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/jumpto/resources/edit_parameters.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public java.awt.Component getToolbarPresenter() {
        if (comboBar == null) {
            comboBar = isAqua
                        ? new AquaQuickSearchComboBar((KeyStroke) this.getValue(Action.ACCELERATOR_KEY))
                        : new QuickSearchComboBar((KeyStroke) this.getValue(Action.ACCELERATOR_KEY));
        }
        return comboBar;
    }

}
