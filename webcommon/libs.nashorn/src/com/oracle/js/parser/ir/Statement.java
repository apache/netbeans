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

package com.oracle.js.parser.ir;

// @formatter:off
/**
 * Statement is something that becomes code and can be stepped past. A block is
 * made up of statements. The only node subclass that needs to keep token and
 * location information is the Statement
 */
public abstract class Statement extends Node implements Terminal {

    private final int lineNumber;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     */
    public Statement(final int lineNumber, final long token, final int finish) {
        super(token, finish);
        this.lineNumber = lineNumber;
    }

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param start      start
     * @param finish     finish
     */
    protected Statement(final int lineNumber, final long token, final int start, final int finish) {
        super(token, start, finish);
        this.lineNumber = lineNumber;
    }

    /**
     * Copy constructor
     *
     * @param node source node
     */
    protected Statement(final Statement node) {
        super(node);
        this.lineNumber = node.lineNumber;
    }

    /**
     * Return the line number
     * @return line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Is this a terminal statement, i.e. does it end control flow like a throw or return?
     *
     * @return true if this node statement is terminal
     */
    @Override
    public boolean isTerminal() {
        return false;
    }

    /**
     * Check if this statement repositions control flow with goto like
     * semantics, for example {@link BreakNode} or a {@link ForNode} with no test
     * @return true if statement has goto semantics
     */
    public boolean hasGoto() {
        return false;
    }

    /**
     * Check if this statement has terminal flags, i.e. ends or breaks control flow
     *
     * @return true if has terminal flags
     */
    public final boolean hasTerminalFlags() {
        return isTerminal() || hasGoto();
    }
}
