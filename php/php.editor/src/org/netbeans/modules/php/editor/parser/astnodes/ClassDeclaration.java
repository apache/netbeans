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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Represents a class declaration.
 *
 * <pre>e.g.
 * class MyClass { },
 * class MyClass extends SuperClass implements Interface1, Interface2 {
 *   const MY_CONSTANT = 3;
 *   public static final $myVar = 5, $yourVar;
 *   var $anotherOne;
 *   private function myFunction($a) { }
 * },
 * #[A(1)]
 * class MyClass { } // [NETBEANS-4443] PHP 8.0
 * </pre>
 */
public class ClassDeclaration extends TypeDeclaration {

    public enum Modifier {
        NONE,
        ABSTRACT,
        FINAL,
        READONLY, // PHP 8.2 gh-4725
        ;

        @Override
        public String toString() {
            if (this == NONE) {
                return ""; // NOI18N
            }
            return this.name().toLowerCase();
        }
    }

    private final Map<Modifier, Set<OffsetRange>> modifiers = new EnumMap<>(Modifier.class);
    private ClassDeclaration.Modifier modifier;
    private Expression superClass;

    private ClassDeclaration(int start, int end, Map<Modifier, Set<OffsetRange>> modifiers, Identifier className, Expression superClass, Expression[] interfaces, Block body, List<Attribute> attributes) {
        super(start, end, className, interfaces, body, attributes);
        this.modifiers.putAll(modifiers);
        this.superClass = superClass;
    }

    private ClassDeclaration(int start, int end, Map<Modifier, Set<OffsetRange>> modifiers, Identifier className, Expression superClass, List<Expression> interfaces, Block body, List<Attribute> attributes) {
        this(start, end, modifiers, className, superClass, interfaces == null ? null : interfaces.toArray(new Expression[0]), body, attributes);
    }

    public ClassDeclaration(int start, int end, Map<Modifier, Set<OffsetRange>>modifiers, Identifier className, Expression superClass, List<Expression> interfaces, Block body) {
        this(start, end, modifiers, className, superClass, interfaces, body, Collections.emptyList());
    }

    /**
     * @deprecated Classes can have multiple modifers since PHP 8.2
     */
    private ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, Expression[] interfaces, Block body, List<Attribute> attributes) {
        super(start, end, className, interfaces, body, attributes);

        this.modifier = modifier;
        this.superClass = superClass;
    }

    /**
     * @deprecated Classes can have multiple modifers since PHP 8.2
     */
    private ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, List<Expression> interfaces, Block body, List<Attribute> attributes) {
        this(start, end, modifier, className, superClass, interfaces == null ? null : interfaces.toArray(new Expression[0]), body, attributes);
    }

    /**
     * @deprecated Classes can have multiple modifers since PHP 8.2
     */
    public ClassDeclaration(int start, int end, ClassDeclaration.Modifier modifier, Identifier className, Expression superClass, List<Expression> interfaces, Block body) {
        this(start, end, modifier, className, superClass, interfaces, body, Collections.emptyList());
    }

    public static ClassDeclaration create(ClassDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new ClassDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getModifiers(),
                declaration.getName(),
                declaration.getSuperClass(),
                declaration.getInterfaces(),
                declaration.getBody(),
                attributes
        );
    }

    /**
     * @deprecated Classes can have multiple modifers since PHP 8.2
     */
    public ClassDeclaration.Modifier getModifier() {
        if (modifier != null) {
            return modifier;
        }
        assert !modifiers.keySet().isEmpty();
        assert false : "Use getModifiers() instead"; // NOI18N
        return modifiers.keySet().iterator().next();
    }

    public Map<Modifier, Set<OffsetRange>> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
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
        StringBuilder sbAttributes = new StringBuilder();
        getAttributes().forEach(attribute -> sbAttributes.append(attribute).append(" ")); // NOI18N
        StringBuilder sbModifiers = new StringBuilder();
        for (Modifier mod : getModifiers().keySet()) {
            if (mod == Modifier.NONE) {
                break;
            }
            sbModifiers.append(mod).append(" "); // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaces()) {
            sb.append(expression).append(","); //NOI18N
        }
        return sbAttributes.toString() + sbModifiers + "class " + getName() + " extends " + getSuperClass() + " implements " + sb + getBody(); //NOI18N
    }

}
