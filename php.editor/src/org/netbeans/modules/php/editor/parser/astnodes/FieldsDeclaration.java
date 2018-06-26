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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a fields declaration
 * <pre>e.g.<pre> var $a, $b;
 * public $a = 3;
 * final private static $var;
 */
public class FieldsDeclaration extends BodyDeclaration {

    private final ArrayList<SingleFieldDeclaration> fields = new ArrayList<>();

    public FieldsDeclaration(int start, int end, int modifier, List variablesAndDefaults) {
        super(start, end, modifier);

        if (variablesAndDefaults == null || variablesAndDefaults.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (Iterator iter = variablesAndDefaults.iterator(); iter.hasNext();) {
            final Object next = iter.next();
            if (next instanceof SingleFieldDeclaration) {
                this.fields.add((SingleFieldDeclaration) next);
            } else {
                ASTNode[] element = (ASTNode[]) next;
                SingleFieldDeclaration field = createField((Variable) element[0], (Expression) element[1]);
                this.fields.add(field);
            }
        }
    }

    private SingleFieldDeclaration createField(Variable name, Expression value) {
        int start = name.getStartOffset();
        int end = value == null ? name.getEndOffset() : value.getEndOffset();
        final SingleFieldDeclaration result = new SingleFieldDeclaration(start, end, name, value);
        return result;
    }

    /**
     * The list of single fields that are declared
     *
     * @return List of single fields
     */
    public List<SingleFieldDeclaration> getFields() {
        return this.fields;
    }

    public Expression[] getInitialValues() {
        Expression[] result = new Expression[this.fields.size()];
        int i = 0;
        for (SingleFieldDeclaration field : this.fields) {
            result[i++] = field.getValue();
        }
        return result;
    }

    public Variable[] getVariableNames() {
        Variable[] result = new Variable[this.fields.size()];
        int i = 0;
        for (SingleFieldDeclaration field : this.fields) {
            result[i++] = field.getName();
        }
        return result;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SingleFieldDeclaration singleFieldDeclaration : getFields()) {
            sb.append(singleFieldDeclaration).append(" "); //NOI18N
        }
        return sb.toString();
    }

}
