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

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * IR representation for a labeled statement. It implements JoinPredecessor to hold conversions that need to be effected
 * when the block exits normally, but is also targeted by a break statement that might bring different local variable
 * types to the join at the break point.
 */
public final class LabelNode extends LexicalContextStatement {
    /** Label ident. */
    private final String labelName;

    /** Statements. */
    private final Block body;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param labelName  label name
     * @param body       body of label node
     */
    public LabelNode(final int lineNumber, final long token, final int finish, final String labelName, final Block body) {
        super(lineNumber, token, finish);

        this.labelName = labelName;
        this.body  = body;
    }

    private LabelNode(final LabelNode labelNode, final String labelName, final Block body) {
        super(labelNode);
        this.labelName = labelName;
        this.body = body;
    }

    @Override
    public boolean isTerminal() {
        return body.isTerminal();
    }

    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterLabelNode(this)) {
            return visitor.leaveLabelNode(setBody(lc, (Block)body.accept(visitor)));
        }

        return this;
    }

    @Override
    public <R> R accept(LexicalContext lc, TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterLabelNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append(labelName).append(':');
    }

    /**
     * Get the body of the node
     * @return the body
     */
    public Block getBody() {
        return body;
    }

    /**
     * Reset the body of the node
     * @param lc lexical context
     * @param body new body
     * @return new for node if changed or existing if not
     */
    public LabelNode setBody(final LexicalContext lc, final Block body) {
        if (this.body == body) {
            return this;
        }
        return Node.replaceInLexicalContext(lc, this, new LabelNode(this, labelName, body));
    }

    /**
     * Get the label name
     * @return the label
     */
    public String getLabelName() {
        return labelName;
    }
}
