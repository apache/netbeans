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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

/**
 * @author Radek Matous
 */
public class ClassDeclarationInfo extends ASTNodeInfo<ClassDeclaration> {

    ClassDeclarationInfo(ClassDeclaration node) {
        super(node);
    }

    public static ClassDeclarationInfo create(ClassDeclaration classDeclaration) {
        return new ClassDeclarationInfo(classDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

    @Override
    public String getName() {
        ClassDeclaration classDeclaration = getOriginalNode();
        return classDeclaration.getName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getOriginalNode().getName());
    }

    @Override
    public OffsetRange getRange() {
        ClassDeclaration classDeclaration = getOriginalNode();
        Identifier name = classDeclaration.getName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public Expression getSuperClass() {
        return (getOriginalNode().getSuperClass() != null) ? getOriginalNode().getSuperClass() : null;
    }

    public QualifiedName getSuperClassName() {
        final Expression superClass = getSuperClass();
        return (superClass != null) ? QualifiedName.create(superClass) : null;
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
        List<Integer> phpModifiers = new ArrayList<>(getOriginalNode().getModifiers().keySet().size());
        phpModifiers.add(PhpModifiers.PUBLIC);
        for (Modifier modifier : getOriginalNode().getModifiers().keySet()) {
            switch (modifier) {
                case ABSTRACT:
                    phpModifiers.add(PhpModifiers.ABSTRACT);
                    break;
                case FINAL:
                    phpModifiers.add(PhpModifiers.FINAL);
                    break;
                case READONLY:
                    phpModifiers.add(PhpModifiers.READONLY);
                    break;
                case NONE:
                    // no-op
                    break;
                default:
                    assert false : "Handle " + modifier + " modifier"; // NOI18N
                    break;
            }
        }
        return PhpModifiers.fromBitMask(phpModifiers.stream().mapToInt(i->i).toArray());
    }

    public Collection<QualifiedName> getUsedTraits() {
        final UsedTraitsVisitor visitor = new UsedTraitsVisitor();
        getOriginalNode().getBody().accept(visitor);
        return visitor.getUsedTraits();
    }

    public List<Attribute> getAttributes() {
        return getOriginalNode().getAttributes();
    }

}
