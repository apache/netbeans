/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a class or namespace constant declaration.
 *
 * <pre>e.g.
 * const MY_CONST = 5;
 * const MY_CONST = 5, YOUR_CONSTANT = 8;
 * const CONSTANT = [0, 1];
 * private const CONSTANT = 1; // PHP7.1
 * </pre>
 */
public class ConstantDeclaration extends BodyDeclaration {

    private final ArrayList<Identifier> names = new ArrayList<>();
    private final ArrayList<Expression> initializers = new ArrayList<>();
    private final boolean isGlobal;

    // XXX remove?
    private ConstantDeclaration(int start, int end, List<Identifier> names, List<Expression> initializers, boolean isGlobal) {
        super(start, end, BodyDeclaration.Modifier.IMPLICIT_PUBLIC);

        if (names == null || initializers == null || names.size() != initializers.size()) {
            throw new IllegalArgumentException();
        }

        Iterator<Identifier> iteratorNames = names.iterator();
        Iterator<Expression> iteratorInitializers = initializers.iterator();
        Identifier identifier;
        while (iteratorNames.hasNext()) {
            identifier = iteratorNames.next();
            this.names.add(identifier);
            Expression initializer = iteratorInitializers.next();
            this.initializers.add(initializer);
        }
        this.isGlobal = isGlobal;
    }

    public ConstantDeclaration(int start, int end, int modifier, List variablesAndDefaults, boolean isGlobal) {
        super(start, end, modifier);
        if (variablesAndDefaults == null || variablesAndDefaults.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (Iterator iter = variablesAndDefaults.iterator(); iter.hasNext();) {
            ASTNode[] element = (ASTNode[]) iter.next();
            assert element != null && element.length == 2 && element[0] != null && element[1] != null;

            this.names.add((Identifier) element[0]);
            this.initializers.add((Expression) element[1]);
        }
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * @return constant initializers expressions
     */
    public List<Expression> getInitializers() {
        return Collections.unmodifiableList(this.initializers);
    }

    /**
     * @return the constant names
     */
    public List<Identifier> getNames() {
        return Collections.unmodifiableList(this.names);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInitializers()) {
            sb.append(expression).append(","); //NOI18N
        }
        return "const " + sb; //NOI18N
    }

}
