/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a class creation. It can be just calling ctor
 * of existing class or creating new anonymous class.
 * This class holds the class name as an expression and
 * array of constructor parameters
 * <pre>e.g.
 * new MyClass(),
 * new $a('start'),
 * new foo()(1, $a),
 * new class {...},
 * new class(10) extends SomeClass implements SomeInterface {...}
 * </pre>
 */
public class ClassInstanceCreation extends Expression {

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
            @NullAllowed Expression superClass, @NullAllowed List<Expression> interfaces, @NonNull Block body) {
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
    }

    public static ClassInstanceCreation anonymous(String fileName, int classCounter, int start, int end, int classStartOffset, List<Expression> ctorParams,
            Expression superClass, List<Expression> interfaces, Block body) {
        return new ClassInstanceCreation(fileName, classCounter, start, end, classStartOffset, ctorParams, superClass, interfaces, body);
    }

    public boolean isAnonymous() {
        return classStartOffset != -1;
    }

    /**
     * Class name of this instance creation node.
     * <p>
     * Syntetic name for anonymous class (<tt>#anon#&lt;fileName>#&lt;counter></tt>).
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new "); // NOI18N
        boolean anonymous = isAnonymous();
        if (anonymous) {
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
