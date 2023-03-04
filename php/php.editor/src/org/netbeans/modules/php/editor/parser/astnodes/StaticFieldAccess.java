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
 * Represents a static field access.
 * <pre>e.g.<pre> MyClass::$a
 * MyClass::$$a[3]
 */
public class StaticFieldAccess extends StaticDispatch {

    private Variable field;

    public StaticFieldAccess(int start, int end, Expression className, Variable field) {
        super(start, end, className);
        this.field = field;
    }

    /**
     * The field of this access
     *
     * @return field of this access
     */
    public Variable getField() {
        return field;
    }

    public ASTNode getMember() {
        return getField();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
