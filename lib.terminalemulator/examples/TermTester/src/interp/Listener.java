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
