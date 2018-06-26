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

package org.netbeans.modules.php.editor.model.nodes;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

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
        return getOriginalNode().getInterfaes();
    }

    public Set<QualifiedName> getInterfaceNames() {
        final Set<QualifiedName> retval = new HashSet<>();
        final List<Expression> interfaes = getOriginalNode().getInterfaes();
        for (Expression iface : interfaes) {
            QualifiedName ifaceName = QualifiedName.create(iface);
            if (ifaceName != null) {
                retval.add(ifaceName);
            }
        }
        return retval;
    }

    public PhpModifiers getAccessModifiers() {
        Modifier modifier = getOriginalNode().getModifier();

        if (modifier.equals(Modifier.ABSTRACT)) {
            return PhpModifiers.fromBitMask(PhpModifiers.PUBLIC, PhpModifiers.ABSTRACT);
        } else if (modifier.equals(Modifier.FINAL)) {
            return PhpModifiers.fromBitMask(PhpModifiers.PUBLIC, PhpModifiers.FINAL);
        }
        return PhpModifiers.fromBitMask(PhpModifiers.PUBLIC);
    }

    public Collection<QualifiedName> getUsedTraits() {
        final UsedTraitsVisitor visitor = new UsedTraitsVisitor();
        getOriginalNode().getBody().accept(visitor);
        return visitor.getUsedTraits();
    }

    private static class UsedTraitsVisitor extends DefaultVisitor {
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
