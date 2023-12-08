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
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.FileElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class FieldElementImpl extends PhpElementImpl implements FieldElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_FIELD;

    private final PhpModifiers modifiers;
    private final TypeElement enclosingType;
    private final Set<TypeResolver> instanceTypes;
    private final Set<TypeResolver> instanceFQTypes;
    private final boolean isAnnotation;
    private final Type.Kind typeKind;
    @NullAllowed
    private final String declaredType;

    private FieldElementImpl(
            final TypeElement enclosingType,
            final String fieldName,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final Set<TypeResolver> instanceTypes,
            final Set<TypeResolver> instanceFQTypes,
            final boolean isDeprecated,
            final boolean isAnnotation,
            Type.Kind typeKind,
            String declaredType
    ) {
        super(FieldElementImpl.getName(fieldName, true), enclosingType.getName(), fileUrl, offset, elementQuery, isDeprecated);
        this.modifiers = PhpModifiers.fromBitMask(flags);
        this.enclosingType = enclosingType;
        this.instanceTypes = instanceTypes;
        this.instanceFQTypes = instanceFQTypes;
        this.isAnnotation = isAnnotation;
        this.typeKind = typeKind;
        this.declaredType = declaredType;
    }

    public static Set<FieldElement> fromSignature(final TypeElement type,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(type, NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<FieldElement> fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<FieldElement> retval = values.length > 0
                ? new HashSet<>() : Collections.<FieldElement>emptySet();
        for (String val : values) {
            final FieldElement field = fromSignature(type, query, indexQuery, Signature.get(val));
            if (field != null) {
                retval.add(field);
            }
        }
        return retval;
    }

    public static FieldElement fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature sig) {
        Parameters.notNull("query", query); //NOI18N
        final FieldSignatureParser signParser = new FieldSignatureParser(sig);
        FieldElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new FieldElementImpl(type, signParser.getFieldName(),
                    signParser.getOffset(), signParser.getFlags(), signParser.getFileUrl(),
                    indexScopeQuery, signParser.getTypes(), signParser.getFQTypes(), signParser.isDeprecated(), signParser.isAnnotation(), signParser.getTypeKind(), signParser.getDeclaredType());

        }
        return retval;
    }

    public static Set<FieldElement> fromNode(TypeElement type, FieldsDeclaration node, ElementQuery.File fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final List<? extends SingleFieldDeclarationInfo> fields = SingleFieldDeclarationInfo.create(node);
        final Set<FieldElement> retval = new HashSet<>();
        for (SingleFieldDeclarationInfo info : fields) {
            final String fieldType = VariousUtils.getFieldTypeFromPHPDoc(fileQuery.getResult().getProgram(), info.getOriginalNode());
            Set<TypeResolver> types = fieldType != null ? TypeResolverImpl.parseTypes(fieldType) : null;
            retval.add(new FieldElementImpl(type, info.getName(), info.getRange().getStart(),
                    info.getAccessModifiers().toFlags(), fileQuery.getURL().toString(), fileQuery,
                    types, types, VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node), false, Type.Kind.fromTypes(fieldType), info.getFieldType()));
        }
        return retval;
    }

    public static FieldElement fromNode(final TypeElement type, final FieldAccess node,
            final Set<TypeResolver> resolvers, final FileElementQuery fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("resolvers", resolvers);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        final ASTNodeInfo<FieldAccess> info = ASTNodeInfo.create(node);
        return new FieldElementImpl(
                type,
                info.getName(),
                info.getRange().getStart(),
                PhpModifiers.PUBLIC,
                fileQuery.getURL().toString(),
                fileQuery,
                resolvers,
                resolvers,
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node),
                false,
                Type.Kind.NORMAL,
                null
        );
    }

    static FieldElement fromFrameworks(final TypeElement type, final PhpType.Field field, final ElementQuery elementQuery) {
        Parameters.notNull("field", field);
        Parameters.notNull("elementQuery", elementQuery);
        // XXX check nullable type?
        final PhpType fldType = field.getType();
        final Set<TypeResolver> typeResolvers = fldType != null
                ? Collections.<TypeResolver>singleton(new TypeResolverImpl(fldType.getFullyQualifiedName(), false))
                : Collections.<TypeResolver>emptySet();
        FieldElementImpl retval = new FieldElementImpl(type, field.getName(), field.getOffset(),
                PhpModifiers.NO_FLAGS, null, elementQuery, typeResolvers, typeResolvers, false, false, Type.Kind.NORMAL, null);
        retval.setFileObject(field.getFile());
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, FieldSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        return (query instanceof NameKind.Empty)
                || query.matchesName(FieldElement.KIND, signParser.getFieldName());
    }


    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        final String noDollarName = getName().substring(1);
        sb.append(noDollarName.toLowerCase()).append(Separator.SEMICOLON);
        sb.append(noDollarName).append(Separator.SEMICOLON);
        sb.append(getOffset()).append(Separator.SEMICOLON);
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON);
        for (TypeResolver typeResolver : getInstanceTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            sb.append(resolverImpl.getSignature());
        }
        sb.append(Separator.SEMICOLON);
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        sb.append(isAnnotation() ? 1 : 0).append(Separator.SEMICOLON);
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return FieldElement.KIND;
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return modifiers;
    }

    @Override
    public TypeElement getType() {
        return enclosingType;
    }

    @Override
    public Set<TypeResolver> getInstanceTypes() {
        return Collections.unmodifiableSet(instanceTypes);
    }

    @Override
    public Set<TypeResolver> getInstanceFQTypes() {
        return Collections.unmodifiableSet(instanceFQTypes);
    }

    @Override
    public boolean isUnionType() {
        return typeKind == Type.Kind.UNION;
    }

    @Override
    public boolean isIntersectionType() {
        return typeKind == Type.Kind.INTERSECTION;
    }

    @Override
    public String getDeclaredType() {
        return declaredType;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            FieldSignatureParser parser = new FieldSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getFieldName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            assert getInstanceTypes().size() == parser.getTypes().size();
            assert getInstanceFQTypes().size() == parser.getFQTypes().size();
            assert isDeprecated() == parser.isDeprecated();
            assert isAnnotation() == parser.isAnnotation();
        }
    }

    @Override
    public String getName(final boolean dollared) {
        return getName(getName(), dollared);
    }

    private static String getName(final String name, final boolean dollared) {
        final boolean startsWithDollar = name.startsWith(VariableElementImpl.DOLLAR_PREFIX);
        if (startsWithDollar == dollared) {
            return name;
        }
        return dollared ? String.format("%s%s", VariableElementImpl.DOLLAR_PREFIX, name) : name.substring(1); //NOI18N
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
    public boolean isAnnotation() {
        return isAnnotation;
    }

    private static class FieldSignatureParser {

        private final Signature signature;

        FieldSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getFieldName() {
            return signature.string(1);
        }

        int getOffset() {
            return signature.integer(2);
        }

        int getFlags() {
            return signature.integer(3);
        }

        @CheckForNull
        String getDeclaredType() {
            return signature.string(4).isEmpty() ? null : signature.string(4);
        }

        @CheckForNull
        String getDeclaredFQType() {
            return signature.string(5).isEmpty() ? null : signature.string(5);
        }

        Set<TypeResolver> getTypes() {
            return TypeResolverImpl.parseTypes(signature.string(4));
        }

        Set<TypeResolver> getFQTypes() {
            return TypeResolverImpl.parseTypes(signature.string(5));
        }

        boolean isDeprecated() {
            return signature.integer(6) == 1;
        }

        String getFileUrl() {
            return signature.string(7);
        }

        boolean isAnnotation() {
            return signature.integer(8) == 1;
        }

        Type.Kind getTypeKind() {
            String types = signature.string(4);
            Type.Kind typeKind = Type.Kind.NORMAL;
            if (types.contains(Type.SEPARATOR)) {
                typeKind = Type.Kind.UNION;
            } else if (types.contains(Type.SEPARATOR_INTERSECTION)) {
                typeKind = Type.Kind.INTERSECTION;
            }
            return typeKind;
        }
    }
}
