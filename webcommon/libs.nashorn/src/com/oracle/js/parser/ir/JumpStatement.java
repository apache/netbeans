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
 * Common base class for jump statements (e.g. {@code break} and {@code continue}).
 */
public abstract class JumpStatement extends Statement {

    private final String labelName;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param labelName  label name for break or null if none
     */
    protected JumpStatement(final int lineNumber, final long token, final int finish, final String labelName) {
        super(lineNumber, token, finish);
        this.labelName = labelName;
    }

    /**
     * Copy constructor.
     * @param jumpStatement the original jump statement.
     */
    protected JumpStatement(final JumpStatement jumpStatement) {
        super(jumpStatement);
        this.labelName = jumpStatement.labelName;
    }

    @Override
    public boolean hasGoto() {
        return true;
    }

    /**
     * Get the label name for this break node
     * @return label name, or null if none
     */
    public String getLabelName() {
        return labelName;
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append(getStatementName());

        if (labelName != null) {
            sb.append(' ').append(labelName);
        }
    }

    abstract String getStatementName();

    /**
     * Finds the target for this jump statement in a lexical context.
     * @param lc the lexical context
     * @return the target, or null if not found
     */
    public abstract BreakableNode getTarget(final LexicalContext lc);

    /**
     * Returns the label corresponding to this kind of jump statement (either a break or continue label) in the target.
     * @param target the target. Note that it need not be the target of this jump statement, as the method can retrieve
     * a label on any passed target as long as the target has a label of the requisite kind. Of course, it is advisable
     * to invoke the method on a jump statement that targets the breakable.
     * @return the label of the target corresponding to the kind of jump statement.
     * @throws ClassCastException if invoked on the kind of breakable node that this jump statement is not prepared to
     * handle.
     */
    public abstract Label getTargetLabel(final BreakableNode target);
}
