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

import java.util.Collections;
import java.util.List;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * IR representation of {@code case} clause.
 * Case nodes are not {@link BreakableNode}s, but the {@link SwitchNode} is.
 */
public final class CaseNode extends Node implements Labels, Terminal {
    /** Test expression. */
    private final Expression test;

    /** Statements. */
    protected final List<Statement> statements;

    /** Case entry label. */
    private final Label entry;

    private final boolean terminal;

    /**
     * Constructors
     *
     * @param token    token
     * @param finish   finish
     * @param test     case test node, can be any node in JavaScript
     * @param statements case body statements
     */
    public CaseNode(final long token, final int finish, final Expression test, List<Statement> statements) {
        super(token, finish);

        this.test  = test;
        this.statements  = statements;
        this.entry = new Label("entry");
        this.terminal = isTerminal(statements);
    }

    CaseNode(final CaseNode caseNode, final int finish, final Expression test, final List<Statement> statements) {
        super(caseNode, finish);

        this.test  = test;
        this.statements = statements;
        this.entry = new Label(caseNode.entry);
        this.terminal = isTerminal(statements);
    }

    private static boolean isTerminal(List<Statement> statements) {
        return statements.isEmpty() ? false : statements.get(statements.size() - 1).hasTerminalFlags();
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterCaseNode(this)) {
            final Expression newTest = test == null ? null : (Expression)test.accept(visitor);
            List<Statement> newStatements = Node.accept(visitor, statements);
            return visitor.leaveCaseNode(setTest(newTest).setStatements(newStatements));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterCaseNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printTypes) {
        if (test != null) {
            sb.append("case ");
            test.toString(sb, printTypes);
            sb.append(':');
        } else {
            sb.append("default:");
        }
    }

    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Get the body for this case node
     * @return the body
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Get the entry label for this case node
     * @return the entry label
     */
    public Label getEntry() {
        return entry;
    }

    /**
     * Get the test expression for this case node
     * @return the test
     */
    public Expression getTest() {
        return test;
    }

    /**
     * Reset the test expression for this case node
     * @param test new test expression
     * @return new or same CaseNode
     */
    public CaseNode setTest(final Expression test) {
        if (this.test == test) {
            return this;
        }
        return new CaseNode(this, finish, test, statements);
    }

    public CaseNode setStatements(final List<Statement> statements) {
        if (this.statements == statements) {
            return this;
        }
        int lastFinish = 0;
        if (!statements.isEmpty()) {
            lastFinish = statements.get(statements.size() - 1).getFinish();
        }
        return new CaseNode(this, lastFinish, test, statements);
    }

    @Override
    public List<Label> getLabels() {
        return Collections.unmodifiableList(Collections.singletonList(entry));
    }
}
