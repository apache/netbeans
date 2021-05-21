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
 * Represents a fields declaration.
 * <pre>
 * e.g.
 * var $a, $b;
 * public $a = 3;
 * final private static $var;
 * private int $int = 20; // PHP 7.4
 * </pre>
 */
public class SingleFieldDeclaration extends ASTNode {

    private final Variable name;
    private final Expression value;
    private final Expression fieldType;

    public SingleFieldDeclaration(int start, int end, Variable name, Expression value, Expression fieldType) {
        super(start, end);
        this.name = name;
        this.value = value;
        this.fieldType = fieldType;
    }

    /**
     * @return the name of the field
     */
    public Variable getName() {
        return this.name;
    }

    /**
     * @return the initial value of this field, null if none
     */
    public Expression getValue() {
        return this.value;
    }

    public Expression getFieldType() {
        return fieldType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getFieldType() + " " + getName() + " = " + getValue(); //NOI18N
    }

}
