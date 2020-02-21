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
