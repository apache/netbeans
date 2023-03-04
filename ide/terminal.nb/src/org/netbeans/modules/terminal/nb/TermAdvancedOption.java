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
package org.netbeans.modules.terminal.nb;

import java.util.prefs.Preferences;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

import org.netbeans.spi.options.OptionsPanelController;

import org.netbeans.lib.terminalemulator.support.TermOptions;

/**
 * Sets up an options category under Miscellaneous.
 */

@OptionsPanelController.SubRegistration(
id = "TermAdvancedOption", // NOI18N
displayName = "#CTL_Term_options", // NOI18N
keywords="#KW_TerminalOptions", // NOI18N
keywordsCategory="Advanced/TermAdvancedOption"// NOI18N
//tooltip="#CTL_Term_options" // NOI18N
)
@OptionsPanelController.Keywords(keywords={"#KW_TerminalOptions2"}, location=OptionsDisplayer.ADVANCED, tabTitle="#CTL_Term_options")
public final class TermAdvancedOption extends OptionsPanelController {
    private TermOptions termOptions;
    private TermOptions clonedTermOptions;
    private TermOptionsPanel panel;

    /**
     * Preferences in which we store term settings.
     */
    private static final Preferences prefs =
	NbPreferences.forModule(TermAdvancedOption.class);

    private void reset() {
	termOptions = TermOptions.getDefault(prefs);
	clonedTermOptions = termOptions.makeCopy();
	panel.setTermOptions(clonedTermOptions);
    }

    // implement OptionsPanelController
    @Override
    public JComponent getComponent(Lookup masterLookup) {
	panel = new TermOptionsPanel();
	return panel;
    }

    /**
     * Load data from model.
     * Called after getComponent().
     */

    // implement OptionsPanelController
    @Override
    public void update() {
	reset();
    }

    // implement OptionsPanelController
    @Override
    public void cancel() {
	reset();
    }

    // implement OptionsPanelController
    @Override
    public void applyChanges()  {
	if (termOptions == null) {
	    // update wasn't called
	    return;
	}
	// assign will fire a property change
	termOptions.assign(clonedTermOptions);
	termOptions.storeTo(prefs);
    }

    // implement OptionsPanelController
    @Override
    public boolean isChanged() {
	if (termOptions == null) {
	    // update wasn't called => no changes
	    return false;
	}

	if (clonedTermOptions == null) {
	    return false;
	} else {
	    return clonedTermOptions.isDirty();
	}
    }

    // implement OptionsPanelController
    @Override
    public boolean isValid() {
	// always valid
	return true;
    }

    // implement OptionsPanelController
    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx ("netbeans.optionsDialog.advanced.terminal"); // NOI18N
    }

    // implement OptionsPanelController
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    // implement OptionsPanelController
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
}
