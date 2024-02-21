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

import java.util.Collections;
import java.util.List;

/**
 * Represents an interface declaration.
 *
 * <pre>e.g.
 * interface MyInterface { },
 * interface MyInterface extends Interface1, Interface2 {
 *	 const MY_CONSTANT = 3;
 *	 public function myFunction($a);
 * },
 * #[A(1)]
 * interface MyInterface { } // [NETBEANS-4443] PHP 8.0
 * </pre>
 */
public class InterfaceDeclaration extends TypeDeclaration {

    private InterfaceDeclaration(int start, int end, Identifier interfaceName, Expression[] interfaces, Block body, List<Attribute> attributes) {
        super(start, end, interfaceName, interfaces, body, attributes);
    }

    private InterfaceDeclaration(int start, int end, Identifier interfaceName, List<Expression> interfaces, Block body, List<Attribute> attributes) {
        this(start, end, interfaceName, interfaces.toArray(new Expression[0]), body, attributes);
    }

    public InterfaceDeclaration(int start, int end, Identifier interfaceName, List<Expression> interfaces, Block body) {
        this(start, end, interfaceName, interfaces, body, Collections.emptyList());
    }

    public static InterfaceDeclaration create(InterfaceDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new InterfaceDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getName(),
                declaration.getInterfaces(),
                declaration.getBody(),
                attributes
        );
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
