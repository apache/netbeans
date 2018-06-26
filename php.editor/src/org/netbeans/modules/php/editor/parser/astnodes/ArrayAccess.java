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
 * Holds a variable and an index that point to array or hashtable
 * <pre>e.g.<pre> $a[],
 * $a[1],
 * $a[$b],
 * $a{'name'}
 */
public class ArrayAccess extends Variable {

    public enum Type {
        VARIABLE_ARRAY,
        VARIABLE_HASHTABLE
    }

    /**
     * In case of array / hashtable variable, the index expression is added
     */
    private ArrayDimension dimension;
    private ArrayAccess.Type arrayType;

    public ArrayAccess(int start, int end, VariableBase variableName, ArrayDimension dimension, ArrayAccess.Type arrayType) {
        super(start, end, variableName);

        //if (variableName != null) variableName.setParent(this);
        //if (index != null) index.setParent(index);
        this.dimension = dimension;
        this.arrayType = arrayType;
    }

    public ArrayDimension getDimension() {
        return dimension;
    }

    public ArrayAccess.Type getArrayType() {
        return arrayType;
    }

    /**
     * Returns the name (expression) of this variable
     *
     * @return the expression name node
     */
    @Override
    public VariableBase getName() {
        return (VariableBase) super.getName();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getArrayType() + " " + getName() + getDimension(); //NOI18N
    }

}
