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

import java.util.Set;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.spi.debugger.ContextProvider;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import java.util.Collections;
import org.netbeans.api.debugger.ActionsManager;

public class AddWatchAction extends NativeActionsProvider {

    /** Generated serial version UID. */
    static final long serialVersionUID = -8705899978543961455L;

    private static class AddWatchProcessor implements PropertyChangeListener {

	private final DialogDescriptor dd;
	private final EditWatchPanel panel;

	AddWatchProcessor(NativeDebugger debugger, String scope) {
	    panel = new EditWatchPanel(debugger, null, scope, null);
	    boolean isModal = true;

	    dd = new DialogDescriptor(panel,
				      Catalog.get("TTL_NewWatch"), // NOI18N
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
            DialogDisplayer.getDefault().notify(dd);    // will block

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

    public AddWatchAction(ContextProvider ctx) {
        super(ctx);
    }

    @Override
    public void doAction(Object action) {
        final NativeDebugger debugger =
	    NativeDebuggerManager.get().currentNativeDebugger();
	AddWatchProcessor processor = new AddWatchProcessor(debugger, null);
	processor.setVisible(true);		// blocks?
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_NEW_WATCH);
    }

    @Override
    public boolean isEnabled(Object action) {
        return true;
    }

//    // interface CallableSystemAction
//    public boolean asynchronous() {
//	return false;
//    }
//
//    // interface SystemAction
//    public String getName() {
//	return Catalog.get("ACT_WATCH_NewWatch"); // NOI18N
//    }
//
//    // interface SystemAction
//    public HelpCtx getHelpCtx() {
//        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
//    }
//
//    protected String iconResource () {
//	return "org/netbeans/modules/debugger/resources/actions/NewWatch.gif"; // NOI18N
//    }


    // interface SystemAction
//    protected void initialize() {
//	super.initialize();
//	setEnabled(true);
//    }

    // interface StateListener
    @Override
    public void update(State state) {
	boolean enable = true;
	if (state != null) {
	    enable = state.isListening();
	}
//	setEnabled(enable);
    }
}
