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

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionUI;
import org.netbeans.modules.cnd.debugger.common2.utils.options.UISet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionCustomizerPanel;
import org.openide.util.HelpCtx;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

class GlobalOptionsSubPanel extends javax.swing.JPanel
    implements OptionCustomizerPanel, HelpCtx.Provider {

    private UISet UISet = new UISet();

    public static class SessionStartup extends GlobalOptionsSubPanel {
	public SessionStartup() {
	    OptionUI[]	dbxtool_panels = {
		    //DebuggerOption.SUPPRESS_STARTUP_MESSAGE.createUI(),
                    DebuggerOption.TRACE_SPEED.createUI(),
		    DebuggerOption.FINISH_SESSION.createUI(),
		    DebuggerOption.SESSION_REUSE.createUI(),
		    DebuggerOption.BALLOON_EVAL.createUI(),
                    DebuggerOption.ARGS_VALUES_IN_STACK.createUI(),
                    DebuggerOption.DO_NOT_POPUP_DEBUGGER_ERRORS_DIALOG.createUI(),
		};  
	    OptionUI[]	panels = {
		    //DebuggerOption.SUPPRESS_STARTUP_MESSAGE.createUI(),
		    DebuggerOption.TRACE_SPEED.createUI(),
                    DebuggerOption.FINISH_SESSION.createUI(),
		    DebuggerOption.RUN_AUTOSTART.createUI(),
		    DebuggerOption.BALLOON_EVAL.createUI(),
                    DebuggerOption.ARGS_VALUES_IN_STACK.createUI(),
                    DebuggerOption.DO_NOT_POPUP_DEBUGGER_ERRORS_DIALOG.createUI(),
		};

	    if (NativeDebuggerManager.isStandalone() || NativeDebuggerManager.isPL()) {
		setup(dbxtool_panels);
            } else {
		setup(panels);
            }
	}
    }

    public static class WindowProperties extends GlobalOptionsSubPanel {
	public WindowProperties() {
	    OptionUI[] panels = {
		DebuggerOption.FRONT_IDE.createUI(),
		DebuggerOption.FRONT_DBGWIN.createUI(),
		DebuggerOption.FRONT_PIO.createUI(),
		/* does not apply to NB model anymore
		 * will grep focus to dbx console and resulting 
		 * need clicking twice on stepi button
		DebuggerOption.FRONT_DBX.createUI(),
		*/
		DebuggerOption.FRONT_ACCESS.createUI(),
		DebuggerOption.FRONT_MEMUSE.createUI(),
		DebuggerOption.OPEN_THREADS.createUI(),
		DebuggerOption.OPEN_SESSIONS.createUI(),
		DebuggerOption.OUTPUT_LIST_SIZE.createUI(),
	    };
	    setup(panels);
	}
    }
/*
    public static class Persistence extends GlobalOptionsSubPanel {
	public Persistence() {
	    OptionUI[] panels = {
		DebuggerOption.SAVE_BREAKPOINTS.createUI(),
		// LATER DebuggerOption.SAVE_WATCHES.createUI(),
	    };
	    setup(panels);
	}
    }

    public static class DebuggingBehaviour extends GlobalOptionsSubPanel {
	public DebuggingBehaviour() {
	    OptionUI[]	dbxtool_panels = {
		DebuggerOption.TRACE_SPEED.createUI(),
		// DebuggerOption.BALLOON_EVAL.createUI(),
	    };
	    
	    OptionUI[] panels = {
		DebuggerOption.TRACE_SPEED.createUI(),
		// DebuggerOption.RUN_AUTOSTART.createUI(),
		// DebuggerOption.BALLOON_EVAL.createUI(),
	    };
	    if (DebuggerManager.isStandalone())
		setup(dbxtool_panels);
	    else
		setup(panels);
	}
    }
*/
    protected GlobalOptionsSubPanel() {
	setBorder(new javax.swing.border.EtchedBorder());
    }

   protected void setup(OptionUI[] panels) {
	UISet.add(panels);
	OptionUI.fillPanel(this, panels);
    }

    // interface OptionCustomizerPanel
    @Override
    public void initValues(OptionSet options) {
	UISet.bind(options);
    }

    // interface OptionCustomizerPanel
    @Override
    public boolean areValuesValid() {
	return true;
    } 

    // interface OptionCustomizerPanel
    @Override
    public void storeValues() {
	UISet.applyChanges();
    }

    // interface HelpCtx.Provider
    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx( GlobalOptionsSubPanel.class );
    }
}
