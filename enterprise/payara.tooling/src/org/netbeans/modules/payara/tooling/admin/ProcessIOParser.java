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
package org.netbeans.modules.payara.tooling.admin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.CyclicStringBuffer;
import org.netbeans.modules.payara.tooling.utils.LinkedList;
import org.netbeans.modules.payara.tooling.utils.OsUtils;

/**
 * Parse process IO and verify it against content verification data.
 * <p/>
 * @author Tomas Kraus
 */
public class ProcessIOParser {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Parse process output.
     * <p/>
     * Process output is being read as text lines separated by prompt string,
     * CR or CRLF. Each finished line is searched for provided process
     * IO content.
     */
    protected static class Parser {

        /**
         * State machine input classes.
         */
        protected enum Input {
            /** Content of user, password hash or tool strings. */
            STRING,
            /** Separator character. */
            PROMPT,
            /** CR character, beginning of CRLF sequence. */
            CR,
            /** LF Character. */
            LF;

            /** Enumeration length. */
            protected static final int length = Input.values().length;
            
            /**
             * Get input class value for provided character.
             * <p/>
             * @param c Character to check tor  input class.
             * @return Input class of provided character.
             */
            protected static Input value(final char c, final String prompt,
                    final CyclicStringBuffer promptBuff) {
                if (prompt != null && promptBuff.equals(prompt)) {
                    return PROMPT;
                }
                switch (c) {
                    case '\r':
                        return CR;
                    case '\n':
                        return LF;
                    default:
                        return STRING;
                }
            }

        }

        /**
         * State machine internal states.
         */
        protected enum State {
            /** Initial state, expecting line 1st character. */
            START,
            /** Reading line characters until line separator or prompt. */
            LINE,
            /** Got '\r', expecting '\n' from EOL. */
            CR,
            /** Error state. */
            ERROR;

            /** Enumeration length. */
            protected static final int length = State.values().length;

            /** Transition table for [State, Input]. */
            protected static final State transition[][] = {
              // STRING  PROMPT    CR     LF
                { LINE,  START,    CR, START}, // START
                { LINE,  START,    CR, START}, // LINE
                { LINE,  START,    CR, START}, // CR
                {ERROR,  ERROR, ERROR, ERROR}  // ERROR
            };

            /**
             * State machine transition.
             * <p/>
             * @param s Current machine state.
             * @param i current input class.
             * @return Next machine state.
             */
            protected static State next(final State s, final Input i) {
                return transition[s.ordinal()][i.ordinal()];
            }

        }

        /** Content to verify on server administration command execution IO. */
        private final ProcessIOContent content;

        /** Content token to be verified. */
        private ProcessIOContent.Token token;

        /** Content verification result. */
        private ProcessIOResult result;

        /** Process input prompt length. Value of zero indicates no prompt. */
        private int promptLen;

        /** Current line being processed. */
        private final StringBuilder line;

        /** Cyclic buffer to compare input against prompt. */
        CyclicStringBuffer promptBuff;

        /** Machine internal state. */
        private State state;

        /** Process output log lines. */
        private final LinkedList<String> output;

        /**
         * Creates an instance of process output parser.
         * <p/>
         * @param content Content to verify on server administration command
         *                execution IO.
         */
        protected Parser(ProcessIOContent content) {
            this.content = content;
            line = new StringBuilder(BUFF_SIZE);
            state = State.START;
            token = this.content.firstToken();
            result = ProcessIOResult.UNKNOWN;
            String prompt = content.getCurrentPrompt();
            promptLen = prompt != null ? prompt.length() : 0;
            promptBuff = new CyclicStringBuffer(promptLen);
            output = new LinkedList();
        }

        /**
         * Parses content of process output.
         * <p/>
         * @param buff Buffer with incoming process standard output data.
         * @param len  Data length in process standard output buffer.
         */
        protected void parse(final char[] buff, final short len) {
            for (int pos = 0; pos < len; pos++) {
                state = action(buff[pos]);
            }
        }

        /**
         * Finish parsing when end of file was reached.
         */
        protected void finish() {
            endOfLine('\0');
        }

        /**
         * Get content verification result.
         * <p/>
         * @return Content verification result.
         */
        protected ProcessIOResult result() {
            return result;
        }

        /**
         * Run parser action based on current state and character class.
         * <p/>
         * @param c Current character being processed from {@link Reader} buffer.
         * @return Next state transition based on current state
         *         and character class.
         */
        protected State action(final char c) {
            Input cl = Input.value(c, content.getCurrentPrompt(), promptBuff);
            switch (state) {
                case START: switch (cl) {
                        case STRING:
                            firstChar(c);
                            break;
                        case PROMPT:
                            firstChar(c);
                        case LF:
                            endOfLine(c);
                    } break;
                case LINE: switch (cl) {
                        case STRING:
                            nextChar(c);
                            break;
                        case PROMPT:
                            nextChar(c);
                        case LF:
                            endOfLine(c);
                    } break;
                case CR: switch (cl) {
                        case STRING:
                            nextCharWithCR(c);
                            break;
                        case PROMPT:
                            nextCharWithCR(c);
                        case LF:
                            endOfLine(c);
                    } break;
            }
            return State.next(state, cl);
        }

        /**
         * Clear line content and append first character.
         * <p/>
         * @param c Current character from buffer.
         */
        protected void firstChar(final char c) {
            line.setLength(0);
            line.append(c);
        }

        /**
         * Append next character.
         * <p/>
         * @param c Current character from buffer.
         */
        protected void nextChar(final char c) {
            line.append(c);
        }

       /**
         * Append next character after CR.
         * <p/>
         * @param c Current character from buffer.
         */
        protected void nextCharWithCR(final char c) {
            line.append('\r');
            line.append(c);
        }

        /**
         * Handle end of line.
         * <p/>
         * @param c Current character from buffer (not used).
         */
        protected void endOfLine(final char c) {
            if (line.length() > 0) {
                output.addLast(line.toString());
            }
            if (token != null) {
                ProcessIOResult matchResult
                        = ProcessIOResult.UNKNOWN;
                for (int i = 0 ; i < line.length() ; i++) {
                    if ((matchResult = token.match(line, i))
                            != ProcessIOResult.UNKNOWN) {
                        token = content.nextToken();
                        String prompt = content.getCurrentPrompt();
                        promptLen = prompt != null ? prompt.length() : 0;
                        promptBuff.resize(promptLen);
                        break;
                    }                    
                }
                switch(matchResult) {
                    case SUCCESS:
                        if (result == ProcessIOResult.UNKNOWN) {
                            result = matchResult;
                        }
                        break;
                    case ERROR:
                        if (result != ProcessIOResult.ERROR) {
                            result = matchResult;
                        }
                        break;
                }
            }
            line.setLength(0);
        }

        /**
         * Build output string from stored process output lines.
         * <p/>
         * @return Process output string.
         */
        protected String getOutputString() {
            int len = 0;
            boolean isElement = output.first();
            while(isElement) {
                len += output.getCurrent().length();
                isElement = output.next();
                if (isElement) {
                    len += OsUtils.LINES_SEPARATOR.length();
                }
            }
            StringBuilder sb = new StringBuilder(len);
            isElement = output.first();
            while(isElement) {
                sb.append(output.getCurrent());
                isElement = output.next();
                if (isElement) {
                    sb.append(OsUtils.LINES_SEPARATOR);
                }
            }
            return sb.toString();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ProcessIOParser.class);

    /** Internal IO buffer size. */
    private static final short BUFF_SIZE = 128;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Process standard input. */
    private final Writer stdIn;

    /** Process standard output. */
    private final Reader stdOut;

    /** Process standard output parser. */
    private final Parser outParser;

    /** Buffer for incoming process standard output data. */
    private final char[] outBuff;

    /** Data length in process standard output buffer. */
    private short outLen;

    /** Vas process output verification already done? */
    private boolean verifydone;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of process IO verification parser.
     * <p/>
     * @param stdIn     Process standard input.
     * @param stdOut    Process standard output.
     * @param ioContent Content to verify on server administration command
     *                  execution IO.
     */
    public ProcessIOParser(final Writer stdIn, final Reader stdOut,
            final ProcessIOContent ioContent) {
        this.stdIn = stdIn;
        this.stdOut = stdOut;
        outParser = new Parser(ioContent);
        outBuff = new char[BUFF_SIZE];
        outLen = 0;
        verifydone = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verify process output streams against content verification data
     * provided in constructor as <code>ioContent</code> argument.
     * <p/>
     * @return Process output streams verification result.
     * @throws IOException When there is an issue with reading process
     *                     output streams.
     */
    public ProcessIOResult verify() throws IOException {
        while (outLen >= 0) {
            outLen = (short)stdOut.read(outBuff);
            outParser.parse(outBuff, outLen);
        }
        outParser.finish();
        verifydone = true;
        return outParser.result();
    }

    /**
     * Return process output as {@link String}.
     * <p/>
     * @return Process output as {@link String}.
     */
    public String getOutput() {
        final String METHOD = "getOutput";
        if (!verifydone) {
            throw new CommandException(LOGGER.excMsg(METHOD, "illegalState"));
        }
        return outParser.getOutputString();
    }
}
