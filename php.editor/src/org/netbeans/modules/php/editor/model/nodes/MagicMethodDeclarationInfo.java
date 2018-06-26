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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class MagicMethodDeclarationInfo extends ASTNodeInfo<PHPDocMethodTag> {
    private final List<ParameterElement> parameters = new LinkedList<>();
    private String returnType;
    private String methodName;
    private int offset;
    private int typeOffset;

    MagicMethodDeclarationInfo(PHPDocMethodTag node) {
        super(node);
        String[] parts = node.getValue().trim().split("\\s+", 3); //NOI18N
        if (parts.length == 1 || (parts.length > 0 && parts[0].trim().indexOf("(") > 0)) { //NOI18N
            // expect that the type is void
            returnType = Type.VOID;
            String[] methodNames = parts[0].split("[(, ]", 2); //NOI18N
            if (methodNames.length > 0) {
                methodName = methodNames[0];
                offset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(methodName);
            }
        } else if (parts.length >= 2) {
            String[] methodNames = parts[1].split("[(, ]", 2); //NOI18N
            if (parts[0].length() > 0 && methodNames.length > 0) {
                returnType = parts[0];
                methodName = methodNames[0];
                offset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(methodName);
                typeOffset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(returnType);
            }
        }

        for (PHPDocVarTypeTag parameter : node.getParameters()) {
            Collection<Pair<QualifiedName, Boolean>> names = new LinkedList<>();
            for (PHPDocTypeNode type : parameter.getTypes()) {
                String typeName = type.getValue();
                boolean isNullableType = CodeUtils.isNullableType(typeName);
                if (isNullableType) {
                    typeName = typeName.substring(1);
                }
                QualifiedName qualifiedName = QualifiedName.create(typeName);
                names.add(Pair.of(qualifiedName, isNullableType));
            }
            Set<TypeResolver> types = TypeResolverImpl.forNames(names);
            String name = parameter.getVariable().getValue();
            String[] split = parameter.getValue().split("="); // NOI18N
            String defaultValue = null;
            if (split.length > 1) {
                defaultValue = split[1].trim();
            }
            boolean isMandatory = defaultValue == null;
            boolean isReference = name.startsWith("&"); // NOI18N
            boolean isVariadic = name.startsWith("..."); // NOI18N
            parameters.add(new ParameterElementImpl(name, defaultValue, 0, types, isMandatory, true, isReference, isVariadic));
        }
    }

    @CheckForNull
    public static MagicMethodDeclarationInfo create(PHPDocMethodTag node) {
        MagicMethodDeclarationInfo retval = new MagicMethodDeclarationInfo(node);
        return (retval.methodName != null && retval.returnType != null) ? retval : null;
    }

    public ASTNodeInfo<PHPDocMethodTag> getClassInfo() {
        return new ASTNodeInfo<PHPDocMethodTag>(getOriginalNode()) {

            @Override
            public String getName() {
                return MagicMethodDeclarationInfo.this.getReturnType();
            }

            @Override
            public OffsetRange getRange() {
                return MagicMethodDeclarationInfo.this.getTypeRange();
            }


            @Override
            public Kind getKind() {
                return Kind.CLASS;
            }

            @Override
            public QualifiedName getQualifiedName() {
                return QualifiedName.create(getName());
            }

            @Override
            public PhpElementKind getPhpElementKind() {
                return PhpElementKind.CLASS;
            }
        };
    }

    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public String getName() {
        return getQualifiedName().toName().toString();
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(methodName).toName();
    }

    @Override
    public OffsetRange getRange() {
        return new OffsetRange(offset, offset + getName().length());
    }

    public OffsetRange getTypeRange() {
        return new OffsetRange(typeOffset, typeOffset + getReturnType().length());
    }

    public List<? extends ParameterElement> getParameters() {
        return new ArrayList<>(parameters);
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(PhpModifiers.PUBLIC);
    }
}
