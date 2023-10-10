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
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class TypeNameResolverImpl implements TypeNameResolver {

    public static TypeNameResolver forNull() {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                return qualifiedName;
            }
        };
    }

    public static TypeNameResolver forChainOf(final List<TypeNameResolver> typeNameResolvers) {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                QualifiedName result = qualifiedName;
                for (TypeNameResolver nameResolver : typeNameResolvers) {
                    result = nameResolver.resolve(result);
                }
                return result;
            }
        };
    }

    public static TypeNameResolver forFullyQualifiedName(final Scope scope, final int offset) {
        return new FullyQualifiedTypeNameResolver(scope, offset);
    }

    public static TypeNameResolver forQualifiedName(final Scope scope, final int offset) {
        return new CommonQualifiedTypeNameResolver(scope, offset);
    }

    public static TypeNameResolver forUnqualifiedName() {
        return new TypeNameResolver() {

            @Override
            public QualifiedName resolve(final QualifiedName qualifiedName) {
                return qualifiedName.toName();
            }
        };
    }

    public static TypeNameResolver forSmartName(final Scope scope, final int offset) {
        return new SmartQualifiedTypeNameResolver(scope, offset);
    }





    private abstract static class BaseTypeNameResolver extends TypeNameResolverImpl {
        private final Scope scope;
        private final int offset;

        public BaseTypeNameResolver(final Scope scope, final int offset) {
            this.scope = scope;
            this.offset = offset;
        }

        protected abstract QualifiedName processFullyQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        protected abstract QualifiedName processQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        protected abstract QualifiedName processUnQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope);

        @Override
        public QualifiedName resolve(final QualifiedName qualifiedName) {
            QualifiedName result = qualifiedName;
            if (!VariousUtils.isSpecialClassName(qualifiedName.getName()) && !Type.isPrimitive(qualifiedName.toString())) {
                result = processExactType(qualifiedName);
            }
            return result;
        }

        protected int getOffset() {
            return offset;
        }

        private QualifiedName processExactType(final QualifiedName qualifiedName) {
            QualifiedName result = qualifiedName;
            NamespaceScope namespaceScope = retrieveNamespaceScope();
            if (namespaceScope != null) {
                if (qualifiedName.getKind().isFullyQualified()) {
                    result = processFullyQualifiedName(qualifiedName, namespaceScope);
                } else if (qualifiedName.getKind().isQualified()) {
                    result = processQualifiedName(qualifiedName, namespaceScope);
                } else {
                    result = processUnQualifiedName(qualifiedName, namespaceScope);
                }
            }
            return result;
        }

        private NamespaceScope retrieveNamespaceScope() {
            NamespaceScope result = null;
            Scope inScope = scope;
            while (inScope != null && !(inScope instanceof NamespaceScope)) {
                inScope = inScope.getInScope();
            }
            if (inScope != null) {
                result = (NamespaceScope) inScope;
            }
            return result;
        }
    }





    private static class FullyQualifiedTypeNameResolver extends BaseTypeNameResolver {

        public FullyQualifiedTypeNameResolver(final Scope scope, final int offset) {
            super(scope, offset);
        }

        @Override
        protected QualifiedName processFullyQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            return fullyQualifiedName;
        }

        @Override
        protected QualifiedName processQualifiedName(final QualifiedName qualifiedName, final NamespaceScope namespaceScope) {
            return resolveFullyQualifiedName(qualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processUnQualifiedName(final QualifiedName unQualifiedName, final NamespaceScope namespaceScope) {
            return resolveFullyQualifiedName(unQualifiedName, namespaceScope);
        }

        private QualifiedName resolveFullyQualifiedName(final QualifiedName qualifiedName, final NamespaceScope namespaceScope) {
            QualifiedName result = qualifiedName;
            String firstSegmentName = qualifiedName.getSegments().getFirst();
            UseScope matchedUseScope = null;
            int lastOffset = -1;
            for (UseScope useElement : namespaceScope.getAllDeclaredSingleUses()) {
                // trying to make a FQ from exact use element, they are FQ by default
                if (useElement.getNameRange().containsInclusive(getOffset())) {
                    result = QualifiedName.create(true, qualifiedName.getSegments());
                    break;
                } else if (useElement.getOffset() < getOffset()) {
                    AliasedName aliasName = useElement.getAliasedName();
                    if (aliasName != null) {
                        if (firstSegmentName.equals(aliasName.getAliasName())) {
                            matchedUseScope = useElement;
                            continue;
                        }
                    } else {
                        if (lastOffset < useElement.getOffset() && (useElement.getName().equals(firstSegmentName)
                                || useElement.getName().endsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR + firstSegmentName))) {
                            matchedUseScope = useElement;
                            lastOffset = useElement.getOffset();
                        }
                    }
                }
            }
            if (matchedUseScope != null) {
                result = resolveForMatchedUseScope(result, matchedUseScope);
            } else if (!result.getKind().isFullyQualified()) {
                String fullNamespaceName = namespaceScope.getNamespaceName().toString();
                if (result.getKind().isQualified()) {
                    fullNamespaceName += fullNamespaceName.trim().isEmpty() ? "" : NamespaceDeclarationInfo.NAMESPACE_SEPARATOR;
                    fullNamespaceName += result.getNamespaceName();
                }
                result = QualifiedName.createFullyQualified(result.getName(), fullNamespaceName);
            }
            return result;
        }

        private QualifiedName resolveForMatchedUseScope(final QualifiedName qualifiedName, final UseScope matchedUseScope) {
            QualifiedName result = qualifiedName;
            final ArrayList<String> segments = new ArrayList<>();
            for (StringTokenizer st = new StringTokenizer(matchedUseScope.getName(), NamespaceDeclarationInfo.NAMESPACE_SEPARATOR); st.hasMoreTokens();) {
                String token = st.nextToken();
                segments.add(token);
            }
            final List<String> origName = result.getSegments();
            for (int i = 1; i < origName.size(); i++) {
                segments.add(origName.get(i));
            }
            return QualifiedName.create(true, segments);
        }

    }





    private interface QualifiedTypeNameResolver {

        QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope);

    }




    private static class CommonQualifiedTypeNameResolver extends BaseTypeNameResolver implements QualifiedTypeNameResolver {

        public CommonQualifiedTypeNameResolver(Scope scope, int offset) {
            super(scope, offset);
        }

        @Override
        protected QualifiedName processFullyQualifiedName(QualifiedName fullyQualifiedName, NamespaceScope namespaceScope) {
            return new FullyQualifiedNameProcessor(this, getOffset()).process(fullyQualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processQualifiedName(QualifiedName qualifiedName, NamespaceScope namespaceScope) {
            return resolveNonFullyQualifiedName(qualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processUnQualifiedName(QualifiedName unQualifiedName, NamespaceScope namespaceScope) {
            return resolveNonFullyQualifiedName(unQualifiedName, namespaceScope);
        }

        @Override
        public QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope) {
            int skipLength = FullyQualifiedNameProcessor.countSkipLength(matchedUseScope);
            return QualifiedName.create(fullyQualifiedName.toString().substring(skipLength));
        }

        private QualifiedName resolveNonFullyQualifiedName(final QualifiedName nonFullyQualifiedName, final NamespaceScope namespaceScope) {
            QualifiedName result = nonFullyQualifiedName;
            UseScope matchedUseScope = getMatchedUseScopeForNonFullyQualifiedName(nonFullyQualifiedName, namespaceScope);
            if (matchedUseScope == null) {
                // passed qualified name is not valid, so construct QN with current NS
                result = namespaceScope.getNamespaceName().append(nonFullyQualifiedName);
            }
            return result;
        }

        private UseScope getMatchedUseScopeForNonFullyQualifiedName(final QualifiedName nonFullyQualifiedName, final NamespaceScope namespaceScope) {
            UseScope result = null;
            String firstSegmentName = nonFullyQualifiedName.getSegments().getFirst();
            int lastOffset = -1;
            for (UseScope useScope : namespaceScope.getAllDeclaredSingleUses()) {
                if (useScope.getOffset() < getOffset()) {
                    AliasedName aliasName = useScope.getAliasedName();
                    if (aliasName != null) {
                        if (firstSegmentName.equals(aliasName.getAliasName())) {
                            result = useScope;
                            continue;
                        }
                    } else {
                        if (lastOffset < useScope.getOffset() && useScope.getName().endsWith(firstSegmentName)) {
                            result = useScope;
                            lastOffset = useScope.getOffset();
                        }
                    }
                }
            }
            return result;
        }
    }





    @NbBundle.Messages({
        "# {0} - Class name",
        "IllegalArgument=Only fully-qualified names can be resolved by {0}"
    })
    private static class SmartQualifiedTypeNameResolver extends BaseTypeNameResolver implements QualifiedTypeNameResolver {

        public SmartQualifiedTypeNameResolver(final Scope scope, final int offset) {
            super(scope, offset);
        }

        @Override
        public QualifiedName resolveForUseScope(final QualifiedName fullyQualifiedName, final UseScope matchedUseScope) {
            QualifiedName result = fullyQualifiedName;
            if (matchedUseScope != null) {
                int skipLength = FullyQualifiedNameProcessor.countSkipLength(matchedUseScope);
                result = QualifiedName.create(fullyQualifiedName.toString().substring(skipLength));
            }
            return result;
        }

        @Override
        protected QualifiedName processFullyQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            return new FullyQualifiedNameProcessor(this, getOffset()).process(fullyQualifiedName, namespaceScope);
        }

        @Override
        protected QualifiedName processQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            throw new IllegalArgumentException(Bundle.IllegalArgument(this.getClass().getName()));
        }

        @Override
        protected QualifiedName processUnQualifiedName(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            throw new IllegalArgumentException(Bundle.IllegalArgument(this.getClass().getName()));
        }
    }





    private static class FullyQualifiedNameProcessor {
        private final int offset;
        private final QualifiedTypeNameResolver qualifiedTypeNameResolver;

        public static int countSkipLength(final UseScope matchedUseElement) {
            int result = NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length();
            if (matchedUseElement != null) {
                List<String> segments = createSegments(matchedUseElement);
                if (!segments.isEmpty()) {
                    result += QualifiedName.create(true, segments).toString().length();
                }
            }
            return result;
        }

        private static List<String> createSegments(final UseScope matchedUseElement) {
            List<String> segments = new ArrayList<>();
            for (StringTokenizer st = new StringTokenizer(matchedUseElement.getName(), NamespaceDeclarationInfo.NAMESPACE_SEPARATOR); st.hasMoreTokens();) {
                String token = st.nextToken();
                if (st.hasMoreTokens()) {
                    segments.add(token);
                }
            }
            return Collections.unmodifiableList(segments);
        }

        private static boolean isFromCurrentNamespace(final QualifiedName fullyQualifiedName, final QualifiedName namespaceName) {
            return fullyQualifiedName.toString()
                    .substring(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length())
                    .startsWith(namespaceName.toString() + NamespaceDeclarationInfo.NAMESPACE_SEPARATOR);
        }

        public FullyQualifiedNameProcessor(final QualifiedTypeNameResolver qualifiedTypeNameResolver, final int offset) {
            this.qualifiedTypeNameResolver = qualifiedTypeNameResolver;
            this.offset = offset;
        }

        public QualifiedName process(final QualifiedName fullyQualifiedName, final NamespaceScope namespaceScope) {
            QualifiedName result = fullyQualifiedName;
            if (namespaceScope != null) {
                QualifiedName namespaceName = namespaceScope.getNamespaceName();
                if (isFromCurrentNamespace(fullyQualifiedName, namespaceName)) {
                    result = resolveFromCurrentNamespace(fullyQualifiedName, namespaceName);
                } else {
                    result = resolveFromAnotherNamespace(fullyQualifiedName, namespaceScope.getAllDeclaredSingleUses());
                }
            }
            return result;
        }

        private QualifiedName resolveFromCurrentNamespace(final QualifiedName fullyQualifiedName, final QualifiedName namespaceName) {
            int namespaceNameSegmentsSize = namespaceName.getSegments().size();
            int qualifiedNameSegmentsSize = fullyQualifiedName.getSegments().size();
            assert namespaceNameSegmentsSize < qualifiedNameSegmentsSize
                    : namespaceName.toString() + ":" + namespaceNameSegmentsSize + " < " + fullyQualifiedName.toString() + ":" + qualifiedNameSegmentsSize; //NOI18N
            String resultName = fullyQualifiedName.toString().substring(
                    NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length() + namespaceName.toString().length() + NamespaceDeclarationInfo.NAMESPACE_SEPARATOR.length());
            return QualifiedName.create(resultName);
        }

        private QualifiedName resolveFromAnotherNamespace(final QualifiedName fullyQualifiedName, final Collection<? extends UseScope> declaredUses) {
            UseScope matchedUseScope = getMatchedUseScopeForFullyQualifiedName(fullyQualifiedName, declaredUses);
            return qualifiedTypeNameResolver.resolveForUseScope(fullyQualifiedName, matchedUseScope);
        }

        private UseScope getMatchedUseScopeForFullyQualifiedName(final QualifiedName fullyQualifiedName, final Collection<? extends UseScope> declaredUses) {
            UseScope result = null;
            String firstSegmentName = fullyQualifiedName.getSegments().getFirst();
            int lastOffset = -1;
            for (UseScope useScope : declaredUses) {
                if (useScope.getOffset() < offset) {
                    AliasedName aliasName = useScope.getAliasedName();
                    if (aliasName != null) {
                        if (firstSegmentName.equals(aliasName.getAliasName())) {
                            result = useScope;
                            continue;
                        }
                    } else {
                        if (lastOffset < useScope.getOffset()) {
                            String useElementName = useScope.getName();
                            String modifiedUseElementName = useElementName.startsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR)
                                    ? useElementName
                                    : NamespaceDeclarationInfo.NAMESPACE_SEPARATOR + useElementName;
                            if (fullyQualifiedName.toString().startsWith(modifiedUseElementName)) {
                                lastOffset = useScope.getOffset();
                                result = useScope;
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

}