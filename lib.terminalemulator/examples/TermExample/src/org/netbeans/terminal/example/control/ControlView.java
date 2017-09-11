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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.terminal.example.control;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.openide.windows.IOSelect;
import org.openide.windows.InputOutput;

import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Term;

import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.ui.IOTerm;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.netbeans.terminal.example.Config;


/**
 *
 * @author ivan
 */
public class ControlView {
    private final ActiveTerm term;

    private static class VAction {
	public final String cmd;
	public final InputOutput io;

	public VAction(String cmd) {
	    this.cmd = cmd;
	    this.io = null;
	}

	public VAction(String cmd, InputOutput io) {
	    this.cmd = cmd;
	    this.io = io;
	}
    }
    public ControlView(ActiveTerm term) {
	this.term = term;

	term.setBackground(Color.white);
	term.setScrollOnOutput(false);
	term.setTrackCursor(false);

	term.setActionListener(new ActiveTermListener() {

	    public void action(ActiveRegion r, InputEvent e) {
		VAction action = (VAction) r.getUserObject();
		if (action == null)
		    return;

		Set<IOSelect.AdditionalOperation> extraOps = Collections.emptySet();

		if (action.cmd.equals("refresh"))
		    ;
		else if (action.cmd.equals("closeInputOutput"))
		    action.io.closeInputOutput();
		else if (action.cmd.equals("select"))
		    action.io.select();
		else if (action.cmd.equals("select(<empty>)")) {
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN);
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN+REQ_VISIBLE)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		                          IOSelect.AdditionalOperation.REQUEST_VISIBLE);
		    IOSelect.select(action.io, extraOps);
		}
		else if (action.cmd.equals("select(OPEN+REQ_VISIBLE+REQ_ACTIVE)")) {
		    extraOps = EnumSet.of(IOSelect.AdditionalOperation.OPEN,
		                          IOSelect.AdditionalOperation.REQUEST_VISIBLE,
					  IOSelect.AdditionalOperation.REQUEST_ACTIVE);
		    IOSelect.select(action.io, extraOps);
		}

		else if (action.cmd.equals("visibility: on")) {
		    IOVisibility.setVisible(action.io, true);
		}
		else if (action.cmd.equals("visibility: off")) {
		    IOVisibility.setVisible(action.io, false);
		}

		else if (action.cmd.equals("closable: true")) {
		    IOVisibility.setClosable(action.io, true);
		}
		else if (action.cmd.equals("closable: false")) {
		    IOVisibility.setClosable(action.io, false);
		}

		else if (action.cmd.equals("debug: on")) {
		    Term term = IOTerm.term(action.io);
                    if (term != null)
                        term.setDebugFlags(Term.DEBUG_INPUT|Term.DEBUG_OUTPUT |Term.DEBUG_OPS);
		}
		else if (action.cmd.equals("debug: off")) {
		    Term term = IOTerm.term(action.io);
                    if (term != null)
                        term.setDebugFlags(0);
		}

		refresh();
	    }
	});
    }

    ActiveRegion beginRegion() {
	boolean hyperlink = true;
	ActiveRegion region = term.beginRegion(hyperlink);
	region.setFeedbackEnabled(true);
	return region;
    }

    void endRegion() {
	term.endRegion();
    }

    public void clear() {
        term.regionManager().reset();
        term.setText("");
    }

    void begin() {
	term.setAnchored(true);
    }

    void end() {
	term.appendText("", true);	// true -> repaint
    }

    void printf(String format, Object ... args) {
	String str = String.format(format, args);
	term.appendText(str, false);	// false -> don't repaint
    }

    public void tabTo(int target_column) {
        Coord cursor = term.getCursorCoord();
        if (target_column == cursor.col)
            return;
        if (cursor.col > target_column) {
            // target is behind us, start a new line
            term.appendText("\n", false);
            cursor = term.getCursorCoord();
        }

        while (cursor.col < target_column) {
            term.appendText(" ", false);
            cursor = term.getCursorCoord();
        }
    }

    private String yesNo(boolean v) {
	if (v)
	    return "";
	else
	    return "no-";
    }

    private void printAction(InputOutput io, String link, String cmd) {
	ActiveRegion r = beginRegion();
	r.setUserObject(new VAction(cmd, io));
	printf(link);
	endRegion();
    }

    private void printAction(InputOutput io, String cmd) {
	ActiveRegion r = beginRegion();
	r.setUserObject(new VAction(cmd, io));
	printf(cmd);
	endRegion();
    }

    private void printIO(ControlModel.IOInfo ii) {
	Config config = ii.config;
	InputOutput io = ii.io;

	printf("'%s'", ii.name);
	printf("\n");
	tabTo(8);

	printf("iop %-7s", config.getIOProvider());
	printf(" container %-7s", config.getContainerProvider());
	printf(" shuttle %-8s", config.getIOShuttling());
	printf(" %-6s", config.getContainerStyle());
	printf(" %srestartable", yesNo(config.isRestartable()));
	printf(" %shupOnClose", yesNo(config.isHUPOnClose()));
	printf(" %skeep", yesNo(config.isKeep()));
	printf("\n");

	tabTo(8);
	printf("%sclosed", yesNo(io.isClosed()));
	printf(" ");
	printAction(io, "closeInputOutput");
	printf(" ");
	printAction(io, "select");
	printf("\n");

	tabTo(8);
	boolean closable = IOVisibility.isClosable(io);
	String vetoable;
	switch (config.getAllowClose()) {
	    case ALWAYS:
		vetoable = "no-vetoable";
		break;
	    case DISCONNECTED:
		vetoable = "vetoable";
		break;
	    default:
	    case NEVER:
		vetoable = "vetoable-n/a";
		break;
	}
	printf("%sclosable ", yesNo(closable));
	printf("%s ", vetoable);

	if (IOVisibility.isSupported(io)) {
	    printf(" visibility: ");
	    printAction(io, "on", "visibility: on");
	    printf(" ");
	    printAction(io, "off", "visibility: off");
	    printf("  closable: ");
	    printAction(io, "true", "closable: true");
	    printf(" ");
	    printAction(io, "false", "closable: false");
	}
	printf("\n");

	tabTo(8);
	if (IOConnect.isSupported(io)) {
	    printf("%sconnected", yesNo(IOConnect.isConnected(io)));
	} else {
	    printf("IOConnect not supported");
	}
	printf("\n");

	tabTo(8);
	if (IOEmulation.isSupported(io)) {
	    printf("emulation %-8s  %sdisciplined",
		    IOEmulation.getEmulation(io),
		    yesNo(IOEmulation.isDisciplined(io)));
	} else {
	    printf("IOEmulation not supported");
	}
	printf("\n");

        if (IOTerm.isSupported(io)) {
            tabTo(8);
	    printf("debug: ");
	    printAction(io, "on", "debug: on");
            printf(" ");
	    printAction(io, "off", "debug: off");
        }
	printf("\n");

	tabTo(8);
	if (IOSelect.isSupported(io)) {
	    printf("IOSelect: ");
	    printAction(io, "select(<empty>)");
	    printf(" ");
	    printAction(io, "select(OPEN)");
	    printf(" ");
	    printAction(io, "select(OPEN+REQ_VISIBLE)");
	    printf(" ");
	    printAction(io, "select(OPEN+REQ_VISIBLE+REQ_ACTIVE)");
	} else {
	    printf("IOSelect not supported");
	}
	printf("\n");
    }

    public void refresh() {
	clear();
	begin();

	printAction(null, "refresh");
	printf("\n");

	// Print all open IO's
	for (ControlModel.IOInfo ii : ControlModel.list()) {
	    if (! ii.io.isClosed())
		printIO(ii);
	}

	printf("------------------ closed ----------------\n");
	// Print all closed IO's
	for (ControlModel.IOInfo ii : ControlModel.list()) {
	    if (ii.io.isClosed())
		printIO(ii);
	}

	end();
    }
}
