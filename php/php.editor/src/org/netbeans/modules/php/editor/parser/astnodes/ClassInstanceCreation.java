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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a class creation. It can be just calling ctor of existing class or
 * creating new anonymous class. This class holds the class name as an
 * expression and array of constructor parameters
 *
 * <pre>e.g.
 * new MyClass(),
 * new $a('start'),
 * new foo()(1, $a),
 * new class {...},
 * new class(10) extends SomeClass implements SomeInterface {...},
 * #[A(1)] new class {...} // [NETBEANS-4443] PHP 8.0
 * </pre>
 */
public class ClassInstanceCreation extends Expression implements Attributed {

    // common
    private final List<Expression> ctorParams = new ArrayList<>();
    // ctor
    private ClassName className;
    // anonymous
    @NullAllowed
    private final String fileName;
    private final int classCounter;
    private final int classStartOffset;
    private Expression superClass;
    private final List<Expression> interfaces = new ArrayList<>();
    private Block body;
    private final List<Attribute> attributes = new ArrayList<>();

    // ctor
    public ClassInstanceCreation(int start, int end, @NonNull ClassName className, @NullAllowed List<Expression> ctorParams) {
        super(start, end);
        assert className != null;
        this.className = className;
        if (ctorParams != null) {
            this.ctorParams.addAll(ctorParams);
        }
        fileName = null;
        classCounter = -1;
        classStartOffset = -1;
    }

    // anonymous
    private ClassInstanceCreation(String fileName, int classCounter, int start, int end, int classStartOffset, @NullAllowed List<Expression> ctorParams,
            @NullAllowed Expression superClass, @NullAllowed List<Expression> interfaces, @NonNull Block body, List<Attribute> attributes) {
        super(start, end);
        assert classCounter > 0 : classCounter;
        assert classStartOffset > -1 : classStartOffset;
        assert body != null;
        this.fileName = fileName == null ? null : fileName.replace(' ', '_').replace('.', '_'); // NOI18N
        this.classCounter = classCounter;
        this.classStartOffset = classStartOffset;
        if (ctorParams != null) {
            this.ctorParams.addAll(ctorParams);
        }
        this.superClass = superClass;
        if (interfaces != null) {
            this.interfaces.addAll(interfaces);
        }
        this.body = body;
        this.attributes.addAll(attributes);
    }

    public static ClassInstanceCreation anonymous(String fileName, int classCounter, int start, int end, int classStartOffset, List<Expression> ctorParams,
            Expression superClass, List<Expression> interfaces, Block body, List<Attribute> attributes) {
        return new ClassInstanceCreation(fileName, classCounter, start, end, classStartOffset, ctorParams, superClass, interfaces, body, attributes);
    }

    public static ClassInstanceCreation anonymous(String fileName, int classCounter, int start, int end, int classStartOffset, List<Expression> ctorParams,
            Expression superClass, List<Expression> interfaces, Block body) {
        return new ClassInstanceCreation(fileName, classCounter, start, end, classStartOffset, ctorParams, superClass, interfaces, body, Collections.emptyList());
    }

    public boolean isAnonymous() {
        return classStartOffset != -1;
    }

    /**
     * Class name of this instance creation node.
     * <p>
     * Syntetic name for anonymous class
     * (<tt>#anon#&lt;fileName>#&lt;counter></tt>).
     *
     * @return class name
     */
    public ClassName getClassName() {
        if (isAnonymous()) {
            return new ClassName(classStartOffset, classStartOffset,
                    new Identifier(classStartOffset, classStartOffset, "#anon#" + fileName + "#" + classCounter)); // NOI18N
        }
        return className;
    }

    /**
     * List of expressions that were given to the the constructor.
     *
     * @return list of expressions that were given to the the constructor
     */
    public List<Expression> ctorParams() {
        return Collections.unmodifiableList(ctorParams);
    }

    @CheckForNull
    public Expression getSuperClass() {
        return superClass;
    }

    public List<Expression> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    @CheckForNull
    public Block getBody() {
        return body;
    }

    @Override
    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public boolean isAttributed() {
        return !attributes.isEmpty();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new "); // NOI18N
        boolean anonymous = isAnonymous();
        if (anonymous) {
            getAttributes().forEach(attribute -> sb.append(attribute).append(" ")); // NOI18N
            sb.append("class"); // NOI18N
        } else {
            sb.append(className);
        }
        if (!ctorParams.isEmpty()) {
            sb.append('('); // NOI18N
            joinExpressions(sb, ctorParams);
            sb.append(')'); // NOI18N
        }
        if (superClass != null) {
            assert className == null : className;
            assert anonymous;
            sb.append(" extends "); // NOI18N
            sb.append(superClass);
        }
        if (!interfaces.isEmpty()) {
            assert className == null : className;
            assert anonymous;
            sb.append(" implements "); // NOI18N
            joinExpressions(sb, interfaces);
        }
        if (body != null) {
            assert className == null : className;
            assert anonymous;
            sb.append(body);
        }
        return sb.toString();
    }

    private void joinExpressions(StringBuilder sb, List<Expression> expressions) {
        boolean first = true;
        for (Expression expression : expressions) {
            if (first) {
                first = false;
            } else {
                sb.append(", "); // NOI18N
            }
            sb.append(expression);
        }
    }

}
