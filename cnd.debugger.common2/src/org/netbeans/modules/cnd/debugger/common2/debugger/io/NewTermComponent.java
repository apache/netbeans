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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.IOSelect;

import org.netbeans.modules.terminal.api.ui.IOTopComponent;
import org.netbeans.modules.terminal.api.ui.IOVisibility;

import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.Term;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.utils.NotifyingInputSteam;
import org.netbeans.modules.terminal.api.ui.IOTerm;
/**
 *
 */
public class NewTermComponent implements TermComponent {

    private final IOTopComponent owner;
    private final int flags;

    private final InputOutput io;
    private final ActiveTerm term;

    public NewTermComponent(IOTopComponent owner, int flags) {
	this.owner = owner;
	this.flags = flags;
	TermComponentFactory.ckFlags(flags);

	IOProvider iop = IOProvider.get("Terminal");	// NOI18N
	if (iop == null) {
	    throw new UnsupportedOperationException("Terminal IO's not available"); // NOI18N
	}

	String tabTitle = null;		// Use TC title
	io = iop.getIO(tabTitle, null, owner.ioContainer());

	if (IOTerm.isSupported(io)) {
	    term = (ActiveTerm) IOTerm.term(io);
	} else {
	    io.closeInputOutput();
	    throw new UnsupportedOperationException("Terminal IO doesn't provide a Term"); // NOI18N
	}

	if (IOVisibility.isSupported(io))
	    IOVisibility.setClosable(io, false);
     }

    // no-one calls this!
    /* OLD
    public void cleanup() {
	if (pty != null) {
	    try {
		pty.close();
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }
     */

    private InputStream interposeActivityDetector(InputStream out) {
	// 4896262, for non-remote PIO
	final NotifyingInputSteam notifier = new NotifyingInputSteam(out);
	notifier.setListener(new NotifyingInputSteam.Listener () {
            @Override
	    public void activity () {
		if (DebuggerOption.FRONT_PIO.isEnabled(
			    NativeDebuggerManager.get().globalOptions()))
		    NewTermComponent.this.requestVisible();
	    }
	});
	notifier.arm();

	out = notifier;
	return out;
    }

    @Override
    public void connectIO(OutputStream in, InputStream out) {
	out = interposeActivityDetector(out);
	term.connect(in, out, null);
    }

    @Override
    public ActiveTerm getActiveTerm() {
	return term;
    }

    @Override
    public Term getTerm() {
	return term;
    }

    @Override
    public int flags() {
	return flags;
    }

    @Override
    public boolean isPty() {
	return TermComponentFactory.isPty(flags);
    }

    @Override
    public boolean isActive() {
	return TermComponentFactory.isActive(flags);
    }

    @Override
    public boolean isPacketMode() {
	return TermComponentFactory.isPacketMode(flags);
    }

    @Override
    public boolean isRaw() {
	return TermComponentFactory.isRaw(flags);
    }

    @Override
    public void requestVisible() {
	// Pass on to containing TC.
	// Bring (TC's tab) to front but don't activate
	// Won't open the TC if it's not opened.
	// Mainly used for the DebuggerOption.FRONT_PIO option.
	// OLD io.select();
	owner.topComponent().requestVisible();
    }

    @Override
    public void switchTo() {
	// select the IO but don't front or activate anything.
	// IO.select() will also open the TC so is not the right thing!
	// Mainly used when we switch sessions.
        IOSelect.select(io, EnumSet.noneOf(IOSelect.AdditionalOperation.class));
    }

    @Override
    public void open() {
	// Pass on to containing TC.
	// Makes it visible but not active.

	owner.topComponent().open();
    }

    @Override
    public void bringUp() {
	// no-op
	// OldTermComponent wasn't self-inserting on construction like us
	// so required an explicit bringUp().
	// OLD io.select();
    }

    @Override
    public void bringDown() {
	// Mainly used when session goes away.
	io.closeInputOutput();
    }

    @Override
    public InputOutput getIO() {
        return io;
    }
}
