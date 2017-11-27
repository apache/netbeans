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

package nbterm;

import org.netbeans.lib.richexecution.PtyProcess;
import org.netbeans.lib.richexecution.PtyExecutor;
import org.netbeans.lib.richexecution.OS;
import org.netbeans.lib.richexecution.program.Program;
import java.awt.Dimension;
import java.util.Map;
import javax.swing.JFrame;
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
    private JFrame titledWindow;

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
            if (titledWindow != null)
                titledWindow.setTitle(title);
        }

        public void cwdChanged(String cwd) {
            // ignore -- for now
       }

	public void externalToolCalled(String command) {
            // ignore -- for now
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

        if (false) {
            String charSet;
            // charSet = "ISO-8859-1";
            charSet = "UTF-8";           // won't render ansi graphics chars correctly.
            term.connect(ptyProcess.getOutputStream(), ptyProcess.getInputStream(), null, charSet);
        } else {
            term.connect(ptyProcess.getOutputStream(), ptyProcess.getInputStream(), null);
        }

        // TMP reaper = ptyProcess.getReaper();
        return ptyProcess;
    }

    public void setMode(Mode mode) {
        delegate.setMode(mode);
    }

    public Mode getMode() {
        return delegate.getMode();
    }

    public void setTitledWindow(JFrame titledWindow) {
        this.titledWindow = titledWindow;
    }
}
