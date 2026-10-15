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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// @formatter:off
/**
 * A loop node, for example a while node, do while node or for node
 */
public abstract class LoopNode extends BreakableStatement {
    /** loop continue label. */
    protected final Label continueLabel;

    /** Loop test node, null if infinite */
    protected final JoinPredecessorExpression test;

    /** Loop body */
    protected final Block body;

    /** Can control flow escape from loop, e.g. through breaks or continues to outer loops? */
    protected final boolean controlFlowEscapes;

    /**
     * Constructor
     *
     * @param lineNumber         lineNumber
     * @param token              token
     * @param finish             finish
     * @param body               loop body
     * @param test               test
     * @param controlFlowEscapes controlFlowEscapes
     */
    protected LoopNode(final int lineNumber, final long token, final int finish, final Block body, final JoinPredecessorExpression test, final boolean controlFlowEscapes) {
        super(lineNumber, token, finish, new Label("while_break"));
        this.continueLabel = new Label("while_continue");
        this.body = body;
        this.controlFlowEscapes = controlFlowEscapes;
        this.test = test;
    }

    /**
     * Constructor
     *
     * @param loopNode loop node
     * @param test     new test
     * @param body     new body
     * @param controlFlowEscapes controlFlowEscapes
     */
    protected LoopNode(final LoopNode loopNode, final JoinPredecessorExpression test, final Block body, final boolean controlFlowEscapes) {
        super(loopNode);
        this.continueLabel = new Label(loopNode.continueLabel);
        this.test = test;
        this.body = body;
        this.controlFlowEscapes = controlFlowEscapes;
    }

    @Override
    public boolean isTerminal() {
        if (!mustEnter()) {
            return false;
        }
        //must enter but control flow may escape - then not terminal
        if (controlFlowEscapes) {
            return false;
        }
        //must enter, but body ends with return - then terminal
        if (body.isTerminal()) {
            return true;
        }
        //no breaks or returns, it is still terminal if we can never exit
        return test == null;
    }

    /**
     * Conservative check: does this loop have to be entered?
     * @return true if body will execute at least once
     */
    public abstract boolean mustEnter();

    /**
     * Get the continue label for this while node, i.e. location to go to on continue
     * @return continue label
     */
    public Label getContinueLabel() {
        return continueLabel;
    }

    @Override
    public List<Label> getLabels() {
        return Collections.unmodifiableList(Arrays.asList(breakLabel, continueLabel));
    }

    @Override
    public boolean isLoop() {
        return true;
    }

    /**
     * Get the body for this for node
     * @return the body
     */
    public abstract Block getBody();

    /**
     * @param lc   lexical context
     * @param body new body
     * @return new for node if changed or existing if not
     */
    public abstract LoopNode setBody(final LexicalContext lc, final Block body);

    /**
     * Get the test for this for node
     * @return the test
     */
    public final JoinPredecessorExpression getTest() {
        return test;
    }

    /**
     * Set the test for this for node
     *
     * @param lc lexical context
     * @param test new test
     * @return same or new node depending on if test was changed
     */
    public abstract LoopNode setTest(final LexicalContext lc, final JoinPredecessorExpression test);

    /**
     * Set the control flow escapes flag for this node.
     * TODO  - integrate this with Lowering in a better way
     *
     * @param lc lexical context
     * @param controlFlowEscapes control flow escapes value
     * @return new loop node if changed otherwise the same
     */
    public abstract LoopNode setControlFlowEscapes(final LexicalContext lc, final boolean controlFlowEscapes);

    /**
     * Does this loop have a LET declaration and hence require a per-iteration scope?
     * @return true if a per-iteration scope is required.
     */
    public abstract boolean hasPerIterationScope();
}
