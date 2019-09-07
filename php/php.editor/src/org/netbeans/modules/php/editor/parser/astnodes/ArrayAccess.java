/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Holds a variable and an index that point to array or hashtable. e.g.
 * <pre>
 * $a[],
 * $a[1],
 * $a[$b],
 * $a{'name'} // deprecate since PHP 7.4
 * </pre>
 *
 * @see https://wiki.php.net/rfc/deprecate_curly_braces_array_access
 */
public class ArrayAccess extends Variable {

    /**
     * In case of array / hashtable variable, the index expression is added
     */
    private final ArrayDimension dimension;

    public ArrayAccess(int start, int end, VariableBase variableName, ArrayDimension dimension) {
        super(start, end, variableName);

        //if (variableName != null) variableName.setParent(this);
        //if (index != null) index.setParent(index);
        this.dimension = dimension;
    }

    public ArrayDimension getDimension() {
        return dimension;
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
        return getDimension().getType() + " " + getName() + getDimension(); //NOI18N
    }

}
