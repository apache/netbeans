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

import java.util.List;

/**
 * Represents an interface declaration
 * <pre>
 * <pre>e.g.<pre>
 * interface MyInterface { },
 * interface MyInterface extends Interface1, Interface2 {
 *	 const MY_CONSTANT = 3;
 *	 public function myFunction($a);
 * }
 */
public class InterfaceDeclaration extends TypeDeclaration {

    private InterfaceDeclaration(int start, int end, Identifier interfaceName, Expression[] interfaces, Block body) {
        super(start, end, interfaceName, interfaces, body);
    }

    public InterfaceDeclaration(int start, int end, Identifier interfaceName, List<Expression> interfaces, Block body) {
        this(start, end, interfaceName, interfaces.toArray(new Expression[interfaces.size()]), body);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
