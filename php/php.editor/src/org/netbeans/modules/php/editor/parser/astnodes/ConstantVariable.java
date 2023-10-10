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
 * Holds a constant name as a variable.
 * <pre>
 * e.g.
 * C,
 * NamespaceName\C,
 * \NamespaceName\C,
 *
 * class Example() {
 *     public string $field = "example";
 *     public function something(): void {
 *         echo "hello!";
 *     }
 * }
 * const C = new Example(); // OK since PHP 8.1
 * C->something();
 * echo C->field;
 * </pre>
 */
public class ConstantVariable extends Variable {

    public ConstantVariable(Expression constant) {
        super(constant.getStartOffset(), constant.getEndOffset(), constant);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
