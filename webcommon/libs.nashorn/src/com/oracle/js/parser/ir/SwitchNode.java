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
 * IR representation of a SWITCH statement.
 */
public final class SwitchNode extends BreakableStatement {
    /** Switch expression. */
    private final Expression expression;

    /** Switch cases. */
    private final List<CaseNode> cases;

    /** Switch default index. */
    private final int defaultCaseIndex;

    /** Tag symbol. */
    private Symbol tag;

    /**
     * Constructor
     *
     * @param lineNumber  lineNumber
     * @param token       token
     * @param finish      finish
     * @param expression  switch expression
     * @param cases       cases
     * @param defaultCase the default case node - null if none, otherwise has to be present in cases list
     */
    public SwitchNode(final int lineNumber, final long token, final int finish, final Expression expression, final List<CaseNode> cases, final CaseNode defaultCase) {
        super(lineNumber, token, finish, new Label("switch_break"));
        this.expression       = expression;
        this.cases            = cases;
        this.defaultCaseIndex = defaultCase == null ? -1 : cases.indexOf(defaultCase);
    }

    private SwitchNode(final SwitchNode switchNode, final Expression expression, final List<CaseNode> cases, final int defaultCaseIndex) {
        super(switchNode);
        this.expression       = expression;
        this.cases            = cases;
        this.defaultCaseIndex = defaultCaseIndex;
        this.tag              = switchNode.getTag(); //TODO are symbols inhereted as references?
    }

    @Override
    public boolean isTerminal() {
        //there must be a default case, and that including all other cases must terminate
        if (!cases.isEmpty() && defaultCaseIndex != -1) {
            for (final CaseNode caseNode : cases) {
                if (!caseNode.isTerminal()) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterSwitchNode(this)) {
            return visitor.leaveSwitchNode(
                setExpression(lc, (Expression)expression.accept(visitor)).
                setCases(lc, Node.accept(visitor, cases), defaultCaseIndex));
        }

        return this;
    }

    @Override
    public <R> R accept(LexicalContext lc, TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterSwitchNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append("switch (");
        expression.toString(sb, printType);
        sb.append(')');
    }

    /**
     * Return the case node that is default case
     * @return default case or null if none
     */
    public CaseNode getDefaultCase() {
        return defaultCaseIndex == -1 ? null : cases.get(defaultCaseIndex);
    }

    /**
     * Get the cases in this switch
     * @return a list of case nodes
     */
    public List<CaseNode> getCases() {
        return Collections.unmodifiableList(cases);
    }

    private SwitchNode setCases(final LexicalContext lc, final List<CaseNode> cases, final int defaultCaseIndex) {
        if (this.cases == cases) {
            return this;
        }
        return Node.replaceInLexicalContext(lc, this, new SwitchNode(this, expression, cases, defaultCaseIndex));
    }

    /**
     * Return the expression to switch on
     * @return switch expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Set or reset the expression to switch on
     * @param lc lexical context
     * @param expression switch expression
     * @return new switch node or same if no state was changed
     */
    public SwitchNode setExpression(final LexicalContext lc, final Expression expression) {
        if (this.expression == expression) {
            return this;
        }
        return Node.replaceInLexicalContext(lc, this, new SwitchNode(this, expression, cases, defaultCaseIndex));
    }

    /**
     * Get the tag symbol for this switch. The tag symbol is where
     * the switch expression result is stored
     * @return tag symbol
     */
    public Symbol getTag() {
        return tag;
    }

    /**
     * Set the tag symbol for this switch. The tag symbol is where
     * the switch expression result is stored
     * @param tag a symbol
     */
    public void setTag(final Symbol tag) {
        this.tag = tag;
    }
}
