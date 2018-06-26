/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.index;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class IdentifierSignatureFactory {

    private IdentifierSignatureFactory() {
    }

    public static IdentifierSignature createIdentifier(Identifier node) {
        String name = node.getName().toLowerCase();
        return new IdentifierSignatureImpl(name);
    }

    public static IdentifierSignature create(String name) {
        return new IdentifierSignatureImpl(name);
    }

    public static IdentifierSignature createDeclaration(Signature sign) {
        String name = sign.string(0);
        int mask = sign.integer(1);
        String typeName = ((mask & IdentifierSignatureImpl.DECLARATION) != 0) ? sign.string(2) : null;
        return new IdentifierSignatureImpl(name, typeName, mask);
    }

    public static IdentifierSignature createInvocation(Signature sign) {
        String name = sign.string(0);
        return new IdentifierSignatureImpl(name);
    }

    public static IdentifierSignature create(MethodScope method) {
        return new IdentifierSignatureImpl(method.getName(),
                method.getPhpModifiers().toFlags(), ElementKind.METHOD, method.getInScope().getName(),
                true, method.getInScope() instanceof ClassScope);
    }

    public static IdentifierSignature create(FieldElement fieldElement) {
        return new IdentifierSignatureImpl(fieldElement.getName(),
                fieldElement.getPhpModifiers().toFlags(), ElementKind.FIELD,
                fieldElement.getInScope().getName(), true, fieldElement.getInScope() instanceof ClassScope);
    }

    public static IdentifierSignature create(ClassConstantElement constantElement) {
        return new IdentifierSignatureImpl(constantElement.getName(),
                constantElement.getPhpModifiers().toFlags(), ElementKind.CONSTANT,
                constantElement.getInScope().getName(), true, constantElement.getInScope() instanceof ClassScope);
    }

    public static IdentifierSignature create(PHPDocTypeNode node) {
        IdentifierSignature result = IdentifierSignature.NONE;
        String type = node.getValue();
        String[] typeParts = type.split("\\\\"); //NOI18N
        if (typeParts.length >= 1) {
            result = new IdentifierSignatureImpl(typeParts[typeParts.length - 1]);
        }
        return result;
    }

    private static final class IdentifierSignatureImpl implements IdentifierSignature {

        private static final int DECLARATION = 0x1; //else invocation or use
        private static final int IFACE_MEMBER = 0x2;
        private static final int CLS_MEMBER = 0x4;
        private static final int MODIFIER_STATIC = 0x8;
        private static final int MODIFIER_ABSTRACT = 0x10;
        private static final int MODIFIER_PROTECTED = 0x20;
        private static final int MODIFIER_PUBLIC = 0x40;
        private static final int KIND_FNC = 0x80;
        private static final int KIND_VAR = 0x100;
        private static final int KIND_CONST = 0x200;
        private static final int KIND_CLASS = 0x400;
        private String name;
        private int mask;
        private String typeName;

        //for invocations
        private IdentifierSignatureImpl(String name) {
            this(name, null, 0);
        }

        private IdentifierSignatureImpl(String name, String typeName, int mask) {
            this.name = name.toLowerCase();
            while (name.startsWith("$")) { //NOI18N
                name = name.substring(1);
            }
            this.mask = mask;
            if (isDeclaration()) {
                this.typeName = typeName;
            }
        }

        private IdentifierSignatureImpl(String name, int modifier,
                ElementKind kind, String typeName, boolean declaration,
                Boolean clsmember) {
            this.name = name.toLowerCase();
            while (name.startsWith("$")) { //NOI18N
                name = name.substring(1);
            }
            if (declaration) {
                mask |= DECLARATION;
            }

            if (clsmember != null && clsmember) {
                mask |= CLS_MEMBER;
            }
            if (clsmember != null && !clsmember) {
                mask |= IFACE_MEMBER;
            }
            switch (kind) {
                case METHOD:
                    mask |= KIND_FNC;
                    break;
                case FIELD:
                    mask |= KIND_VAR;
                    break;
                case CONSTANT:
                    mask |= KIND_CONST;
                    break;
                case CLASS:
                    mask |= KIND_CLASS;
                    break;
                default:
                    throw new IllegalStateException(kind.toString());
            }

            if (BodyDeclaration.Modifier.isAbstract(modifier)) {
                mask |= MODIFIER_ABSTRACT;
            } else if (BodyDeclaration.Modifier.isStatic(modifier)) {
                mask |= MODIFIER_STATIC;
            }

            if (BodyDeclaration.Modifier.isPublic(modifier)) {
                mask |= MODIFIER_PUBLIC;
            } else if (BodyDeclaration.Modifier.isProtected(modifier)) {
                mask |= MODIFIER_PROTECTED;
            }
            if (isDeclaration()) {
                this.typeName = typeName;
            }
        }

        private String getName() {
            return name;
        }

        private String getTypeName() {
            return typeName;
        }

        private boolean isDeclaration() {
            return (mask & DECLARATION) != 0;
        }

        private boolean isClassMember() {
            return (mask & CLS_MEMBER) != 0;
        }

        private boolean isIfaceMember() {
            return (mask & IFACE_MEMBER) != 0;
        }

        private boolean isStatic() {
            return (mask & MODIFIER_STATIC) != 0;
        }

        private boolean isAbstract() {
            return (mask & MODIFIER_ABSTRACT) != 0;
        }

        private boolean isPublic() {
            return (mask & MODIFIER_PUBLIC) != 0;
        }

        private boolean isProtected() {
            return (mask & MODIFIER_PROTECTED) != 0;
        }

        private boolean isPrivate() {
            return !isPublic() && !isProtected();
        }

        private boolean isFunction() {
            return (mask & KIND_FNC) != 0 && (!isClassMember() && !isIfaceMember());
        }

        private boolean isMethod() {
            return (mask & KIND_FNC) != 0 && (isClassMember() || isIfaceMember());
        }

        private boolean isVariable() {
            return (mask & KIND_VAR) != 0 && (!isClassMember() && !isIfaceMember());
        }

        private boolean isField() {
            return (mask & KIND_VAR) != 0 && (isClassMember() || isIfaceMember());
        }

        private boolean isClass() {
            return (mask & KIND_CLASS) != 0;
        }

        private boolean isConstant() {
            return (mask & KIND_CONST) != 0 && (!isClassMember() && !isIfaceMember());
        }

        private boolean isClassConstant() {
            return (mask & KIND_CONST) != 0 && (isClassMember() || isIfaceMember());
        }

        private String getSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append(Signature.encodeItem(name)).append(";"); //NOI18N
            if (mask != 0) {
                sb.append(mask).append(";"); //NOI18N
            } else {
                assert !isDeclaration();
            }
            if (isDeclaration()) {
                sb.append(Signature.encodeItem(typeName)).append(";"); //NOI18N
            }
            return sb.toString();
        }

        @Override
        public void save(IndexDocument indexDocument) {
            indexDocument.addPair(PHPIndexer.FIELD_IDENTIFIER, getSignature(), true, true);
        }

    }

}
