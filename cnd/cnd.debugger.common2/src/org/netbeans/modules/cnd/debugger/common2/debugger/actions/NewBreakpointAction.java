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

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
import org.netbeans.modules.cnd.debugger.common2.debugger.DialogManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.EditBreakpointPanel;


/**
 * dbxtool-specific NewBreakpointAction.
 * The main reason we have our own is so that we can post an error 
 * if there's no program loaded.
 * See also org.netbeans.modules.debugger.ui.actions.AddBreakpointAction
 * See also org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.
 *          BreakpointFilter.CustomizeBreakpointProcessor
 */

public class NewBreakpointAction
    extends CallableSystemAction implements StateListener {

    /** Generated serial version UID. */
    static final long serialVersionUID = -8705899978543961455L;
    

    private static class NewBreakpointProcessor extends DialogManager
	implements ActionListener, PropertyChangeListener {

	private Dialog dialog;
	private final DialogDescriptor dd;
	private final EditBreakpointPanel panel;

	NewBreakpointProcessor() {
	    panel = new EditBreakpointPanel(null);
	    boolean isModal = true;
	    dd = new DialogDescriptor(panel,
				      Catalog.get("TITLE_NewBreakpoint"), // NOI18N
				      isModal,
				      this);
	    Object[] buttons = new Object[] {
		DialogDescriptor.OK_OPTION,
		DialogDescriptor.CANCEL_OPTION
	    };
	    dd.setOptions(buttons);
	    // We will close the dialog ourselves.
	    // This so it's still there if we pop up an error.
	    // OLD dd.setClosingOptions(buttons);
	    dd.setClosingOptions(new Object[0]);
	    dialog = DialogDisplayer.getDefault().createDialog(dd);
	    dialog.pack();

	    setValid();

	    panel.addPropertyChangeListener(this);

	    Controller controller = panel.getController();
	    if (controller != null)
		controller.addPropertyChangeListener(this);
	}

	public void setVisible(boolean v) {
	    dialog.setVisible(v);
	}

	/**
	 * Called when some dialog button is pressed
	 */

	// interface ActionListener
        @Override
	public void actionPerformed(ActionEvent e) {
	    boolean ok = (e.getSource() == DialogDescriptor.OK_OPTION);
	    accept(ok);
	}

	// interface DialogManager
        @Override
	public void accept(boolean yes) {
	    boolean done = false;
	    Controller controller = panel.getController();
	    if (yes) {
		NativeDebuggerManager.get().registerDialog(this);
		//System.out.println("NewBreakpointProcessor: OK");
		done = controller.ok();
		// keep up until bringDown is called externally.
	    } else {
		NativeDebuggerManager.get().registerDialog(this);
		//System.out.println("NewBreakpointProcessor: Cancel");
		done = controller.cancel();
		// will call bringDown:
		NativeDebuggerManager.get().bringDownDialog();
	    }
	}

	// interface DialogManager
        @Override
	public void bringDown() {
	    dialog.setVisible(false);
	    dialog.dispose();
	    dialog = null;
	}

	// interface DialogManager
        @Override
	public void refocus() {
            // do nothing
	}


	// interface PropertyChangeListener
        @Override
	public void propertyChange(PropertyChangeEvent e) {
	    if (e.getPropertyName () == EditBreakpointPanel.PROP_TYPE) {
		stopListening ();
		setValid ();
		startListening ();
	    } else
	    if (e.getPropertyName () == Controller.PROP_VALID) {
		setValid();
	    }
	}

	void startListening () {
	    Controller controller = panel.getController ();
	    if (controller == null) 
		return;
	    controller.addPropertyChangeListener (this);
	}

	void stopListening () {
	    Controller controller = panel.getController ();
	    if (controller == null) 
		return;
	    controller.removePropertyChangeListener (this);
	    controller = null;
	}


	void setValid() {
	    dd.setValid(panel.getController().isValid());
	}
    }

    // interface CallableSystemAction
    @Override
    public boolean asynchronous() {
	return false;
    }

    // interface CallableSystemAction
    @Override
    public void performAction() {

	boolean canDo;

	if (! NativeDebuggerManager.isPerTargetBpts()) {
	    canDo = true;
	} else {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	    if (debugger == null)
		canDo = false;
	    else if (!debugger.state().isLoaded)
		canDo = false;
	    else
		canDo = true;
	}

	if (canDo) {
	    NewBreakpointProcessor processor = new NewBreakpointProcessor();
	    processor.setVisible(true);
	} else {
	    NativeDebuggerManager.errorLoadBeforeBpt();
	}
    }
    
    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("ACT_BPT_NewBreakpoint"); // NOI18N
    }
    
    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }

    // interface SystemAction
    @Override
    protected void initialize() {
	super.initialize();
	// OLD setEnabled(DebuggerManager.isGlobalBreakpoints());
	setEnabled(true);	// see update() below
    }

    // interface StateListener
    @Override
    public void update(State state) {
	// Always keep it enabled.
	// In cases where we can't accept bpts we'll post an error
	// dialog instead.
	setEnabled(true);

	/* OLD
	if (! DebuggerManager.isPerTargetBpts()) {
	    setEnabled(true);
	} else {
	    boolean enable = false;
	    if (state != null) {
		enable = state.isLoaded && state.isListening() ;
	    }
	    setEnabled(enable);
	}
	*/
    }
}
