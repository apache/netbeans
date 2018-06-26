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

import java.util.List;

/**
 * Represents a class declaration
 * <pre>
 * <pre>e.g.<pre>
 * class MyClass { },
 * class MyClass extends SuperClass implements Interface1, Interface2 {
 *   const MY_CONSTANT = 3;
 *   public static final $myVar = 5, $yourVar;
 *   var $anotherOne;
 *   private function myFunction($a) { }
 * }
 */
public class ClassDeclaration extends TypeDeclaration {

    public enum Modifier {
        NONE,
        ABSTRACT,
        FINAL
    }

    private ClassDeclaration.Modifier modifier;
    private Expression superClass;

    private ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, Expression[] interfaces, Block body) {
        super(start, end, className, interfaces, body);

        this.modifier = modifier;
        this.superClass = superClass;
    }

    public ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, List<Expression> interfaces, Block body) {
        this(start, end, modifier, className, superClass, interfaces == null ? null : interfaces.toArray(new Expression[interfaces.size()]), body);
    }

    public ClassDeclaration.Modifier getModifier() {
        return modifier;
    }

    public Expression getSuperClass() {
        return superClass;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaes()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getModifier() + "class " + getName() + " extends " + getSuperClass() + " implements " + sb + getBody(); //NOI18N
    }

}
