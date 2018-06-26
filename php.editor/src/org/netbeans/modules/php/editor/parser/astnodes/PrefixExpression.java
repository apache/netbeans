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
 * Represents a prefix expression
 * <pre>e.g.<pre> --$a,
 * --foo()
 */
public class PrefixExpression extends Expression {

    public enum Operator {
        INC("++"), //NOI18N
        DEC("--"); //NOI18N

        private final String operatorSign;

        private Operator(final String operatorSign) {
            this.operatorSign = operatorSign;
        }

        @Override
        public String toString() {
            return operatorSign;
        }
    }

    private VariableBase variable;
    private PrefixExpression.Operator operator;

    public PrefixExpression(int start, int end, VariableBase variable, PrefixExpression.Operator operator) {
        super(start, end);

        if (variable == null) {
            throw new IllegalArgumentException();
        }

        this.variable = variable;
        this.operator = operator;
    }


    /**
     * Returns the operator of this prefix expression.
     *
     * @return the prefix operator
     */
    public PrefixExpression.Operator getOperator() {
        return this.operator;
    }

    /**
     * Returns the variable in the prefix expression.
     *
     * @return the expression node
     */
    public VariableBase getVariable() {
        return variable;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getOperator() + getVariable(); //NOI18N
    }

}
