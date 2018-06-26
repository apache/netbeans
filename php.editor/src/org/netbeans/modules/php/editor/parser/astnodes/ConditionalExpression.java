/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 * Represents conditional expression
 * Holds the condition, if true expression and if false expression
 * each on e can be any expression
 * <pre>e.g.
 * (bool) $a ? 3 : 4
 * $a > 0 ? $a : -$a
 * $a > 0 ?: -$a
 * $a > 0 ?? -$a
 * </pre>
 */
public class ConditionalExpression extends Expression {

    public enum OperatorType {
        QUESTION_MARK("?") { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                throw new IllegalStateException();
            }
        },
        ELVIS("?:", true) { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                // XXX introduce token for elvis
                assert false;
                return false;
            }
        },
        COALESCE("??", true) { // NOI18N
            @Override
            public boolean isOperatorToken(Token<PHPTokenId> token) {
                return token.id() == PHPTokenId.PHP_OPERATOR
                        && TokenUtilities.textEquals("??", token.text()); // NOI18N
            }
        };

        private final String operatorSign;
        private final boolean shortened;


        private OperatorType(String operatorSign) {
            this(operatorSign, false);
        }

        private OperatorType(String operatorSign, boolean shortened) {
            this.operatorSign = operatorSign;
            this.shortened = shortened;
        }

        public abstract boolean isOperatorToken(Token<PHPTokenId> token);

        public boolean isShortened() {
            return shortened;
        }

        @Override
        public String toString() {
            return operatorSign;
        }

    }


    private final Expression condition;
    private final OperatorType operator;
    private final Expression ifTrue;
    private final Expression ifFalse;


    public ConditionalExpression(int start, int end, Expression condition, OperatorType operator, Expression ifTrue, Expression ifFalse) {
        super(start, end);

        if (condition == null || ifFalse == null) {
            throw new IllegalArgumentException();
        }
        this.condition = condition;
        this.operator = operator;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    /**
     * Returns the condition of this conditional expression.
     *
     * @return the condition node
     */
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Returns the operator of this conditional expression.
     *
     * @return the conditional operator
     */
    public OperatorType getOperator() {
        return operator;
    }

    /**
     * Returns the "then" part of this conditional expression.
     *
     * @return the "then" expression node
     */
    public Expression getIfTrue() {
        return ifTrue;
    }

    /**
     * Returns the "else" part of this conditional expression.
     *
     * @return the "else" expression node
     */
    public Expression getIfFalse() {
        return this.ifFalse;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getCondition() + " ? " + getIfTrue() + " : " + getIfFalse(); //NOI18N
    }

}
