/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents an infix expression
 * <pre>e.g.<pre> $a + 1,
 * 3 - 2,
 * foo() * $a->bar(),
 * 'string'.$c
 */
public class InfixExpression extends Expression {

    public enum OperatorType {
        IS_IDENTICAL("==="), //NOI18N
        IS_NOT_IDENTICAL("!=="), //NOI18N
        IS_EQUAL("=="), //NOI18N
    	IS_NOT_EQUAL("!="), //NOI18N
        RGREATER("<"), //NOI18N
        IS_SMALLER_OR_EQUAL("<="), //NOI18N
        LGREATER(">"), //NOI18N
        IS_GREATER_OR_EQUAL(">="), //NOI18N
        SPACESHIP("<=>"), //NOI18N
        BOOL_OR("||"), //NOI18N
        BOOL_AND("&&"), //NOI18N
        STRING_OR("or"), //NOI18N
    	STRING_AND("and"), //NOI18N
        STRING_XOR("xor"), //NOI18N
        OR("|"), //NOI18N
        AND("&"), //NOI18N
        XOR("^"), //NOI18N
        CONCAT("."), //NOI18N
        PLUS("+"), //NOI18N
        MINUS("-"), //NOI18N
    	MUL("*"), //NOI18N
        DIV("/"), //NOI18N
        MOD("%"), //NOI18N
        SL("<<"), //NOI18N
    	SR(">>"), //NOI18N
    	POW("**"); //NOI18N

        private final String operatorSign;

        private OperatorType(final String operatorSign) {
            this.operatorSign = operatorSign;
        }

        @Override
        public String toString() {
            return operatorSign;
        }
    }

    private Expression left;
    private InfixExpression.OperatorType operator;
    private Expression right;

    public InfixExpression(int start, int end, Expression left, InfixExpression.OperatorType operator, Expression right) {
        super(start, end);

        if (right == null || left == null ) {
            throw new IllegalArgumentException();
        }

        this.left = left;
        this.right = right;
        this.operator = operator;
    }



    /**
     * Returns the operator of this infix expression.
     *
     * @return the infix operator
     */
    public InfixExpression.OperatorType getOperator() {
        return this.operator;
    }

    /**
     * Returns the left operand of this infix expression.
     *
     * @return the left operand node
     */
    public Expression getLeft() {
        return this.left;
    }

    /**
     * Returns the right operand of this infix expression.
     *
     * @return the right operand node
     */
    public Expression getRight() {
        return this.right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getLeft() + " " + getOperator() + " " + getRight(); //NOI18N
    }

}

