/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.terminalemulator.TermStream;

/**
 * simple key processing for gdb console
 */
final class KeyProcessing {

    static TermStream createStream() {
        return new KeyProcessingStream();
    }

    private KeyProcessing() {
    }

    static final class ESCAPES {

        // move caret to start/end of line
        private static final char CHAR_SOH = (char) 1; // ^A ASCII Start of heading
        private static final char CHAR_ENQ = (char) 5; // ^E ASCII Enquiry

        private static final char CHAR_BS = (char) 8; // ^H ASCII BackSpace
        static final char CHAR_LF = (char) 10; // ^J ASCII LineFeed
        static final char CHAR_CR = (char) 13; // ^M ASCII CarriageReturn
        static final char CHAR_ESC = (char) 27; // ^[ ASCII ESCape
        private static final char CHAR_SP = (char) 32; // ASCII SPace
        // remove end/start of line
        private static final char CHAR_VT = (char) 11; // ^K ASCII Vertical tab
        private static final char CHAR_NAK = (char) 21; // ^U ASCII NegativeAcknowledge
        private static final char[] BS_SEQUENCE = {CHAR_BS, CHAR_SP, CHAR_BS};
        // erase to the start/end of line sequence
        private static final char[] BS_SOL_SEQUENCE = {CHAR_ESC, '[', '1', 'K'};
        // erase the entire line
        private static final char[] BS_EL_SEQUENCE = {CHAR_ESC, '[', '2', 'K'};
        private static final char[] BS_EOL_SEQUENCE = {CHAR_ESC, '[', 'K'};
        // erase specified amount of symbols
        private static final String DEL_SEQUENCE_FMT = "\033[%dP"; // NOI18N

        static final char[] BOLD_SEQUENCE = {CHAR_ESC, '[', '1', 'm'};
        static final char[] BLUEBOLD_SEQUENCE = {CHAR_ESC, '[', '1', ';', '3', '4', 'm'};
        static final char[] RED_SEQUENCE = {CHAR_ESC, '[', '3', '1', 'm'};
        // used as cutoms colors: 0, 1, 2 set up as ioPack.console().getTerm().setCustomColor above
        static final char[] BROWN_SEQUENCE = {CHAR_ESC, '[', '5', '0', 'm'};
        static final char[] GREEN_SEQUENCE = {CHAR_ESC, '[', '5', '1', 'm'};
        static final char[] LOG_SEQUENCE = {CHAR_ESC, '[', '5', '2', 'm'};

        static final char[] RESET_SEQUENCE = {CHAR_ESC, '[', '0', 'm'};

        // pressed DEL key (as sequence)
        private static final String DEL_SEQUENCE = "\033[3~";  // NOI18N
        // navigation in line (as sequence)
        private static final String UP_SEQUENCE = "\033[A";  // NOI18N
        private static final String DOWN_SEQUENCE = "\033[B";  // NOI18N
        private static final String RIGHT_SEQUENCE = "\033[C";  // NOI18N
        private static final String LEFT_SEQUENCE = "\033[D";  // NOI18N
        private static final String HOME_SEQUENCE = "\033[H";  // NOI18N
        private static final String END_SEQUENCE = "\033[F";  // NOI18N

        // as char array
        private static final char[] RIGHT_SEQUENCE_CHARS = ESCAPES.RIGHT_SEQUENCE.toCharArray();
    }

    private static class KeyProcessingStream extends TermStream {

        private static final Logger STREAM_LOG = Logger.getLogger(Gdb.class.getName());
        private static final char FIRST_PRINTABLE = ' '; // NOI18N
        private final History history = new History();
        private boolean resendingLastCommand = false;

        private static class History {

            private static final int MAX_SIZE = 100;
            private final LinkedList<String> list;
            private ListIterator<String> iter;

            private boolean forward = false;
            private boolean lastIsTmp = false;

            public History() {
                list = new LinkedList<String>();
                iter = list.listIterator();
            }

            public void add(final String string) {
                if (lastIsTmp) {
                    list.removeLast();
                    lastIsTmp = false;
                } else {
                    if (list.size() > MAX_SIZE) {
                        list.removeFirst();
                    }
                }
                list.addLast(string);
                iter = list.listIterator(list.size());
                forward = false;
            }

            public String previous() {
                if (forward && iter.hasPrevious()) {
                    iter.previous();
                }
                forward = false;
                if (iter.hasPrevious()) {
                    return iter.previous();
                } else {
                    return (!list.isEmpty())
                            ? list.getFirst()
                            : "";
                }
            }

            /*package*/ String previous(final String current) {
                if (isLast()) {
                    add(current);
                    lastIsTmp = true;
                    previous();
                }
                return previous();
            }

            public String next() {
                if (!forward && iter.hasNext()) {
                    iter.next();
                }
                forward = true;
                String val = "";
                if (iter.hasNext()) {
                    val = iter.next();
                }
                if (isLast() && lastIsTmp) {
                    removeLast();
                    lastIsTmp = false;
                }
                return val;
            }

            public boolean isLast() {
                return !iter.hasNext();
            }

            public String getLast() {
                return (!list.isEmpty())
                        ? list.getLast()
                        : "";
            }

            // iterator must be on the last index
            private void removeLast() {
                assert !iter.hasNext() : "The iterator must be on the last index";
                list.removeLast();
                iter = list.listIterator(list.size());
            }
        }

        private final StringBuilder line = new StringBuilder();
        private int charIdxInLine;
        private int send_buf_sz = 2;
        private char[] send_buf = new char[send_buf_sz];

        private char[] send_buf(int n) {
            if (n >= send_buf_sz) {
                send_buf_sz = n + 1;
                send_buf = new char[send_buf_sz];
            }
            return send_buf;
        }

        @Override
        public void sendChars(char[] c, int offset, int count) {
            STREAM_LOG.log(Level.FINE, "sendChars from term: \"{0}\"", Arrays.toString(c));
            // check known sequences
            boolean consumed = processCharSequences(c, offset, count);
            if (consumed) {
                toDTE.flush();
                STREAM_LOG.log(Level.FINE, "processCharSequences: line in term: \"{0}\"", line);
                return;
            }
            // handle per character
            for (int cx = 0; cx < count; cx++) {
                sendCharImpl(c[offset + cx]);
            }
            toDTE.flush();
            STREAM_LOG.log(Level.FINE, "sendChars: line in term: \"{0}\"", line);
        }

        @Override
        public void sendChar(char c) {
            STREAM_LOG.log(Level.FINE, "sendChar from term: \"{0}:{1}\"", new Object[]{c, (int) c});
            sendCharImpl(c);
            toDTE.flush();
            STREAM_LOG.log(Level.FINE, "sendChar: line in term: \"{0}\"", line);
        }

        @Override
        public void putChar(char c) {
            toDTE.putChar(c);
        }

        @Override
        public void flush() {
            toDTE.flush();
        }

        @Override
        public void putChars(char[] buf, int offset, int count) {
            toDTE.putChars(buf, offset, count);
        }

        private boolean processCharSequences(char[] chars, int offset, int count) {
            String seq = String.valueOf(chars, offset, count);

            if (ESCAPES.UP_SEQUENCE.equals(seq)) {
                historyUp();
            } else if (ESCAPES.DOWN_SEQUENCE.equals(seq)) {
                historyDown();
            } else if (ESCAPES.LEFT_SEQUENCE.equals(seq)) {
                moveCaretLeft(charIdxInLine - 1);
            } else if (ESCAPES.RIGHT_SEQUENCE.equals(seq)) {
                moveCaretRight(charIdxInLine + 1);
            } else if (ESCAPES.HOME_SEQUENCE.equals(seq)) {
                moveCaretLeft(0);
            } else if (ESCAPES.END_SEQUENCE.equals(seq)) {
                moveCaretRight(line.length());
            } else if (ESCAPES.DEL_SEQUENCE.equals(seq)) {
                // remove symbol
                removeRight(1);
            } else {
                return false;
            }
            return true;
        }

        private void sendCharImpl(char c) {
            if (c == ESCAPES.CHAR_CR || c == ESCAPES.CHAR_LF) {
                if (!resendingLastCommand && line.toString().trim().length() == 0) {
                    // if line is empty, repeat the last command
                    String last = history.getLast();
                    int nchars = last.length() + 1;
                    char[] tmp = send_buf(nchars);
                    last.getChars(0, nchars - 1, tmp, 0);
                    tmp[nchars - 1] = c;

                    cleanLine();

                    resendingLastCommand = true;
                    sendChars(tmp, 0, nchars);
                    resendingLastCommand = false;
                } else {
                    toDTE.putChar(ESCAPES.CHAR_CR);
                    toDTE.putChar(ESCAPES.CHAR_LF);
                    toDTE.flush();
                    if (!resendingLastCommand) {
                        history.add(line.toString());
                    }
                    line.append('\n');
                    int nchars = line.length();
                    char[] tmp = send_buf(nchars);
                    line.getChars(0, nchars, tmp, 0);
                    line.delete(0, nchars);
                    charIdxInLine = 0;
                    toDCE.sendChars(tmp, 0, nchars);
                    toDCE.flush();
                }
            } else if (c == ESCAPES.CHAR_SOH) {
                moveCaretLeft(0);
            } else if (c == ESCAPES.CHAR_ENQ) {
                moveCaretRight(line.length());
            } else if (c == ESCAPES.CHAR_BS) {
                // BS
                if (charIdxInLine == 0) {
                    return;
                }
                removeLeft(1);
                printPostfixKeepCaret();
            } else if (c == ESCAPES.CHAR_VT) {
                // remove till the end of line
                removeRight(line.length() - charIdxInLine);
            } else if (c == ESCAPES.CHAR_NAK) {
                // remove till the start of line
                if (charIdxInLine == 0) {
                    return;
                }
                removeLeft(charIdxInLine);
                assert charIdxInLine == 0 : "" + charIdxInLine;
                // print postfix if middle of the line
                printPostfixKeepCaret();
            } else if (c < FIRST_PRINTABLE) {
            } else {
                // update line content

                if (charIdxInLine == line.length()) {
                    // end of line
                    line.append(c);
                } else {
                    assert charIdxInLine < line.length() : charIdxInLine + "vs. " + line.length();
                    // new line content
                    line.insert(charIdxInLine, c);
                }
                // print in term
                toDTE.putChar(c);
                charIdxInLine++;
                // print postfix if middle of the line
                printPostfixKeepCaret();
            }
        }

        private void removeRight(int count) {
            if (charIdxInLine == line.length()) {
                return;
            }
            assert count > 0;
            // special case to remove end of line
            if (charIdxInLine + count == line.length()) {
                toDTE.putChars(ESCAPES.BS_EOL_SEQUENCE, 0, ESCAPES.BS_EOL_SEQUENCE.length);
                line.delete(charIdxInLine, line.length());
                return;
            }
            int nrTermPositions = 0;
            while (count-- > 0) {
                assert charIdxInLine > 0 : "unexpected " + charIdxInLine;
                char erased_char = line.charAt(charIdxInLine);
                line.deleteCharAt(charIdxInLine);
                nrTermPositions += getTerm().charWidth(erased_char);
            }
            deleteRightImpl(nrTermPositions);
        }

        private void removeLeft(int count) {
            // special case to erase till start of line
            if (false && count == charIdxInLine) {
                toDTE.putChars(ESCAPES.BS_SOL_SEQUENCE, 0, ESCAPES.BS_SOL_SEQUENCE.length);
                toDTE.putChar(ESCAPES.CHAR_CR);
                charIdxInLine = 0;
                return;
            }
            int nrTermPositions = 0;
            while (count-- > 0) {
                assert charIdxInLine > 0 : "unexpected " + charIdxInLine;
                charIdxInLine--;
                char erased_char = line.charAt(charIdxInLine);
                line.deleteCharAt(charIdxInLine);
                nrTermPositions += getTerm().charWidth(erased_char);
            }
            if (nrTermPositions == 1 && charIdxInLine == line.length()) {
                // special case for one backspace at the end of line
                toDTE.putChars(ESCAPES.BS_SEQUENCE, 0, ESCAPES.BS_SEQUENCE.length);
            } else if (nrTermPositions > 0) {
                // move left
                int move = nrTermPositions;
                while (move-- > 0) {
                    toDTE.putChar(ESCAPES.CHAR_BS);
                }
                deleteRightImpl(nrTermPositions);
            }
        }

        private void cleanLine() {
            line.delete(0, line.length());
            charIdxInLine = 0;
            toDTE.putChars(ESCAPES.BS_EL_SEQUENCE, 0, ESCAPES.BS_EL_SEQUENCE.length);
            toDTE.putChar(ESCAPES.CHAR_CR);
        }

        private void deleteRightImpl(int nrTermPositions) {
            assert nrTermPositions > 0;
            // prepare DEL sequence
            String delSeq = String.format(ESCAPES.DEL_SEQUENCE_FMT, nrTermPositions);
            // send DEL
            int nchars = delSeq.length();
            char[] tmp = send_buf(nchars);
            delSeq.getChars(0, nchars, tmp, 0);
            toDTE.putChars(tmp, 0, nchars);
        }

        private void printPostfixKeepCaret() {
            if (charIdxInLine < line.length()) {
                int oldCaretIdx = charIdxInLine;
                while (charIdxInLine < line.length()) {
                    toDTE.putChar(line.charAt(charIdxInLine++));
                }
                // adjust caret index
                moveCaretLeft(oldCaretIdx);
            }
        }

        private void moveCaretRight(int requestedIdx) {
            if (requestedIdx <= line.length()) {
                while (charIdxInLine < requestedIdx) {
                    int cwidth = getTerm().charWidth(line.charAt(charIdxInLine));
                    charIdxInLine++;
                    while (cwidth-- > 0) {
                        toDTE.putChars(ESCAPES.RIGHT_SEQUENCE_CHARS, 0, ESCAPES.RIGHT_SEQUENCE_CHARS.length);
                    }
                }
                assert requestedIdx == charIdxInLine : charIdxInLine + " different from " + requestedIdx;
            }
        }

        private void moveCaretLeft(int requestedIdx) {
            if (requestedIdx == 0) {
                // just carriage return
                toDTE.putChar(ESCAPES.CHAR_CR);
                charIdxInLine = 0;
            } else if (requestedIdx >= 0) {
                while (requestedIdx < charIdxInLine) {
                    charIdxInLine--;
                    int cwidth = getTerm().charWidth(line.charAt(charIdxInLine));
                    while (cwidth-- > 0) {
                        toDTE.putChar(ESCAPES.CHAR_BS);
                    }
                }
                assert requestedIdx == charIdxInLine : charIdxInLine + " different from " + requestedIdx;
            }
        }

        private void historyUp() {
            String str = line.toString();
            cleanLine();
            String prev = history.previous(str);

            line.append(prev);
            toDTE.putChars(prev.toCharArray(), 0, prev.length());
            charIdxInLine = prev.length();
        }

        private void historyDown() {
            cleanLine();
            String next = history.next();

            line.append(next);
            toDTE.putChars(next.toCharArray(), 0, next.length());
            charIdxInLine = next.length();
        }
    }
}
