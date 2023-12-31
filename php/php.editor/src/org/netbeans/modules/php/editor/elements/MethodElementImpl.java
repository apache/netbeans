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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class MethodElementImpl extends PhpElementImpl implements MethodElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_METHOD;
    public static final String IDX_CONSTRUCTOR_FIELD = PHPIndexer.FIELD_CONSTRUCTOR;
    private final PhpModifiers modifiers;
    private final TypeElement enclosingType;
    private final BaseFunctionElementSupport functionSupport;
    private final boolean isMagic;

    private MethodElementImpl(
            final TypeElement enclosingType,
            final String methodName,
            final boolean isMagic,
            final int offset,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery,
            final BaseFunctionElementSupport.Parameters parameters,
            final BaseFunctionElementSupport.ReturnTypes returnTypes,
            final boolean isDeprecated) {
        super(methodName, enclosingType.getName(), fileUrl, offset, elementQuery, isDeprecated);
        final boolean isFromInterface = enclosingType.isInterface();
        this.modifiers = PhpModifiers.fromBitMask((isFromInterface) ? (flags | Modifier.ABSTRACT | Modifier.PUBLIC) : flags);
        this.isMagic = isMagic;
        this.enclosingType = enclosingType;
        this.functionSupport = new BaseFunctionElementSupport(parameters, returnTypes);
    }

    public static Set<MethodElement> getMagicMethods(final TypeElement type) {
        Set<MethodElement> retval = new HashSet<>();
        retval.add(createMagicMethod(type, "__callStatic", Modifier.PUBLIC | Modifier.STATIC, Arrays.asList(Pair.of(Type.STRING, "$name"), Pair.of(Type.ARRAY, "$arguments")), Type.MIXED)); // NOI18N
        retval.add(createMagicMethod(type, "__call", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.STRING, "$name"), Pair.of(Type.ARRAY, "$arguments")), Type.MIXED)); // NOI18N
        retval.add(createMagicMethod(type, "__invoke", Modifier.PUBLIC, Collections.emptyList(), Type.MIXED)); // NOI18N
        if (!(type instanceof EnumElement)) {
            // Enum can't contain these
            retval.add(createMagicMethod(type, "__set_state", Modifier.PUBLIC | Modifier.STATIC, Arrays.asList(Pair.of(Type.ARRAY, "$properties")), Type.OBJECT)); // NOI18N
            retval.add(createMagicMethod(type, "__clone", Modifier.PUBLIC, Collections.emptyList(), Type.VOID)); // NOI18N
            retval.add(forIntroduceHint(type, "__construct", Modifier.PUBLIC)); // NOI18N constructor can't declare a return type
            retval.add(forIntroduceHint(type, "__destruct", Modifier.PUBLIC)); // NOI18N destructor can't delcare a return type
            retval.add(createMagicMethod(type, "__get", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.STRING, "$name")), Type.MIXED)); // NOI18N
            retval.add(createMagicMethod(type, "__set", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.STRING, "$name"), Pair.of(Type.MIXED, "$value")), Type.VOID)); // NOI18N
            retval.add(createMagicMethod(type, "__isset", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.STRING, "$name")), Type.BOOL)); // NOI18N
            retval.add(createMagicMethod(type, "__unset", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.STRING, "$name")), Type.VOID)); // NOI18N
            retval.add(createMagicMethod(type, "__sleep", Modifier.PUBLIC, Collections.emptyList(), Type.ARRAY)); // NOI18N
            retval.add(createMagicMethod(type, "__wakeup", Modifier.PUBLIC, Collections.emptyList(), Type.VOID)); // NOI18N
            retval.add(createMagicMethod(type, "__toString", Modifier.PUBLIC, Collections.emptyList(), Type.STRING)); // NOI18N
            retval.add(createMagicMethod(type, "__debugInfo", Modifier.PUBLIC, Collections.emptyList(), Type.ARRAY)); // NOI18N
            // PHP 7.4 New custom object serialization mechanism
            // https://wiki.php.net/rfc/custom_object_serialization
            retval.add(createMagicMethod(type, "__serialize", Modifier.PUBLIC, Collections.emptyList(), Type.ARRAY)); // NOI18N
            retval.add(createMagicMethod(type, "__unserialize", Modifier.PUBLIC, Arrays.asList(Pair.of(Type.ARRAY, "$data")), Type.VOID)); // NOI18N
        }
        return retval;
    }

    public static MethodElement forIntroduceHint(final TypeElement type, String methodName, int flags, String... arguments) {
        MethodElement retval = new MethodElementImpl(
                type,
                methodName,
                true,
                0,
                flags,
                type.getFilenameUrl(),
                null,
                BaseFunctionElementSupport.ParametersImpl.create(fromParameterNames(arguments)),
                BaseFunctionElementSupport.ReturnTypes.NONE,
                type.isDeprecated());
        return retval;
    }

    public static MethodElement createMagicMethod(final TypeElement type, String methodName, int flags, List<Pair<String, String>> arguments, String returnType) {
        MethodElement retval = new MethodElementImpl(
                type,
                methodName,
                true,
                0,
                flags,
                type.getFilenameUrl(),
                null,
                BaseFunctionElementSupport.ParametersImpl.create(fromParameterNames(arguments)),
                BaseFunctionElementSupport.ReturnTypesImpl.create(returnType),
                type.isDeprecated());
        return retval;
    }

    static String getValidType(@NullAllowed String declaredType, @NullAllowed PhpVersion phpVersion) {
        if (declaredType == null) {
            return CodeUtils.EMPTY_STRING;
        }
        String type = declaredType.trim();
        if (phpVersion == null) {
            return type;
        }
        switch (type) {
            case Type.MIXED:
                return phpVersion.hasMixedType() ? type : CodeUtils.EMPTY_STRING;
            case Type.OBJECT:
                return phpVersion.hasObjectType() ? type : CodeUtils.EMPTY_STRING;
            case Type.VOID:
                return phpVersion.hasVoidReturnType() ? type : CodeUtils.EMPTY_STRING;
            default:
                return type;
        }
    }

    private static List<ParameterElement> fromParameterNames(String... names) {
        List<ParameterElement> retval = new ArrayList<>();
        for (String parameterName : names) {
            ParameterElement parameterElement = new ParameterElementImpl.Builder(parameterName)
                    .isMagicMethod(false)
                    .isMandatory(true)
                    .isRawType(true)
                    .build();
            retval.add(parameterElement);
        }
        return retval;
    }

    private static List<ParameterElement> fromParameterNames(List<Pair<String, String>> parameterTypeAndNamePairs) {
        List<ParameterElement> retval = new ArrayList<>();
        for (Pair<String, String> parameterTypeAndName : parameterTypeAndNamePairs) {
            String declaredType = parameterTypeAndName.first();
            String parameterName = parameterTypeAndName.second();
            ParameterElement parameterElement = new ParameterElementImpl.Builder(parameterName)
                    .isMagicMethod(true)
                    .isMandatory(true)
                    .isRawType(declaredType != null)
                    .declaredType(declaredType)
                    .build();
            retval.add(parameterElement);
        }
        return retval;
    }

    public static Set<MethodElement> fromSignature(final TypeElement type,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(type, NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<MethodElement> fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<MethodElement> retval = values.length > 0
                ? new HashSet<MethodElement>() : Collections.<MethodElement>emptySet();
        for (String val : values) {
            final MethodElement method = fromSignature(type, query, indexQuery, Signature.get(val));
            if (method != null) {
                retval.add(method);
            }
        }
        return retval;
    }

    private static MethodElement fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final Signature sig) {
        Parameters.notNull("NameKind query: can't be null", query);
        final MethodSignatureParser signParser = new MethodSignatureParser(sig);
        MethodElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new MethodElementImpl(type, signParser.getMethodName(), false,
                    signParser.getOffset(), signParser.getFlags(), signParser.getFileUrl(),
                    indexScopeQuery, new ParametersFromSignature(signParser), new ReturnTypesFromSignature(signParser), signParser.isDeprecated());
        }
        return retval;
    }

    public static MethodElement fromNode(final TypeElement type, final MethodDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("type", type);
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        MethodDeclarationInfo info = MethodDeclarationInfo.create(fileQuery.getResult().getProgram(), node, type.isInterface());
        return new MethodElementImpl(
                type,
                info.getName(),
                false,
                info.getRange().getStart(),
                info.getAccessModifiers().toFlags(),
                fileQuery.getURL().toExternalForm(),
                fileQuery,
                BaseFunctionElementSupport.ParametersImpl.create(info.getParameters()),
                BaseFunctionElementSupport.ReturnTypesImpl.create(
                    TypeResolverImpl.parseTypes(VariousUtils.getReturnType(fileQuery.getResult().getProgram(), node.getFunction())), node.getFunction().getReturnType()),
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node.getFunction()));
    }

    private static boolean matchesQuery(final NameKind query, MethodSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        return (query instanceof NameKind.Empty)
                || query.matchesName(MethodElement.KIND, signParser.getMethodName());
    }

    public static Set<MethodElement> fromConstructorSignature(final TypeElement type,
            final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(MethodElementImpl.IDX_CONSTRUCTOR_FIELD);
        final Set<MethodElement> retval = new HashSet<>();
        for (String val : values) {
            retval.add(fromConstructorSignature(type, indexQuery, indexResult, Signature.get(val)));
        }
        return retval;
    }

    public static MethodElement fromConstructorSignature(final TypeElement type,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult, final Signature sig) {
        final MethodSignatureParser signParser = new MethodSignatureParser(sig);
        final MethodElement retval = new MethodElementImpl(type, MethodElementImpl.CONSTRUCTOR_NAME, false,
                signParser.getOffset(), signParser.getFlags(), indexResult.getUrl().toString(),
                indexScopeQuery, new ParametersFromSignature(signParser), new ReturnTypesFromSignature(signParser), signParser.isDeprecated());
        return retval;
    }

    @Override
    public List<ParameterElement> getParameters() {
        return this.functionSupport.getParameters();
    }

    @Override
    public Collection<TypeResolver> getReturnTypes() {
        return this.functionSupport.getReturnTypes();
    }

    @Override
    public String getDeclaredReturnType() {
        return functionSupport.getDeclaredReturnType();
    }

    @Override
    public boolean isReturnUnionType() {
        return functionSupport.isReturnUnionType();
    }

    @Override
    public boolean isReturnIntersectionType() {
        return functionSupport.isReturnIntersectionType();
    }

    @Override
    public String asString(PrintAs as) {
        return asString(as, TypeNameResolverImpl.forNull());
    }

    @Override
    public String asString(PrintAs as, TypeNameResolver typeNameResolver) {
        return this.functionSupport.asString(as, this, typeNameResolver);
    }

    @Override
    public String asString(PrintAs as, TypeNameResolver typeNameResolver, PhpVersion phpVersion) {
        return this.functionSupport.asString(as, this, typeNameResolver, phpVersion);
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return MethodElement.KIND;
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
    public boolean isConstructor() {
        final Exact exactName = NameKind.exact(getName());
        return exactName.matchesName(getPhpElementKind(), CONSTRUCTOR_NAME)
                || exactName.matchesName(getPhpElementKind(), getType().getName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase(Locale.ROOT)).append(Separator.SEMICOLON); // 0: lower case name
        sb.append(getName()).append(Separator.SEMICOLON); // 1: name
        sb.append(getSignatureLastPart());
        checkSignature(sb);
        return sb.toString();
    }

    public String getConstructorSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType().getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getType().getName()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getSignatureLastPart());
        checkConstructorSignature(sb);
        return sb.toString();
    }

    private String getSignatureLastPart() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOffset()).append(Separator.SEMICOLON); // 2: offset
        List<ParameterElement> parameterList = getParameters();
        for (int idx = 0; idx < parameterList.size(); idx++) {
            ParameterElementImpl parameter = (ParameterElementImpl) parameterList.get(idx);
            if (idx > 0) {
                sb.append(Separator.COMMA);
            }
            sb.append(parameter.getSignature()); // 3: parameter
        }
        sb.append(Separator.SEMICOLON);
        for (TypeResolver typeResolver : getReturnTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            sb.append(resolverImpl.getSignature()); // 4: return types
        }
        sb.append(Separator.SEMICOLON);
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON); // 5: flags
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON); // 6: isDeprecated
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON); // 7: file name URL
        sb.append(isReturnUnionType()? 1 : 0).append(Separator.SEMICOLON); // 8: isReturnUnionType
        sb.append(isReturnIntersectionType() ? 1 : 0).append(Separator.SEMICOLON); // 9: isReturnIntersectionType
        sb.append(getDeclaredReturnType()).append(Separator.SEMICOLON); // 10: declared return type
        return sb.toString();
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

    private static class MethodSignatureParser {

        private final Signature signature;

        MethodSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getMethodName() {
            return signature.string(1);
        }

        int getOffset() {
            return signature.integer(2);
        }

        List<ParameterElement> getParameters() {
            return ParameterElementImpl.parseParameters(signature.string(3));
        }

        int getFlags() {
            return signature.integer(5);
        }

        Set<TypeResolver> getReturnTypes() {
            return TypeResolverImpl.parseTypes(signature.string(4));
        }

        boolean isDeprecated() {
            return signature.integer(6) == 1;
        }

        String getFileUrl() {
            return signature.string(7);
        }

        boolean isUnionType() {
            return signature.integer(8) == 1;
        }

        boolean isIntersectionType() {
            return signature.integer(9) == 1;
        }

        String getDeclaredReturnType() {
            return signature.string(10);
        }
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            MethodSignatureParser parser = new MethodSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getMethodName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            assert getParameters().size() == parser.getParameters().size();
            assert getReturnTypes().size() == parser.getReturnTypes().size();
            assert getDeclaredReturnType().equals(parser.getDeclaredReturnType());
        }
    }

    private void checkConstructorSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            MethodSignatureParser parser = new MethodSignatureParser(Signature.get(retval));
            assert getName().equals(CONSTRUCTOR_NAME);
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            assert getParameters().size() == parser.getParameters().size();
        }
    }

    private static final class ParametersFromSignature implements BaseFunctionElementSupport.Parameters {
        private final MethodSignatureParser methodSignatureParser;
        //@GuardedBy("this")
        private List<ParameterElement> retrievedParameters = null;

        public ParametersFromSignature(MethodSignatureParser methodSignatureParser) {
            this.methodSignatureParser = methodSignatureParser;
        }

        @Override
        public synchronized List<ParameterElement> getParameters() {
            if (retrievedParameters == null) {
                retrievedParameters = methodSignatureParser.getParameters();
            }
            return retrievedParameters;
        }
    }

    private static final class ReturnTypesFromSignature implements BaseFunctionElementSupport.ReturnTypes {
        private final MethodSignatureParser methodSignatureParser;
        //@GuardedBy("this")
        private Set<TypeResolver> retrievedReturnTypes = null;
        private final boolean isUnionType;
        private final boolean isIntersectionType;
        @NullAllowed
        private final String declaredReturnType;

        public ReturnTypesFromSignature(MethodSignatureParser methodSignatureParser) {
            this.methodSignatureParser = methodSignatureParser;
            this.isUnionType = methodSignatureParser.isUnionType();
            this.isIntersectionType = methodSignatureParser.isIntersectionType();
            this.declaredReturnType = methodSignatureParser.getDeclaredReturnType();
        }

        @Override
        public synchronized Set<TypeResolver> getReturnTypes() {
            if (retrievedReturnTypes == null) {
                retrievedReturnTypes = methodSignatureParser.getReturnTypes();
            }
            return retrievedReturnTypes;
        }

        @Override
        public boolean isUnionType() {
            return isUnionType;
        }

        @Override
        public boolean isIntersectionType() {
            return isIntersectionType;
        }

        @CheckForNull
        @Override
        public String getDeclaredReturnType() {
            return declaredReturnType;
        }
    }
}
