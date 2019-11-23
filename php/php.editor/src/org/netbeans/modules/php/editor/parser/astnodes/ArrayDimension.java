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
 * Represents array dimension. e.g.
 * <pre>
 * [1],
 * ["test"],
 * {1},
 * {'key'}
 * </pre>
 *
 * Used classes:
 * {@link ArrayAccess}, {@link ExpressionArrayAccess}, {@link DereferencedArrayAccess}
 *
 * Curly brace syntax ({}) is deprecated since PHP 7.4.
 *
 * @see https://wiki.php.net/rfc/deprecate_curly_braces_array_access
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ArrayDimension extends Expression {

    public enum Type {
        VARIABLE_ARRAY,
        VARIABLE_HASHTABLE
    }

    private final Expression index;
    private final ArrayDimension.Type type;

    public ArrayDimension(int start, int end, Expression index, ArrayDimension.Type type) {
        super(start, end);
        this.index = index;
        this.type = type;
    }

    public Expression getIndex() {
        return index;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (type == Type.VARIABLE_HASHTABLE) {
            return "{" + getIndex() + "}"; //NOI18N
        }
        return "[" + getIndex() + "]"; //NOI18N
    }

}
