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
 * Holds an anonymous object variable (object creation encapsulated by braces).
 * <pre>e.g.
 * (new Object()),
 * (new Object),
 * (clone (new Object()))
 * </pre>
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AnonymousObjectVariable extends Variable {

    public AnonymousObjectVariable(int start, int end, ClassInstanceCreation classInstanceCreation) {
        super(start, end, classInstanceCreation);
    }

    public AnonymousObjectVariable(int start, int end, CloneExpression cloneExpression) {
        super(start, end, cloneExpression);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + getName() + ")"; //NOI18N
    }

}
