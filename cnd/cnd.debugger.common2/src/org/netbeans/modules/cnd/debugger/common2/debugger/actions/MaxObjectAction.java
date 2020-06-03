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


package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;

public class MaxObjectAction
    extends CallableSystemAction implements StateListener {

    private static class MaxObjectProcessor implements PropertyChangeListener {

	private final DialogDescriptor dd;
	private final EditMaxObjectPanel panel;

	MaxObjectProcessor(NativeDebugger debugger, String max_object_size) {
	    panel = new EditMaxObjectPanel(debugger, max_object_size);
	    Catalog.setAccessibleDescription(panel,

		"ACSD_MaxObjectSize");	// NOI18N
	    boolean isModal = true;
	    dd = new DialogDescriptor(panel,
				      Catalog.get("TTL_MaxObjectSize"),//NOI18N
				      isModal,
				      null);
	    Object[] buttons = new Object[] {
		DialogDescriptor.OK_OPTION,
		DialogDescriptor.CANCEL_OPTION
	    };
	    dd.setOptions(buttons);
	    dd.setClosingOptions(buttons);
	    panel.getController().addPropertyChangeListener(this);
	    setValid();
	}

	public void setVisible(boolean v) {
	    DialogDisplayer.getDefault().notify(dd);	// will block

	    boolean ok = (dd.getValue() == DialogDescriptor.OK_OPTION);
	    Controller controller = panel.getController();
	    if (ok) {
		controller.ok();
	    } else {
		controller.cancel();
	    }
	}

	// interface PropertyChangeListener
        @Override
	public void propertyChange(PropertyChangeEvent e) {
	    if (e.getPropertyName () == Controller.PROP_VALID) {
		setValid();
	    }
	}

	void setValid() {
	    dd.setValid(panel.getController().isValid());
	}
    }

    // interface CallableSystemAction
    @Override
    public void performAction() {
	final NativeDebugger debugger =
	    NativeDebuggerManager.get().currentNativeDebugger();
	if (debugger != null) {
	    String max_object_size = DebuggerOption.OUTPUT_MAX_OBJECT_SIZE.getCurrValue(debugger.optionLayers());
	    MaxObjectProcessor processor = new MaxObjectProcessor(debugger, max_object_size);
	    if (processor != null)
	        processor.setVisible(true);		// blocks?
	}
    }
    
    // interface CallableSystemAction
    @Override
    public boolean asynchronous() {
	return false;
    }

    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("ACT_Max_Object_Size"); // NOI18N
    }
    
    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }

    @Override
    protected String iconResource () {
	return "org/netbeans/modules/debugger/resources/actions/NewWatch.gif"; // NOI18N
    }


    // interface SystemAction
    @Override
    protected void initialize() {
	super.initialize();
	setEnabled(false);
    }

    // interface StateListener
    @Override
    public void update(State state) {
	boolean enable = false;
	if (state != null) {
	    enable = state.isLoaded && state.isListening();
	}
	setEnabled(enable);
    }
}

