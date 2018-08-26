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
 * Represents a class declaration
 * <pre>
 * <pre>e.g.<pre>
 * class MyClass { },
 * class MyClass extends SuperClass implements Interface1, Interface2 {
 *   const MY_CONSTANT = 3;
 *   public static final $myVar = 5, $yourVar;
 *   var $anotherOne;
 *   private function myFunction($a) { }
 * }
 */
public class ClassDeclaration extends TypeDeclaration {

    public enum Modifier {
        NONE,
        ABSTRACT,
        FINAL
    }

    private ClassDeclaration.Modifier modifier;
    private Expression superClass;

    private ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, Expression[] interfaces, Block body) {
        super(start, end, className, interfaces, body);

        this.modifier = modifier;
        this.superClass = superClass;
    }

    public ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, List<Expression> interfaces, Block body) {
        this(start, end, modifier, className, superClass, interfaces == null ? null : interfaces.toArray(new Expression[interfaces.size()]), body);
    }

    public ClassDeclaration.Modifier getModifier() {
        return modifier;
    }

    public Expression getSuperClass() {
        return superClass;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaes()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getModifier() + "class " + getName() + " extends " + getSuperClass() + " implements " + sb + getBody(); //NOI18N
    }

}
