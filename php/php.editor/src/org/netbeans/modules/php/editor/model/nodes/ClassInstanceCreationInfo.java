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
package org.netbeans.modules.php.editor.model.nodes;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * Info for anonymous classes.
 */
public class ClassInstanceCreationInfo extends ASTNodeInfo<ClassInstanceCreation> {

    public ClassInstanceCreationInfo(ClassInstanceCreation node) {
        super(node);
    }

    public static ClassInstanceCreationInfo create(ClassInstanceCreation classInstanceCreation) {
        assert classInstanceCreation.isAnonymous() : classInstanceCreation;
        return new ClassInstanceCreationInfo(classInstanceCreation);
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

    @Override
    public OffsetRange getRange() {
        ClassInstanceCreation originalNode = getOriginalNode();
        // class name range is used in ClassDeclarationInfo
        // anonymous class doesn't have a class name
        // so, just use the range of "class" instead of range of the original node
        ClassName className = originalNode.getClassName();
        int start = className.getStartOffset();
        int end = start + "class".length(); // NOI18N
        return new OffsetRange(start, end);
    }

    public Expression getSuperClass() {
        return getOriginalNode().getSuperClass();
    }

    public QualifiedName getSuperClassName() {
        final Expression superClass = getSuperClass();
        return superClass != null ? QualifiedName.create(superClass) : null;
    }

    public List<? extends Expression> getInterfaces() {
        return getOriginalNode().getInterfaces();
    }

    public Set<QualifiedName> getInterfaceNames() {
        final Set<QualifiedName> retval = new HashSet<>();
        final List<Expression> interfaes = getOriginalNode().getInterfaces();
        for (Expression iface : interfaes) {
            QualifiedName ifaceName = QualifiedName.create(iface);
            if (ifaceName != null) {
                retval.add(ifaceName);
            }
        }
        return retval;
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(PhpModifiers.NO_FLAGS);
    }

    public Collection<QualifiedName> getUsedTraits() {
        final UsedTraitsVisitor visitor = new UsedTraitsVisitor();
        Block body = getOriginalNode().getBody();
        assert body != null : getOriginalNode();
        body.accept(visitor);
        return visitor.getUsedTraits();
    }

    public List<Attribute> getAttributes() {
        return getOriginalNode().getAttributes();
    }

    //~ Inner classes
    private static final class UsedTraitsVisitor extends DefaultVisitor {

        private final List<UseTraitStatementPart> useParts = new LinkedList<>();


        @Override
        public void visit(UseTraitStatementPart node) {
            useParts.add(node);
        }

        public Collection<QualifiedName> getUsedTraits() {
            Collection<QualifiedName> retval = new HashSet<>();
            for (UseTraitStatementPart useTraitStatementPart : useParts) {
                retval.add(QualifiedName.create(useTraitStatementPart.getName()));
            }
            return retval;
        }

    }

}
