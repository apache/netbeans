/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
 * Represents a single element of array.
 * Holds the key and the value both can be any expression
 * The key can be null
 * <pre>e.g.<pre> 1,
 * 'Dodo'=>'Golo',
 * $a,
 * $b=>foo(),
 * 1=>$myClass->getFirst() *
 */
public class ArrayElement extends ASTNode {

    private Expression key;
    private Expression value;

    public ArrayElement(int start, int end, Expression key, Expression value) {
        super(start, end);
        this.value = value;
        this.key = key;
//        if (key != null) key.setParent(this);
//        if (value != null) value.setParent(this);
    }

    public ArrayElement(int start, int end, Expression value) {
        this(start, end, null, value);
    }

    /**
     * Returns the key of this array element(null if missing).
     *
     * @return the key of the array element
     */
    public Expression getKey() {
        return key;
    }

    /**
     * Returns the value expression of this array element.
     *
     * @return the value expression of this array element
     */
    public Expression getValue() {
        return this.value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getKey() + " => " + getValue(); //NOI18N
    }

}
