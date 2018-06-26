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
 * Holds a variable.
 * note that the variable name can be expression,
 * <pre>e.g.<pre> $a
 *
 * Subclasses: {@link ArrayAccess}, {@link ReflectionVariable}, {@link StaticFieldAccess}
 */
public class Variable extends VariableBase {

    private Expression name;
    private boolean isDollared;

    protected Variable(int start, int end, Expression variableName, boolean isDollared) {
        super(start, end);
        this.name = variableName;
        this.isDollared = isDollared;
    }

    protected Variable(int start, int end, Expression variableName) {
        this(start, end, variableName, false);
    }

    /**
     * A simple variable (like $a) can be constructed with a string
     * The string is warped by an identifier
     * @param start
     * @param end
     * @param variableName
     */
    public Variable(int start, int end, String variableName) {
        this(start, end, createIdentifier(start, end, variableName), checkIsDollared(variableName));
    }

    private static Identifier createIdentifier(int start, int end, String idName) {
        if (checkIsDollared(idName)) {
            idName = idName.substring(1);
            // the start position move after the the dollar mark
            start++;
        }
        return new Identifier(start, end, idName);
    }

    private static boolean checkIsDollared(String variableName) {
        return variableName.indexOf('$') == 0;
    }

    /**
     * Returns the name (expression) of this variable
     *
     * @return the expression name node
     */
    public Expression getName() {
        return name;
    }

    /**
     * True this variable node is dollared
     *
     * @return True if this variable node is dollared
     */
    public boolean isDollared() {
        return isDollared;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (isDollared() ? "$" : "") + getName(); //NOI18N
    }
}
