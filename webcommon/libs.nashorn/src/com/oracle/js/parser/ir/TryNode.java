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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * IR representation of a {@code try} statement.
 */
public final class TryNode extends Statement {
    /** Try statements. */
    private final Block body;

    /** List of catch clauses. */
    private final List<Block> catchBlocks;

    /** Finally clause. */
    private final Block finallyBody;

    /** Exception symbol. */
    private Symbol exception;

    /**
     * Constructor
     *
     * @param lineNumber  lineNumber
     * @param token       token
     * @param finish      finish
     * @param body        try node body
     * @param catchBlocks list of catch blocks in order
     * @param finallyBody body of finally block or null if none
     */
    public TryNode(final int lineNumber, final long token, final int finish, final Block body, final List<Block> catchBlocks, final Block finallyBody) {
        super(lineNumber, token, finish);
        this.body        = body;
        this.catchBlocks = catchBlocks;
        this.finallyBody = finallyBody;
    }

    private TryNode(final TryNode tryNode, final Block body, final List<Block> catchBlocks, final Block finallyBody) {
        super(tryNode);
        this.body        = body;
        this.catchBlocks = catchBlocks;
        this.finallyBody = finallyBody;
        this.exception = tryNode.exception;
    }

    @Override
    public boolean isTerminal() {
        if (body.isTerminal()) {
            for (final Block catchBlock : getCatchBlocks()) {
                if (!catchBlock.isTerminal()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterTryNode(this)) {
            // Need to do finally body first for termination analysis. TODO still necessary?
            final Block newFinallyBody = finallyBody == null ? null : (Block)finallyBody.accept(visitor);
            final Block newBody        = (Block)body.accept(visitor);
            return visitor.leaveTryNode(
                setBody(newBody).
                setFinallyBody(newFinallyBody).
                setCatchBlocks(Node.accept(visitor, catchBlocks)));
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterTryNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append("try ");
    }

    /**
     * Get the body for this try block
     * @return body
     */
    public Block getBody() {
        return body;
    }

    /**
     * Reset the body of this try block
     * @param body new body
     * @return new TryNode or same if unchanged
     */
    public TryNode setBody(final Block body) {
        if (this.body == body) {
            return this;
        }
        return new TryNode(this,  body, catchBlocks, finallyBody);
    }

    /**
     * Get the catches for this try block
     * @return a list of catch nodes
     */
    public List<CatchNode> getCatches() {
        final List<CatchNode> catches = new ArrayList<>(catchBlocks.size());
        for (final Block catchBlock : catchBlocks) {
            catches.add(getCatchNodeFromBlock(catchBlock));
        }
        return Collections.unmodifiableList(catches);
    }

    private static CatchNode getCatchNodeFromBlock(final Block catchBlock) {
        return (CatchNode)catchBlock.getStatements().get(0);
    }

    /**
     * Get the catch blocks for this try block
     * @return a list of blocks
     */
    public List<Block> getCatchBlocks() {
        return Collections.unmodifiableList(catchBlocks);
    }

    /**
     * Set the catch blocks of this try
     * @param catchBlocks list of catch blocks
     * @return new TryNode or same if unchanged
     */
    public TryNode setCatchBlocks(final List<Block> catchBlocks) {
        if (this.catchBlocks == catchBlocks) {
            return this;
        }
        return new TryNode(this, body, catchBlocks, finallyBody);
    }

    /**
     * Get the exception symbol for this try block
     * @return a symbol for the compiler to store the exception in
     */
    public Symbol getException() {
        return exception;
    }

    /**
     * Get the body of the finally clause for this try
     * @return finally body, or null if no finally
     */
    public Block getFinallyBody() {
        return finallyBody;
    }

    /**
     * Set the finally body of this try
     * @param finallyBody new finally body
     * @return new TryNode or same if unchanged
     */
    public TryNode setFinallyBody(final Block finallyBody) {
        if (this.finallyBody == finallyBody) {
            return this;
        }
        return new TryNode(this, body, catchBlocks, finallyBody);
    }
}
