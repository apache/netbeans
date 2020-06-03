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
