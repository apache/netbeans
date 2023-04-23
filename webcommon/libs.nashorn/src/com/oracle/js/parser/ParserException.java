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


/**
 * ECMAScript parser exceptions.
 */
@SuppressWarnings("serial")
public final class ParserException extends RuntimeException {
    // script file name
    private String fileName;
    // script line number
    private int line;
    // script column number
    private int column;

    // Source from which this ParserException originated
    private final Source source;
    // token responsible for this exception
    private final long token;
    // if this is translated as ECMA error, which type should be used?
    private final JSErrorType errorType;

    /**
     * Constructor.
     *
     * @param msg exception message for this parser error.
     */
    public ParserException(final String msg) {
        this(JSErrorType.SyntaxError, msg, null, -1, -1, -1);
    }

    /**
     * Constructor.
     *
     * @param errorType error type
     * @param msg exception message
     * @param source source from which this exception originates
     * @param line line number of exception
     * @param column column number of exception
     * @param token token from which this exception originates
     *
     */
    public ParserException(final JSErrorType errorType, final String msg, final Source source, final int line, final int column, final long token) {
        super(msg);
        this.fileName = source != null ? source.getName() : null;
        this.line = line;
        this.column = column;
        this.source = source;
        this.token = token;
        this.errorType = errorType;
    }

    /**
     * Get the source file name for this {@code ParserException}.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the source file name for this {@code ParserException}.
     *
     * @param fileName the file name
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the line number for this {@code ParserException}.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * Set the line number for this {@code ParserException}.
     *
     * @param line the line number
     */
    public void setLineNumber(final int line) {
        this.line = line;
    }

    /**
     * Get the column for this {@code ParserException}.
     *
     * @return the column number
     */
    public int getColumnNumber() {
        return column;
    }

    /**
     * Set the column for this {@code ParserException}.
     *
     * @param column the column number
     */
    public void setColumnNumber(final int column) {
        this.column = column;
    }

    /**
     * Get the {@code Source} of this {@code ParserException}.
     *
     * @return source
     */
    public Source getSource() {
        return source;
    }

    /**
     * Get the token responsible for this {@code ParserException}.
     *
     * @return token
     */
    public long getToken() {
        return token;
    }

    /**
     * Get token position within source where the error originated.
     *
     * @return token position if available, else -1
     */
    public int getPosition() {
        return Token.descPosition(token);
    }

    /**
     * Get the {@code JSErrorType} of this {@code ParserException}.
     *
     * @return error type
     */
    public JSErrorType getErrorType() {
        return errorType;
    }
}
