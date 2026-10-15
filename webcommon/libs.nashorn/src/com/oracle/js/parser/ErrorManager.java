/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser;

import java.io.PrintWriter;

/**
 * Handles JavaScript error reporting.
 */
public abstract class ErrorManager {
    // TODO - collect and sort/collapse error messages.
    // TODO - property based error messages.
    /** Error count. */
    private int errors;

    /** Warning count */
    private int warnings;

    /** Limit of the number of messages. */
    private int limit;

    /** Treat warnings as errors. */
    private boolean warningsAsErrors;

    /** First thrown ParserException. */
    private ParserException parserException;

    /**
     * Constructor
     */
    protected ErrorManager() {
        this.limit = 100;
        this.warningsAsErrors = false;
    }

    /**
     * Check to see if number of errors exceed limit.
     */
    private void checkLimit() {
        int count = errors;

        if (warningsAsErrors) {
            count += warnings;
        }

        if (limit != 0 && count > limit) {
            // throw rangeError("too.many.errors", Integer.toString(limit));
            throw new RuntimeException("too many errors");
        }
    }

    /**
     * Format an error message to include source and line information.
     *
     * @param message Error message string.
     * @param source Source file information.
     * @param line Source line number.
     * @param column Source column number.
     * @param token Offending token descriptor.
     * @return formatted string
     */
    public static String format(final String message, final Source source, final int line, final int column, final long token) {
        final String eoln = System.lineSeparator();
        final int position = Token.descPosition(token);
        final StringBuilder sb = new StringBuilder();

        // Source description and message.
        sb.append(source.getName()).append(':').append(line).append(':').append(column).append(' ').append(message).append(eoln);

        // Source content.
        final String sourceLine = source.getSourceLine(position);
        sb.append(sourceLine).append(eoln);

        // Pointer to column.
        for (int i = 0; i < column; i++) {
            if (sourceLine.charAt(i) == '\t') {
                sb.append('\t');
            } else {
                sb.append(' ');
            }
        }

        sb.append('^');
        // Use will append eoln.
        // buffer.append(eoln);

        return sb.toString();
    }

    /**
     * Report an error or warning message.
     *
     * @param message Error message string.
     */
    protected void message(final String message) {
    }

    /**
     * Report an error using information provided by the ParserException
     *
     * @param e ParserException object
     */

    public void error(final ParserException e) {
        if (this.parserException == null) {
            this.parserException = e;
        }
        error(e.getMessage());
    }

    /**
     * Report an error message provided
     *
     * @param message Error message string.
     */
    public void error(final String message) {
        message(message);
        errors++;
        checkLimit();
    }

    /**
     * Report a warning using information provided by the ParserException
     *
     * @param e ParserException object
     */
    public void warning(final ParserException e) {
        warning(e.getMessage());
    }

    /**
     * Report a warning message provided
     *
     * @param message Error message string.
     */
    public void warning(final String message) {
        message(message);
        warnings++;
        checkLimit();
    }

    /**
     * Test to see if errors have occurred.
     *
     * @return True if errors.
     */
    public boolean hasErrors() {
        return errors != 0;
    }

    /**
     * Get the message limit
     *
     * @return max number of messages
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the message limit
     *
     * @param limit max number of messages
     */
    public void setLimit(final int limit) {
        this.limit = limit;
    }

    /**
     * Check whether warnings should be treated like errors
     *
     * @return true if warnings should be treated like errors
     */
    public boolean isWarningsAsErrors() {
        return warningsAsErrors;
    }

    /**
     * Set warnings to be treated as errors
     *
     * @param warningsAsErrors true if warnings should be treated as errors, false otherwise
     */
    public void setWarningsAsErrors(final boolean warningsAsErrors) {
        this.warningsAsErrors = warningsAsErrors;
    }

    /**
     * Get the number of errors
     *
     * @return number of errors
     */
    public int getNumberOfErrors() {
        return errors;
    }

    /**
     * Get number of warnings
     *
     * @return number of warnings
     */
    public int getNumberOfWarnings() {
        return warnings;
    }

    public ParserException getParserException() {
        return parserException;
    }

    /**
     * {@link ErrorManager} that reports to a {@link PrintWriter}.
     */
    public static class PrintWriterErrorManager extends ErrorManager {
        /** Reporting writer. */
        private final PrintWriter writer;

        /**
         * Constructor. Reports to {@link System#err}.
         */
        public PrintWriterErrorManager() {
            this(new PrintWriter(System.err, true));
        }

        /**
         * Constructor.
         *
         * @param writer I/O writer to report on.
         */
        public PrintWriterErrorManager(final PrintWriter writer) {
            this.writer = writer;
        }

        @Override
        protected void message(String message) {
            writer.println(message);
        }
    }

    /**
     * {@link ErrorManager} that reports to a {@link StringBuilder}.
     */
    public static class StringBuilderErrorManager extends ErrorManager {
        private final StringBuilder buffer = new StringBuilder(0);

        public StringBuilderErrorManager() {
        }

        @Override
        protected void message(String message) {
            buffer.append(message).append('\n');
        }

        public String getOutput() {
            return buffer.toString();
        }
    }

    /**
     * ThrowErrorManager that throws ParserException upon error conditions.
     */
    public static class ThrowErrorManager extends ErrorManager {
        @Override
        public void error(final String message) {
            throw new ParserException(message);
        }

        @Override
        public void error(final ParserException e) {
            throw e;
        }

        @Override
        protected void message(String message) {
            System.err.println(message);
        }
    }
}
