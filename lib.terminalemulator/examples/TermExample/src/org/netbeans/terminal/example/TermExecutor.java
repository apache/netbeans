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

package org.netbeans.terminal.example;

import org.netbeans.lib.richexecution.PtyProcess;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.richexecution.OS;
import org.netbeans.lib.richexecution.program.Program;
import java.awt.Dimension;
import java.util.Map;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.Pty.Mode;

/**
 * Execute a program connected to a Term.
 * @author ivan
 */
public final class TermExecutor {
    private final static OS os = OS.get();
    private final PtyExecutor delegate = new PtyExecutor();
    private Boolean lineDiscipline = null;
    private boolean debug = false;

    public TermExecutor() {
        switch (os) {
            case WINDOWS:
		delegate.setMode(Mode.NONE);
                break;
	    default:
		break;
        }
    }

    private class MyTermListener implements TermListener {
        private final Pty pty;

        public MyTermListener(Pty pty) {
            this.pty = pty;
        }

        public void sizeChanged(Dimension cells, Dimension pixels) {
            /* LATER
            if (pty.isRaw())
                return;     // otherwise SWINSZ will give us an IOException
            */

            // On Linux, Solaris and Mac setting WINSZ from the master
            // fd works.
            // On Linux and (I think) on Solaris and Mac setting it from the
            // slave fd works.
	    switch (OS.get()) {
		/* OLD
		case MACOS:
		    pty.slaveTIOCSWINSZ(cells.height, cells.width,
					 pixels.height, pixels.width);
		    break;
		 */
		default:
		    pty.masterTIOCSWINSZ(cells.height, cells.width,
					 pixels.height, pixels.width);
		    break;
	    }
        }

        public void titleChanged(String title) {
            ;   // ignore - for now
        }

        public void cwdChanged(String cwd) {
            ;   // ignore - for now
        }

	public void externalToolCalled(String command) {
            ;   // ignore - for now
	}
    }

    private static void error(String fmt, Object...args) {
        String msg = String.format(fmt, args);
        throw new IllegalStateException(msg);
    }

    /**
     * Set whether Terms line discipline should be used.
     * Should be called before start().
     */
    public void setLineDiscipline(Boolean lineDiscipline) {
        this.lineDiscipline = lineDiscipline;
    }

    /**
     * Allows control of Term debugging
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Start this program running under the given Term.
     * @return The wrapper process.
     */
    public PtyProcess start(Program program, StreamTerm term) {
        if (program.command().isEmpty())
            error("Empty command");

        Pty pty = null;

        //
        // Check and adjust arguments
        //
        switch (os) {
            case WINDOWS:
                if (delegate.getMode() != Mode.NONE)
                    error("Can only use 'pipe' mode on windows");
                break;
            case LINUX:
                break;
        }

        //
        // Create pty
        //
        switch (delegate.getMode()) {
            case NONE:
                break;
            case REGULAR:
                try {
                    pty = Pty.create(Pty.Mode.REGULAR);
                } catch (Exception x) {
                    System.out.printf("Exception %s\n",x);
                }
                break;
            case RAW:
                error("raw pty mode not supported yet");
                break;
            case PACKET:
                error("packet pty mode not supported yet");
                break;
        }

        //
        // Create Term
        //
        if (debug)
            term.setDebugFlags(Term.DEBUG_INPUT |
                               Term.DEBUG_OUTPUT |
                               Term.DEBUG_OPS |
			       Term.DEBUG_KEYS);

        if (pty != null)
            term.addListener(new MyTermListener(pty));
        
        // 
        // Push own line discipline if needed or overriden
        //
        if (lineDiscipline != null) {
            if (lineDiscipline)
                term.pushStream(new LineDiscipline());
        } else {
            switch (delegate.getMode()) {
                case NONE:
                case RAW:
                    term.pushStream(new LineDiscipline());
                    break;
            }
        }
        
        //
        // Start program and connect it to term
        //
        Map<String, String> env = program.environment();
        env.put("TERM", term.getEmulation());
	PtyProcess ptyProcess = delegate.start(program, pty);

        /* OLD
        if (pty == null) {
            term.connect(ptyProcess.getOutputStream(), ptyProcess.getInputStream(), null);
        } else {
            term.connect(pty.getOutputStream(), pty.getInputStream(), null);
        }
        */
        term.connect(ptyProcess.getOutputStream(), ptyProcess.getInputStream(), null);

        // TMP reaper = ptyProcess.getReaper();
        return ptyProcess;
    }

    public void setMode(Mode mode) {
        delegate.setMode(mode);
    }

    public Mode getMode() {
        return delegate.getMode();
    }
}
