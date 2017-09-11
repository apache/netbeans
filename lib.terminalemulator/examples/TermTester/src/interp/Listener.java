/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package interp;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.TermInputListener;

/**
 * Listener mediates IO between a Term and an Interp.
 * 
 * It's the central element in a ReadEvalPrint loop where
 * the reading is event driver by ListenerListener, the evaluation
 * is handled by Interp.execute() and the printing is mediated by
 * ListenerOutput.
 */

public final class Listener extends Thread implements ListenerOutput {
    private final StreamTerm term;
    private final Interp interp;
    private final BlockingQueue<Character> queue = new LinkedBlockingQueue<Character>();

    private StringBuffer buf = new StringBuffer();

    private class ListenerListener implements TermInputListener {

        public void sendChars(char c[], int offset, int count) {
            for (int cx = 0; cx < count; cx++)
                sendChar(c[offset+cx]);
        }
        public void sendChar(char c) {
            try {
                queue.put(c);
            } catch (InterruptedException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (false) {
                // With LineDiscipline pushed we get NL instead or CR.
                final char EOL = 10;
                if (c == EOL) {
                    interp.execute(Listener.this, buf.toString());
                    prompt();
                    buf = new StringBuffer();
                } else {
                    buf.append(c);
                }
            }
	}
    }

    public Listener(StreamTerm term, Interp interp) {
	this.term = term;
	this.interp = interp;

	term.setReadOnly(false);
	term.setHistorySize(10000);
	term.setHorizontallyScrollable(false);
	term.pushStream(new LineDiscipline());
	term.addInputListener(new ListenerListener());

        interp.setOutput((ListenerOutput) this);
	interp.greet();
	prompt();
    }

    @Override
    public void run() {
        while(true) {
            char c;
            try {
                c = queue.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            // With LineDiscipline pushed we get NL instead or CR.
            final char EOL = 10;
            if (c == EOL) {
                interp.execute(Listener.this, buf.toString());
                prompt();
                buf = new StringBuffer();
            } else {
                buf.append(c);
            }
        }
    }

    private void prompt() {
	printf("> ");
    }

    private void printWork(String msg) {
        final int len = msg.length();
        final char cbuf[] = new char[len];
        msg.getChars(0, len, cbuf, 0);
        term.putChars(cbuf, 0, len);
    }

    public void print(final String msg) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    printWork(msg);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void println(final String msg) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    printWork(msg + '\n');
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
}

    public void printf(final String fmt, final Object ... args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    printWork(String.format(fmt, args));
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
