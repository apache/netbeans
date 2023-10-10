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

import java.util.ArrayList;
import java.util.List;

import com.oracle.js.parser.ir.Statement;

/**
 * Base class for parser context nodes
 */
abstract class ParserContextBaseNode implements ParserContextNode {
    /**
     * Flags for this node
     */
    protected int flags;

    private List<Statement> statements;

    /**
     * Constructor
     */
    ParserContextBaseNode() {
        this.statements = new ArrayList<>();
    }

    /**
     * @return The flags for this node
     */
    @Override
    public int getFlags() {
        return flags;
    }

    /**
     * Returns a single flag
     *
     * @param flag
     * @return A single flag
     */
    protected int getFlag(final int flag) {
        return (flags & flag);
    }

    /**
     * @param flag
     * @return the new flags
     */
    @Override
    public int setFlag(final int flag) {
        flags |= flag;
        return flags;
    }

    /**
     * @return The list of statements that belongs to this node
     */
    @Override
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * @param statements
     */
    @Override
    public void setStatements(final List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * Adds a Statement at the end of the Statementlist
     *
     * @param statement The statement to add
     */
    @Override
    public void appendStatement(final Statement statement) {
        this.statements.add(statement);
    }

    /**
     * Adds a statement at the begining of the statementlist
     *
     * @param statement The statement to add
     */
    @Override
    public void prependStatement(final Statement statement) {
        this.statements.add(0, statement);
    }
}
