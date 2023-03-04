/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.terminalemulator.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermStream;

/**
 * Allow filtering of lines being sent to a Term and installing of
 * hyperlinks.
 * <p>
 * <h3>Typical usage:</h3>
 * <pre>
 * LineFilter lineFilter = new LineFilter() {
 *     public void processLine(String line, LineSink sink) {
 *         sink.forwardLine(line);
 *     }
 * };
 * int delay = 100;
 * LineFilter.pushInto(lineFilter, term, delay);
 * </pre>
 * A line may be dropped, by not calling sink.forwardLine() or it may
 * be multiplied, by calling sink.forwardLine() more than once.
 * <p>
 * <h3>Hyperlinks</h3>
 * Hyperlinks will only work if ActiveTerm and the dtterm interpreter is used:
 * <pre>
 * ActiveTerm term = ...
 * term.setEmulation("dtterm");
 * </pre>
 * <p>
 * A common application of LineFilter is to convert certain text patterns,
 * like error messages, into hyperlinks. For example the following 
 * implementation of processLine will convert a typical compiler error
 * message file:line prefix into a hyperlink:
 * <pre>
 *     public void processLine(String line, LineSink sink) {
 *         Pattern errPattern = Pattern.compile("(.*:\\d+)(: .*)");
 *         Matcher  errMatcher = errPattern.matcher(line);
 *         if (errMatcher.find()) {
 *             String location = errMatcher.group(1);
 *             String rest = errMatcher.group(2);
 * 
 *             StringBuilder buf = new StringBuilder();
 *             buf.append(hyperlink(location, location));
 *             buf.append(rest);
 *             sink.forwardLine(buf.toString());
 * 
 *         } else {
 *             sink.forwardLine(line);
 *         }
 *     }
 * </pre>
 * Then to process clicks on the hyperlink do this:
 * <pre>
 * term.setActionListener(new ActiveTermListener() {
 *     public void action(ActiveRegion r, InputEvent e) {
 *         if (r.isLink()) {
 *             String clientData = (String) r.getUserObject();
 *         }
 *     }
 * };
 * </pre>
 * <h3>Line buffering</h3>
 * LineFilter is in general <u>not</u> suitable for processing of interactive
 * i/o. That is because it doesn't call processLine() until an end of 
 * line is detected. As a result the prompt of a shell, or even the 
 * echoing of user-entered characters will not appear promptly.
 * <p>
 * A simple, but not particularly good, remedy is to use a timer to
 * flush output if no end of line is detected. This is controlled by
 * the delay parameter passed to pushInto().
 * If it is set to 0 the timer is not used.
 * <p>
 * The use of this timer is <u>only</u> recommended for Terms which
 * are dedicated to processing batch output, like that of a build.
 *
 * <h3>End of line (EOL) characters</h3>
 * LineFilter will consider any of '\r', '\n' or "\r\n" as line terminators;
 * same as BufferedReader.readLine().
 * <br>
 * These characters are <u>not</u> appended to the line passed to processLine()
 * and need not be appended to the line passed to forwardLine().
 * <br> 
 * Because we're outputting to a raw terminal the "line.separator" System
 * property is irrelevant and forwardLine() always tacks on a "\r\n" 
 * unless the line is being processed due to timer expiration.
 * <p>
 * This general recognition of EOL is <u>also</u> not suitable for interactive
 * applications. For example, a shell with cmdline editing or a readline based
 * application will output a sole '\r' when cmdline editing is invoked. Should
 * it really be considered as a line terminator?
 *
 * <h3>Efficiency</h3>
 * LineFilter is <u>not</u> suitable if there is a large amount of pattern
 * recognition to be done. This is because it processes characters downstream
 * from {@link Term#putChars} which, until Term becomes multi-threaded, has
 * to be called on the AWT event dispatch thread.
 * <p>
 * However, all is not lost. One can still insert hyperlinks into Term as
 * described in {@link LineFilter#hyperlink} using an external pattern
 * processor like sed, awk or perl and handle them as shown above using
 * {@link org.netbeans.lib.terminalemulator.ActiveTermListener}.
 * <br>
 * In fact this is the recommended way for using hyperlinks in Term.
 *
 * @author ivan
 */
public abstract class LineFilter {

    /**
     * Sink for processed lines.
     * Line should <u>not</u> be terminated with '\r' or '\n'.
     */
    protected static interface LineSink {
        public void forwardLine(String line);
    };


    /**
     * Called whenever any of '\r', '\n' or "\r\n" is detected <u>or</u> if
     * the timer expires. 'line' will not contain any of the line termination
     * characters.
     * @param line Line to be processed.
     * @param lineSink Forward the processed line to this lineSink.
     */
    public abstract void processLine(String line, LineSink lineSink);

    /**
     * Wrap the clientData (think of it as a URL) and text into the
     * appropriate escape sequences as understood by Term I.e.
     * <pre>
     *      &lt;ESC&gt;]10;&lt;clientData&gt;;&lt;text&gt;&lt;BEL&gt;
     * </pre>
     * which is analogous to 
     * <pre>
     *      &lt;a href="clientData"&gt;text&lt;/a&gt;
     * </pre>
     * <br>
     * clientData will be accessible as the userObject property of an 
     * {@link org.netbeans.lib.terminalemulator.ActiveRegion}.
     * @param clientData
     * @param text
     * @return Wrapped string
     */
    protected final String hyperlink(String clientData, String text) {
        StringBuilder buf = new StringBuilder();
        buf.append((char) 27); // ESC
        buf.append("]10;");			// NOI18N
        buf.append(clientData);
        buf.append(";");			// NOI18N
        buf.append(text);
        buf.append((char) 7); // BEL
        return buf.toString();
    }

    /**
     * Install lf into term.
     * @param lf LineFilter to install.
     * @param term Term to install line filter into.
     * @param delayMillis How much to wait after each character for an
     * EOL before giving up and processing a line.
     * *<br>
     * 0 disables the timer.
     */
    public static void pushInto(LineFilter lf, Term term, int delayMillis) {
        LineProcessorBridge lpb = new LineProcessorBridge(lf, delayMillis);
        term.pushStream(lpb);
    }

    /**
     * A TermStream which collects lines and calls processLine().
     */
    private static class LineProcessorBridge extends TermStream
                                             implements LineSink, ActionListener {

        private final LineFilter lineProcessor;     // ... to call
        private final Timer timer;

        // This sequence is not system dependent because we're sending to a
        // raw terminal which always requires both.
        private final char[] eol = new char[] {'\n', '\r'};

        private StringBuilder buf = new StringBuilder();

        private boolean ignoreNextCR;   // AKA "we've seen a '\r'"
        private boolean fullLine;       // true if processLine called on a EOL
                                        // as opposed to on the timer.
        public LineProcessorBridge(LineFilter lineProcessor, int delay) {
            super();

            if (delay == 0)
                this.timer = null;
            else {
                this.timer = new Timer(delay, this);
                timer.setRepeats(false);
            }
            this.lineProcessor = lineProcessor;
        }

        @Override
        public void flush() {
            toDTE.flush();
        }

        @Override
        public void putChar(char c) {
            processChar(c);
        }

        @Override
        public void putChars(char[] buf, int offset, int count) {
            for (int bx = 0; bx < count; bx++) {
                processChar(buf[offset + bx]);
            }
        }

        @Override
        public void sendChar(char c) {
            toDCE.sendChar(c);
        }

        @Override
        public void sendChars(char[] c, int offset, int count) {
            toDCE.sendChars(c, offset, count);
        }

        private void processChar(char c) {
            if (c == '\n') {
                if (ignoreNextCR) {
                    ignoreNextCR = false;
                } else {
                    doProcessLine(buf.toString(), true);
                }
            } else if (c == '\r') {
                ignoreNextCR = true;
                doProcessLine(buf.toString(), true);
            } else {
                ignoreNextCR = false;
                buf.append(c);
                if (timer != null)
                    timer.restart();
            }
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            // Called when timer fires
            doProcessLine(buf.toString(), false);
        }

        private void doProcessLine(String line, boolean fullLine) {
            this.fullLine = fullLine;
            lineProcessor.processLine(line, this);
            buf = new StringBuilder();
        }

        /**
         * Implementation of LineSink.forwardLine.
         * @param line
         */
	@Override
        public void forwardLine(String line) {
            toDTE.putChars(line.toCharArray(), 0, line.length());
            if (fullLine)
                toDTE.putChars(eol, 0, eol.length);
        }
    }
}
