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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.MethodDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
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
        retval.add(createMagicMethod(type, "__callStatic", Modifier.PUBLIC | Modifier.STATIC, "$name", "$arguments")); //NOI18N
        retval.add(createMagicMethod(type, "__set_state", Modifier.PUBLIC | Modifier.STATIC, "$array")); //NOI18N
        retval.add(createMagicMethod(type, "__call",  Modifier.PUBLIC, "$name", "$arguments")); //NOI18N
        retval.add(createMagicMethod(type, "__clone",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__construct",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__destruct",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__invoke",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__get",  Modifier.PUBLIC, "$name")); //NOI18N
        retval.add(createMagicMethod(type, "__set",  Modifier.PUBLIC, "$name", "$value")); //NOI18N
        retval.add(createMagicMethod(type, "__isset",  Modifier.PUBLIC, "$name")); //NOI18N
        retval.add(createMagicMethod(type, "__unset",  Modifier.PUBLIC, "$name")); //NOI18N
        retval.add(createMagicMethod(type, "__sleep",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__wakeup",  Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__toString",  Modifier.PUBLIC)); //NOI18N
        // PHP 7.4 New custom object serialization mechanism
        // https://wiki.php.net/rfc/custom_object_serialization
        retval.add(createMagicMethod(type, "__serialize", Modifier.PUBLIC)); //NOI18N
        retval.add(createMagicMethod(type, "__unserialize", Modifier.PUBLIC, "array $data")); //NOI18N
        return retval;
    }

    public static MethodElement createMagicMethod(final TypeElement type, String methodName, int flags, String... arguments) {
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

    private static List<ParameterElement> fromParameterNames(String... names) {
        List<ParameterElement> retval = new ArrayList<>();
        for (String parameterName : names) {
            retval.add(new ParameterElementImpl(parameterName, null, 0, Collections.<TypeResolver>emptySet(), true, true, false, false, false, 0));
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
        boolean isUnionType = node.getFunction().getReturnType() instanceof UnionType;
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
                    TypeResolverImpl.parseTypes(VariousUtils.getReturnType(fileQuery.getResult().getProgram(), node.getFunction())), isUnionType),
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
    public boolean isReturnUnionType() {
        return functionSupport.isReturnUnionType();
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
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getName()).append(Separator.SEMICOLON); //NOI18N
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
        sb.append(getOffset()).append(Separator.SEMICOLON);
        List<ParameterElement> parameterList = getParameters();
        for (int idx = 0; idx < parameterList.size(); idx++) {
            ParameterElementImpl parameter = (ParameterElementImpl) parameterList.get(idx);
            if (idx > 0) {
                sb.append(Separator.COMMA);
            }
            sb.append(parameter.getSignature());
        }
        sb.append(Separator.SEMICOLON);
        for (TypeResolver typeResolver : getReturnTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            sb.append(resolverImpl.getSignature());
        }
        sb.append(Separator.SEMICOLON);
        sb.append(getPhpModifiers().toFlags()).append(Separator.SEMICOLON);
        sb.append(isDeprecated() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getFilenameUrl()).append(Separator.SEMICOLON);
        sb.append(isReturnUnionType()? 1 : 0).append(Separator.SEMICOLON);
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

        public ReturnTypesFromSignature(MethodSignatureParser methodSignatureParser) {
            this.methodSignatureParser = methodSignatureParser;
            this.isUnionType = methodSignatureParser.isUnionType();
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

    }
}
