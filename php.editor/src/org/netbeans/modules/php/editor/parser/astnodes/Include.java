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
 * Represents include, include_once, require and require_once expressions
 * <pre>e.g.<pre> include('myFile.php'),
 * include_once($myFile),
 * require($myClass->getFileName()),
 * require_once(A::FILE_NAME)
 */
public class Include extends Expression {

    public enum Type {
        REQUIRE,
        REQUIRE_ONCE,
        INCLUDE,
        INCLUDE_ONCE
    }

    private Expression expression;
    private Include.Type includeType;

    public Include(int start, int end, Expression expr, Include.Type type) {
        super(start, end);

        if (expr == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expr;
        this.includeType = type;
    }


    /**
     * Returns the expression of this include.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * the include type one of the following {@link #IT_INCLUDE_ONCE}, {@link #IT_INCLUDE},
     * 	{@link #IT_REQUIRE_ONCE}, {@link #IT_REQUIRE}
     * @return include type
     */
    public Include.Type getIncludeType() {
        return this.includeType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getIncludeType() + " " + getExpression(); //NOI18N
    }

}
