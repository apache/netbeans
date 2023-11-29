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
package org.netbeans.modules.php.editor.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class TypeConstantElementImpl extends PhpElementImpl implements TypeConstantElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_CLASS_CONST;
    private final TypeElement enclosingType;
    private final String value;
    private final boolean isMagic;
    private final PhpModifiers modifiers;
    @NullAllowed
    private final String declaredType;

    private TypeConstantElementImpl(
            final TypeElement enclosingType,
            final String declaredType,
            final String constantName,
            final String value,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated) {
        this(enclosingType, declaredType, constantName, value, offset, flags, fileUrl, elementQuery, isDeprecated, false);
    }

    private TypeConstantElementImpl(
            final TypeElement enclosingType,
            final String declaredType,
            final String constantName,
            final String value,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final boolean isDeprecated,
            final boolean isMagic) {
        super(constantName, enclosingType.getName(), fileUrl, offset, elementQuery, isDeprecated);
        this.enclosingType = enclosingType;
        this.declaredType = declaredType;
        this.value = value;
        this.isMagic = isMagic;
        this.modifiers = PhpModifiers.fromBitMask(flags);
    }

    public static Set<TypeConstantElement> getMagicConstants(TypeElement type) {
        Set<TypeConstantElement> retval = new HashSet<>();
        retval.add(createMagicConstant(type, "class")); //NOI18N
        return retval;
    }

    private static TypeConstantElement createMagicConstant(TypeElement type, String constantName) {
        TypeConstantElement retval = new TypeConstantElementImpl(
                type,
                null,
                constantName,
                type.getFullyQualifiedName().toString(),
                0,
                BodyDeclaration.Modifier.PUBLIC,
                type.getFilenameUrl(),
                null,
                type.isDeprecated(),
                true);
        return retval;
    }

    public static Set<TypeConstantElement> fromSignature(final TypeElement type,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(type, NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<TypeConstantElement> fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<TypeConstantElement> retval = values.length > 0
                ? new HashSet<>() : Collections.<TypeConstantElement>emptySet();
        for (final String val : values) {
            final TypeConstantElement constant = fromSignature(type, query, indexScopeQuery, Signature.get(val));
            if (constant != null) {
                retval.add(constant);
            }
        }
        return retval;
    }

    private static TypeConstantElement fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature signature) {
        final ConstantSignatureParser signParser = new ConstantSignatureParser(signature);
        TypeConstantElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new TypeConstantElementImpl(
                    type,
                    signParser.getDeclaredType(),
                    signParser.getConstantName(),
                    signParser.getValue(),
                    signParser.getOffset(),
                    signParser.getFlags(),
                    signParser.getFileUrl(),
                    indexScopeQuery,
                    signParser.isDeprecated());
        }
        return retval;
    }

    public static Set<TypeConstantElement> fromNode(final TypeElement type, ConstantDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final List<? extends ClassConstantDeclarationInfo> consts = ClassConstantDeclarationInfo.create(node);
        final Set<TypeConstantElement> retval = new HashSet<>();
        for (ClassConstantDeclarationInfo info : consts) {
            retval.add(new TypeConstantElementImpl(
                    type, info.getDeclaredType(), info.getName(), info.getValue(), info.getRange().getStart(),
                    info.getAccessModifiers().toFlags(), fileQuery.getURL().toExternalForm(), fileQuery,
                    VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node)));
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, ConstantSignatureParser signParser) {
        Parameters.notNull("query", query); //NOI18N
        return (query instanceof NameKind.Empty)
                || query.matchesName(TypeConstantElement.KIND, signParser.getConstantName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase(Locale.ROOT)).append(Separator.SEMICOLON); // 0
        sb.append(getName()).append(Separator.SEMICOLON); // 1
        sb.append(getOffset()).append(Separator.SEMICOLON); // 2
        sb.append(getValue()).append(Separator.SEMICOLON); // 3
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON); // 4
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON); // 5
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON); // 6
        sb.append((getDeclaredType() == null) ? "" : getDeclaredType()).append(Separator.SEMICOLON); // NOI18N 7
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return TypeConstantElement.KIND;
    }

    @Override
    public TypeElement getType() {
        return enclosingType;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ConstantSignatureParser parser = new ConstantSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getConstantName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            if (getDeclaredType() != null) {
                assert getDeclaredType().equals(parser.getDeclaredType())
                        : "getDeclaredType(): " + getDeclaredType() + ", parser.getDeclaredType(): " + parser.getDeclaredType(); // NOI18N
            }
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @CheckForNull
    @Override
    public String getDeclaredType() {
        return declaredType;
    }

    @Override
    public boolean isStatic() {
        return getPhpModifiers().isStatic();
    }

    @Override
    public boolean isPublic() {
        return getPhpModifiers().isPublic();
    }

    @Override
    public boolean isProtected() {
        return getPhpModifiers().isProtected();
    }

    @Override
    public boolean isPrivate() {
        return getPhpModifiers().isPrivate();
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    @Override
    public boolean isMagic() {
        return isMagic;
    }

    private static class ConstantSignatureParser {

        private final Signature signature;

        ConstantSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getConstantName() {
            return signature.string(1);
        }

        int getOffset() {
            return signature.integer(2);
        }

        String getValue() {
            return signature.string(3);
        }

        boolean isDeprecated() {
            return signature.integer(4) == 1;
        }

        String getFileUrl() {
            return signature.string(5);
        }

        int getFlags() {
            return signature.integer(6);
        }

        @CheckForNull
        String getDeclaredType() {
            String declaredType = signature.string(7);
            if (declaredType.isEmpty()) {
                declaredType = null;
            }
            return declaredType;
        }
    }
}
