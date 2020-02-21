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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.netbeans.spi.options.*;
import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;

/**
 * Panel for global native debugger options.
 * Was in Tools->Options->Miscellaneous.
 * Now in tab in Tools->Options->C/C++. See IZ 91044
 */
@OptionsPanelController.SubRegistration(
	id = CndUIConstants.TOOLS_OPTIONS_CND_DEBUGGER_ID,
	location = CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID,
	displayName = "#TAB_DebuggerOptionsPanelTitle", // NOI18N
	position = 550)
public final class GlobalAdvancedOption extends OptionsPanelController {

    private GlobalOptionsPanel preferencesDialog = new GlobalOptionsPanel();
    private OptionSet clonedOptions;// working copy
    private OptionSet options;	// keep so we can assign back to it

    // implement OptionsPanelController
    @Override
    public void applyChanges()  {
	preferencesDialog.applyChanges();

	options.assign(clonedOptions);	// copy back cloned values
	options.save();			// write to disk
	NativeDebuggerManager.get().applyGlobalOptions();
					    // propagate to all sessions
    }

    // implement OptionsPanelController
    @Override
    public void cancel() {
	preferencesDialog.cancelChanges();
    }

    // implement OptionsPanelController
    @Override
    public JComponent getComponent(Lookup masterLookup) {
	//preferencesDialog = new GlobalOptionsPanel();
	return preferencesDialog;
    }

    // implement OptionsPanelController
    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("GlobalDebuggingOptions");
    }

    // implement OptionsPanelController
    @Override
    public boolean isChanged() {
	if (clonedOptions == null) {
	    return false;

	} else {
	    // Call applyChanges(), otherwise values in UI elements
	    // will not make it to the actual clonedOptions and
	    // isDirty will never be true.
	    // This application leaves the original 'options' unmolested
	    // untile we assign back to it in applyChanges() above.
	    preferencesDialog.applyChanges();
            
            // since original 'options' are unmolested, until Apply/OK button is pressed in the options window,
            // find if current 'clonedOptions' and saved 'options' differ, without affecting the existing logic of isDirty()
            boolean isChanged = false;
            for(OptionValue o : options.values()) {
                isChanged = !o.type().getCurrValue(clonedOptions).equals(o.type().getCurrValue(options));
                if(isChanged) { // no need to iterate further
                    return true;
                }
            }
	    return isChanged;
	}
    }

    // implement OptionsPanelController
    @Override
    public boolean isValid() {
	// always valid
	return true;
    }


    /**
     * Load data from model.
     * Called after getComponent().
     */

    // implement OptionsPanelController
    @Override
    public void update() {
	options = NativeDebuggerManager.get().globalOptions();
	clonedOptions = options.makeCopy();
	clonedOptions.clearDirty();
	preferencesDialog.setOptions(clonedOptions);
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
