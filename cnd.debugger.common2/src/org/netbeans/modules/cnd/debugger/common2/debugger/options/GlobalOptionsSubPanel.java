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
