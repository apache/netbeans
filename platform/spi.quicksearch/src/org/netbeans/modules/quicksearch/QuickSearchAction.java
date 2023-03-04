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
