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
 * Represents an assignment statement.
 * <pre>e.g.<pre> $a = 5,
 * $a += 5,
 * $a .= $b,
 */
public class Assignment extends Expression {

    public enum Type {
        EQUAL("="), //NOI18N
        PLUS_EQUAL("+="), //NOI18N
        MINUS_EQUAL("-="), //NOI18N
        MUL_EQUAL("*="), //NOI18N
        DIV_EQUAL("/="), //NOI18N
        CONCAT_EQUAL(".="), //NOI18N
        MOD_EQUAL("%="), //NOI18N
        AND_EQUAL("&="), //NOI18N
        OR_EQUAL("|="), //NOI18N
        XOR_EQUAL("^="), //NOI18N
        SL_EQUAL("<<="), //NOI18N
        SR_EQUAL(">>="), //NOI18N
        POW_EQUAL("**="); //NOI18N

        private String operator;

        private Type(String operator) {
            this.operator = operator;
        }

        @Override
        public String toString() {
            return operator;
        }
    }

    private VariableBase leftHandSide;
    private Assignment.Type operator;
    private Expression rightHandSide;

    public Assignment(int start, int end, VariableBase leftHandSide, Assignment.Type operator, Expression rightHandSide) {
        super(start, end);
        if (leftHandSide == null || rightHandSide == null) {
            throw new IllegalArgumentException();
        }
        this.leftHandSide = leftHandSide;
        this.operator = operator;
        this.rightHandSide = rightHandSide;
//        leftHandSide.setParent(this);
//        rightHandSide.setParent(this);
    }

    /**
     * Returns the operator of this assignment expression.
     *
     * @return the assignment operator
     */
    public Assignment.Type getOperator() {
        return this.operator;
    }

    /**
     * Returns the left hand side of this assignment expression.
     *
     * @return the left hand side node
     */
    public VariableBase getLeftHandSide() {
        return this.leftHandSide;
    }

    /**
     * Returns the right hand side of this assignment expression.
     *
     * @return the right hand side node
     */
    public Expression getRightHandSide() {
        return this.rightHandSide;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getLeftHandSide() + " " + getOperator() + " " + getRightHandSide(); //NOI18N
    }

}
