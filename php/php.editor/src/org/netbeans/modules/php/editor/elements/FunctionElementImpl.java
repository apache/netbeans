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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class FunctionElementImpl extends FullyQualifiedElementImpl implements FunctionElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_BASE;
    private final BaseFunctionElementSupport functionSupport;

    private FunctionElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery,
            final BaseFunctionElementSupport.Parameters parameters,
            final BaseFunctionElementSupport.ReturnTypes returnTypes,
            final boolean isDeprecated) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(),
                fileUrl, offset, elementQuery, isDeprecated);
        this.functionSupport = new BaseFunctionElementSupport(parameters, returnTypes);
    }

    public static Set<FunctionElement> fromSignature(final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexQuery, indexResult);
    }

    public static Set<FunctionElement> fromSignature(
            final NameKind query, final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<FunctionElement> retval = values.length > 0 ? new HashSet<>() : Collections.<FunctionElement>emptySet();
        for (String val : values) {
            final FunctionElement fnc = fromSignature(query, indexQuery, Signature.get(val));
            if (fnc != null) {
                retval.add(fnc);
            }
        }
        return retval;
    }

    public static FunctionElement fromSignature(final NameKind query, IndexQueryImpl indexScopeQuery, Signature sig) {
        Parameters.notNull("NameKind query: can't be null", query);
        FunctionSignatureParser signParser = new FunctionSignatureParser(sig);
        FunctionElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new FunctionElementImpl(signParser.getQualifiedName(),
                    signParser.getOffset(), signParser.getFileUrl(),
                    indexScopeQuery,  new ParametersFromSignature(signParser), new ReturnTypesFromSignature(signParser),
                    signParser.isDeprecated());
        }
        return retval;
    }

    public static FunctionElement fromNode(final NamespaceElement namespace, final FunctionDeclaration node, final ElementQuery.File fileQuery) {
        Parameters.notNull("node", node);
        Parameters.notNull("fileQuery", fileQuery);
        FunctionDeclarationInfo info = FunctionDeclarationInfo.create(node);
        final QualifiedName fullyQualifiedName = namespace != null ? namespace.getFullyQualifiedName() : QualifiedName.createForDefaultNamespaceName();
        return new FunctionElementImpl(
                fullyQualifiedName.append(info.getName()), info.getRange().getStart(),
                fileQuery.getURL().toExternalForm(), fileQuery, BaseFunctionElementSupport.ParametersImpl.create(info.getParameters()),
                BaseFunctionElementSupport.ReturnTypesImpl.create(TypeResolverImpl.parseTypes(VariousUtils.getReturnType(fileQuery.getResult().getProgram(), node)), node.getReturnType()),
                VariousUtils.isDeprecatedFromPHPDoc(fileQuery.getResult().getProgram(), node));
    }

    private static boolean matchesQuery(final NameKind query, FunctionSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        return (query instanceof NameKind.Empty) || query.matchesName(FunctionElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return FunctionElement.KIND;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getName()).append(Separator.SEMICOLON); //NOI18N
        sb.append(getSignatureLastPart());

        checkFunctionSignature(sb);
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
        sb.append(isReturnUnionType() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(isReturnIntersectionType() ? 1 : 0).append(Separator.SEMICOLON);
        sb.append(getDeclaredReturnType()).append(Separator.SEMICOLON);
        return sb.toString();
    }

    private void checkFunctionSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            FunctionSignatureParser parser = new FunctionSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getParameters().size() == parser.getParameters().size();
            assert getReturnTypes().size() == parser.getReturnTypes().size();
            assert getDeclaredReturnType().equals(parser.getDeclaredReturnType());
        }
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
        return this.functionSupport.getDeclaredReturnType();
    }

    @Override
    public boolean isReturnUnionType() {
        return this.functionSupport.isReturnUnionType();
    }

    @Override
    public boolean isReturnIntersectionType() {
        return this.functionSupport.isReturnIntersectionType();
    }

    @Override
    public boolean isAnonymous() {
        return CodeUtils.isSyntheticFunctionName(getName());
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

    //~ Inner classes

    private static class FunctionSignatureParser {
        private final Signature signature;

        FunctionSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(5), signature.string(1));
        }

        int getOffset() {
            return signature.integer(2);
        }

        List<ParameterElement> getParameters() {
            return ParameterElementImpl.parseParameters(signature.string(3));
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

        boolean isReturnUnionType() {
            return signature.integer(8) == 1;
        }

        boolean isReturnIntersectionType() {
            return signature.integer(9) == 1;
        }

        String getDeclaredReturnType() {
            return signature.string(10);
        }
    }

    private static final class ParametersFromSignature implements BaseFunctionElementSupport.Parameters {
        private final FunctionSignatureParser functionSignatureParser;
        //@GuardedBy("this")
        private List<ParameterElement> retrievedParameters = null;

        public ParametersFromSignature(FunctionSignatureParser functionSignatureParser) {
            this.functionSignatureParser = functionSignatureParser;
        }

        @Override
        public synchronized List<ParameterElement> getParameters() {
            if (retrievedParameters == null) {
                retrievedParameters = functionSignatureParser.getParameters();
            }
            return retrievedParameters;
        }
    }

    private static final class ReturnTypesFromSignature implements BaseFunctionElementSupport.ReturnTypes {
        private final FunctionSignatureParser functionSignatureParser;
        //@GuardedBy("this")
        private Set<TypeResolver> retrievedReturnTypes = null;
        private final boolean isUnionType;
        private final boolean isIntersectionType;
        @NullAllowed
        private final String declaredReturnType;

        public ReturnTypesFromSignature(FunctionSignatureParser functionSignatureParser) {
            this.functionSignatureParser = functionSignatureParser;
            this.isUnionType = functionSignatureParser.isReturnUnionType();
            this.isIntersectionType = functionSignatureParser.isReturnIntersectionType();
            this.declaredReturnType = functionSignatureParser.getDeclaredReturnType();
        }

        @Override
        public synchronized Set<TypeResolver> getReturnTypes() {
            if (retrievedReturnTypes == null) {
                retrievedReturnTypes = functionSignatureParser.getReturnTypes();
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

        @Override
        public String getDeclaredReturnType() {
            return declaredReturnType;
        }

    }
}
