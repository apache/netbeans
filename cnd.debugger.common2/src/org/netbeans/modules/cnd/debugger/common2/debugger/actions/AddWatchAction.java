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
