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
package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.LinkedList;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.utils.InfoPanel;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Tap into the io between gdb and Term. - It echoes stuff it gets from gdb to
 * the Term while accumulating lines and sending them on to the MI processor via
 * MIProxy.processLine(). In this sense it works a bit like the unix 'tee(1)'
 * command. - It passes on stuff typed by the user on to gdb. - It allows
 * commands to be sent to gdb programmatically via MICommandInjector.inject() -
 * It allows informative message to be printed via MICommandInjector.log().
 *
 * It also colorizes lines. - Injected commands destined to gdb is in bold
 * black. - Stuff typed by user destined to gdb is in bold blue. - Error and
 * informative message from the ide (MIInjector.log) are printed in blue but not
 * forwarded to gdb. - gdb console stream ouptut (~) is echoed in green. - gdb
 * errors are echoed in red.
 *
 * Modelled after org.netbeans.lib.terminalemulator.LineDiscipline "put" is from
 * process to console. "send" is from "keyboard" to process.
 */
/*package*/ class Tap extends org.netbeans.lib.terminalemulator.TermStream implements MICommandInjector {

    // characters from gdb accumulate here and are forwarded to the tap
    private final StringBuilder interceptBuffer = new StringBuilder();
    private boolean interceptBufferTerminated;
    private final LinkedList<String> interceptedLines = new LinkedList<String>();

    private final StringBuilder toTermBuf = new StringBuilder();
    private boolean toTermBufTerminated;
    private MIProxy miProxy;
    private GdbDebuggerImpl debugger;
    private boolean prompted = false;

    /*package*/ Tap() {
    }

    @Override
    public void flush() {
        toDTE.flush();
    }

    /**
     * Put character from gdb to console.
     *
     * @param c
     */
    @Override
    public void putChar(char c) {
        processCharFromGdb(c);
        dispatchInterceptedLines();
    }

    /**
     * Put characters from gdb to console.
     *
     * @param buf
     * @param offset
     * @param count
     */
    @Override
    public void putChars(char[] buf, int offset, int count) {
        CndUtils.assertUiThread();
        for (int bx = 0; bx < count; bx++) {
            processCharFromGdb(buf[offset + bx]);
        }
        dispatchInterceptedLines();
    }

    /**
     * Send character typed into console to gdb
     *
     * @param c
     */
    @Override
    public void sendChar(char c) {
        CndUtils.assertTrueInConsole(false, "should not be used; KeyProcessingStream should send only lines");
        toDCE.sendChar(c);
    }
    
    private static final Preferences prefs =
        NbPreferences.forModule(GdbDebuggerImpl.class);
    private static final String PREFIX = "Doption."; // NOI18N
    private static final String PREF_DONOTSHOWAGAIN = PREFIX + "doNotShowAgain";//NOI18N    
    /**
     * Send character typed into console to gdb
     *
     * @param c
     * @param offset
     * @param count
     */
    @Override
    public void sendChars(char c[], int offset, int count) {
        if (!prefs.getBoolean(PREF_DONOTSHOWAGAIN + "MSG_OldGdbVersionConsole", false) && //NOI18N
                !debugger.getGdbVersionPeculiarity().supports(GdbVersionPeculiarity.Feature.BREAKPOINT_NOTIFICATIONS)) {
            // IDE is unable to detect that something should be updated without a notification
            InfoPanel panel = new InfoPanel(Catalog.get("MSG_OldGdbVersionConsole"));//NOI18N
            NotifyDescriptor dlg = new NotifyDescriptor.Confirmation(
                    panel,
                    Catalog.get("INFORMATION"), // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION);
            DialogDisplayer.getDefault().notify(dlg);
            prefs.putBoolean(PREF_DONOTSHOWAGAIN + "MSG_OldGdbVersionConsole", //NOI18N
                    panel.dontShowAgain());

        }
        prompted = false;

        final String line = String.valueOf(c, offset, count);
        CndUtils.assertTrueInConsole(line.length() == 0 || line.endsWith("\n"), "KeyProcessingStream should send only lines");
        String cmd = line.trim();
        if (debugger != null) {
            debugger.sendTerminalTypedCommand(cmd);
        } else {
            // as fallback while debugger is not yet initialized
            toDCE.sendChars(c, offset, count);
            toDCE.flush();
        }
    }

    /*package*/ void setMiProxy(MIProxy miProxy) {
        this.miProxy = miProxy;
    }

    /*package*/ void setDebugger(GdbDebuggerImpl debugger) {
        this.debugger = debugger;
    }

    private final RequestProcessor sendQueue = new RequestProcessor("GDB send queue", 1); // NOI18N
    private static final boolean TRACING_IN_CONSOLE = CndUtils.getBoolean("cnd.gdb.trace.console", false); // NOI18N

    private static final int TERMINAL_LINE_SIZE_LIMIT = Integer.getInteger("gdb.terminal.linesize", 64*1024); //NOI18N
    private static final int MI_LINE_SIZE_LIMIT = Integer.getInteger("gdb.mi.linesize", 16*1024*1024); // NOI18N

    // interface MICommandInjector
    @Override
    public void inject(String cmd) {
        final char[] cmda = cmd.toCharArray();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // echo
                if (TRACING_IN_CONSOLE) {
                    toDTE.putChars(KeyProcessing.ESCAPES.BOLD_SEQUENCE, 0, KeyProcessing.ESCAPES.BOLD_SEQUENCE.length);
                    toDTE.putChars(cmda, 0, cmda.length);
                    toDTE.putChar(KeyProcessing.ESCAPES.CHAR_CR);			// tack on a CR
                    toDTE.putChars(KeyProcessing.ESCAPES.RESET_SEQUENCE, 0, KeyProcessing.ESCAPES.RESET_SEQUENCE.length);
                    toDTE.flush();
                }

                // send to gdb
                sendQueue.post(new Runnable() {
                    @Override
                    public void run() {
                        toDCE.sendChars(cmda, 0, cmda.length);
                    }
                });
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    /*package*/ void printError(String errMsg) {
        final char[] cmda = errMsg.toCharArray();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // echo
                toDTE.putChars(KeyProcessing.ESCAPES.RED_SEQUENCE, 0, KeyProcessing.ESCAPES.RED_SEQUENCE.length);
                toDTE.putChars(cmda, 0, cmda.length);
                toDTE.putChar(KeyProcessing.ESCAPES.CHAR_CR);			// tack on a CR
                toDTE.putChar(KeyProcessing.ESCAPES.CHAR_LF);			// tack on a CR
                toDTE.putChars(KeyProcessing.ESCAPES.RESET_SEQUENCE, 0, KeyProcessing.ESCAPES.RESET_SEQUENCE.length);
                toDTE.flush();
            }
        });
    }

    // interface MICommandInjector
    @Override
    public void log(String cmd) {
        final char[] cmda = cmd.toCharArray();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // echo
                toDTE.putChars(KeyProcessing.ESCAPES.LOG_SEQUENCE, 0, KeyProcessing.ESCAPES.LOG_SEQUENCE.length);
                toDTE.putChars(cmda, 0, cmda.length);
                // toDTE.putChar(char_CR);			// tack on a CR
                toDTE.putChars(KeyProcessing.ESCAPES.RESET_SEQUENCE, 0, KeyProcessing.ESCAPES.RESET_SEQUENCE.length);
                if (prompted) {
                    toDTE.putChars(PROMPT.toCharArray(), 0, PROMPT.length());
                    toDTE.putChar(KeyProcessing.ESCAPES.CHAR_CR);			// tack on a CR
                    toDTE.putChar(KeyProcessing.ESCAPES.CHAR_LF);
                }
                toDTE.flush();
            }
        });

        // don't send to gdb
    }

    /**
     * Process character from gdb to console.
     */
    private void processCharFromGdb(char c) {
        if (c == KeyProcessing.ESCAPES.CHAR_LF) {
            if (toTermBufTerminated) {
                toTermBuf.append("..."); //NOI18N
                toTermBufTerminated = false;
            }
            toTermBuf.append(c);
            if (interceptBufferTerminated) {
                interceptBuffer.append("..."); //NOI18N
                toTermBufTerminated = false;
            }
            interceptBuffer.append(c);
        } else {
            if (toTermBuf.length() < TERMINAL_LINE_SIZE_LIMIT) {
                toTermBuf.append(c);
                toTermBufTerminated = false;
            } else {
                toTermBufTerminated = true;
            }
            if (interceptBuffer.length() < MI_LINE_SIZE_LIMIT) {
                interceptBuffer.append(c);
                interceptBufferTerminated = false;
            } else {
                interceptBufferTerminated = true;
            }
        }

        // detected EOL
        if (c == KeyProcessing.ESCAPES.CHAR_LF) {

            String line = interceptBuffer.toString();
            synchronized (interceptedLines) {
                interceptedLines.addLast(line);
            }
            interceptBuffer.delete(0, interceptBuffer.length());

            // Map NL to NLCR
            toTermBuf.append(KeyProcessing.ESCAPES.CHAR_CR);

            // do some pattern recognition and alternative colored output.
            if (line.startsWith("~")) { // NOI18N
                if (TRACING_IN_CONSOLE) {
                    // comment line
                    toTermBuf.insert(0, KeyProcessing.ESCAPES.GREEN_SEQUENCE);
                    toTermBuf.append(KeyProcessing.ESCAPES.RESET_SEQUENCE);
                } else {
                    toTermBuf.delete(0, toTermBuf.length());
                }
            } else if (line.startsWith("&") || line.startsWith("*") || line.startsWith("=")) { // NOI18N
                if (TRACING_IN_CONSOLE) {
                    // output
                    toTermBuf.insert(0, KeyProcessing.ESCAPES.BROWN_SEQUENCE);
                    toTermBuf.append(KeyProcessing.ESCAPES.RESET_SEQUENCE);
                } else {
                    toTermBuf.delete(0, toTermBuf.length());
                }
            } else {
                int caretx = line.indexOf('^');
                if (caretx != -1) {
                    if (TRACING_IN_CONSOLE) {
                        if (line.startsWith("^error,", caretx)) { // NOI18N
                            // error
                            toTermBuf.insert(0, KeyProcessing.ESCAPES.RED_SEQUENCE);
                            toTermBuf.append(KeyProcessing.ESCAPES.RESET_SEQUENCE);
                        }
                    } else {
                        toTermBuf.delete(0, toTermBuf.length());
                    }
                }
            }

            if (toTermBuf.length() > 0) {
                boolean sendFlag = true;
                if (toTermBuf.toString().trim().equals(PROMPT)) {
                    if (prompted) {
                        sendFlag = false;
                    } else {
                        prompted = true;
                    }
                } else {
                    prompted = false;
                }
                if (sendFlag) {
                    char chars[] = new char[toTermBuf.length()];
                    toTermBuf.getChars(0, toTermBuf.length(), chars, 0);
                    toDTE.putChars(chars, 0, toTermBuf.length());
                    toTermBuf.delete(0, toTermBuf.length());
                }
            }
        }
    }
    private static final String PROMPT = "(gdb)"; // NOI18N

    private final RequestProcessor processingQueue = new RequestProcessor("GDB output processing", 1); // NOI18N

    private void dispatchInterceptedLines() {
        synchronized (interceptedLines) {
            while (!interceptedLines.isEmpty()) {
                final String line = interceptedLines.removeFirst();

                processingQueue.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            miProxy.processLine(line);
                        } catch (Exception e) {
                            Exceptions.printStackTrace(new Exception("when processing line: " + line, e)); //NOI18N
                        }
                    }
                });
            }
        }
    }
}
