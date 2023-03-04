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
