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
package org.netbeans.modules.php.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NamespaceIndexFilter;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * @author Radek Matous
 */
public final class ModelUtils {

    private static final Logger LOGGER = Logger.getLogger(ModelUtils.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ModelUtils.class);

    private ModelUtils() {
    }

    public static Set<AliasedName> getAliasedNames(final Model model, final int offset) {
        final Set<AliasedName> aliases = new HashSet<>();
        final NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), offset);
        if (namespaceScope != null) {
            Collection<? extends UseScope> declaredUses = namespaceScope.getAllDeclaredSingleUses();
            for (UseScope useElement : declaredUses) {
                AliasedName aliasedName = useElement.getAliasedName();
                if (aliasedName != null) {
                    aliases.add(aliasedName);
                }
            }
        }
        return aliases;
    }

    public static NamespaceScope getNamespaceScope(NamespaceDeclaration currenNamespace, FileScope fileScope) {
        NamespaceDeclarationInfo ndi = currenNamespace != null ? NamespaceDeclarationInfo.create(currenNamespace) : null;
        NamespaceScope currentScope = ndi != null
                ? ModelUtils.getFirst(ModelUtils.filter(fileScope.getDeclaredNamespaces(), ndi.getName()))
                : fileScope.getDefaultDeclaredNamespace();
        return currentScope;
    }

    public static Collection<? extends TypeScope> getDeclaredTypes(FileScope fileScope) {
        List<TypeScope> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredTypes());
        }
        return retval;
    }

    public static Collection<? extends ClassScope> getDeclaredClasses(FileScope fileScope) {
        List<ClassScope> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredClasses());
        }
        return retval;
    }

    public static Collection<? extends InterfaceScope> getDeclaredInterfaces(FileScope fileScope) {
        List<InterfaceScope> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredInterfaces());
        }
        return retval;
    }

    public static Collection<? extends TraitScope> getDeclaredTraits(FileScope fileScope) {
        List<TraitScope> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredTraits());
        }
        return retval;
    }

    public static Collection<? extends ConstantElement> getDeclaredConstants(FileScope fileScope) {
        List<ConstantElement> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredConstants());
        }
        return retval;
    }

    public static Collection<? extends FunctionScope> getDeclaredFunctions(FileScope fileScope) {
        List<FunctionScope> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredFunctions());
        }
        return retval;
    }

    public static Collection<? extends VariableName> getDeclaredVariables(FileScope fileScope) {
        List<VariableName> retval = new ArrayList<>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredVariables());
        }
        return retval;
    }

    public static List<? extends ModelElement> getElements(Scope scope, boolean resursively) {
        List<ModelElement> retval = new ArrayList<>();
        List<? extends ModelElement> elements = scope.getElements();
        retval.addAll(elements);
        for (ModelElement modelElement : elements) {
            if (modelElement instanceof Scope) {
                retval.addAll(getElements((Scope) modelElement, resursively));
            }
        }
        return retval;
    }

    public static Collection<? extends TypeScope> resolveType(Model model, StaticDispatch dispatch) {
        VariableScope variableScope = model.getVariableScope(dispatch.getStartOffset());
        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(
                ASTNodeInfo.toQualifiedName(dispatch, true),
                dispatch.getStartOffset(),
                variableScope);
        NamespaceIndexFilter filter = new NamespaceIndexFilter(fullyQualifiedName.toString());
        Collection<? extends TypeScope> staticTypeName = VariousUtils.getStaticTypeName(
                variableScope != null ? variableScope : model.getFileScope(), filter.getName());
        return filter.filterModelElements(staticTypeName, true);
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveType(Model model, VariableBase varBase) {
        return resolveType(model, varBase, true);
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveType(Model model, VariableBase varBase, boolean justDispatcher) {
        Collection<? extends TypeScope> retval = Collections.emptyList();
        VariableScope scp = model.getVariableScope(varBase.getStartOffset());
        if (scp != null) {
            String vartype = VariousUtils.extractTypeFroVariableBase(varBase);
            if (vartype != null) {
                retval = VariousUtils.getType(scp, vartype, varBase.getStartOffset(), justDispatcher);
            }
        }
        return retval;
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveType(Model model, Assignment varBase) {
        Collection<? extends TypeScope> retval = Collections.emptyList();
        VariableScope scp = model.getVariableScope(varBase.getStartOffset());
        if (scp != null) {
            String vartype = CodeUtils.extractVariableType(varBase);
            if (vartype == null) {
                final Expression rightHandSide = varBase.getRightHandSide();
                if (rightHandSide instanceof VariableBase) {
                    vartype = VariousUtils.extractTypeFroVariableBase((VariableBase) rightHandSide);
                    if (vartype != null) {
                        return VariousUtils.getType(scp, vartype, varBase.getStartOffset(), false);
                    }
                } else if (rightHandSide instanceof StaticDispatch) {
                    QualifiedName qName = ASTNodeInfo.toQualifiedName(rightHandSide, true);
                    if (qName != null) {
                        VariableScope variableScope = model.getVariableScope(rightHandSide.getStartOffset());
                        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(
                                qName,
                                rightHandSide.getStartOffset(),
                                variableScope);
                        NamespaceIndexFilter filter = new NamespaceIndexFilter(fullyQualifiedName.toString());
                        Collection<? extends TypeScope> staticTypeName = VariousUtils.getStaticTypeName(
                                variableScope != null ? variableScope : model.getFileScope(), filter.getName());
                        return filter.filterModelElements(staticTypeName, true);
                    }
                }
            } else {
                retval = VariousUtils.getType(scp, vartype, varBase.getStartOffset(), false);
            }
        }
        return retval;
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveType(Model model, int offset) {
        VariableScope variableScope = model.getVariableScope(offset);
        TypeScope typeScope = getTypeScope(variableScope);
        if (typeScope != null) {
            return Collections.singletonList(typeScope);
        }
        return Collections.emptyList();
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveTypeAfterReferenceToken(Model model, TokenSequence<PHPTokenId> tokenSequence,
            int offset, boolean specialVariable) {
        tokenSequence.move(offset);
        Collection<? extends TypeScope> retval = Collections.emptyList();
        VariableScope scp = model.getVariableScope(offset);
        if (specialVariable) {
            // #247082
            // typically 'self', '$this' etc.; it means that we need to find method scope since these
            // variables can be used in lambda functions directly
            Scope tmpScope = scp;
            while (tmpScope != null) {
                if (tmpScope instanceof MethodScope) {
                    scp = (VariableScope) tmpScope;
                    break;
                }
                tmpScope = tmpScope.getInScope();
            }
        }
        if (scp != null) {
                String semiType = VariousUtils.getSemiType(tokenSequence, VariousUtils.State.START, scp);
                if (semiType != null) {
                    return VariousUtils.getType(scp, semiType, offset, true);
                }

        }
        return retval;
    }

    @CheckForNull
    public static <T> T getFirst(Collection<? extends T> all) {
        if (all instanceof List) {
            return all.size() > 0 ? ((List<T>) all).get(0) : null;
        }
        return all.size() > 0 ? all.iterator().next() : null;
    }

    @CheckForNull
    public static <T extends ModelElement> T getLast(List<? extends T> all) {
        return all.size() > 0 ? all.get(all.size() - 1) : null;
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final QualifiedName qualifiedName) {
        final QualifiedNameKind kind = qualifiedName.getKind();
        final String name = qualifiedName.toName().toString();
        final String namespaceName = qualifiedName.toNamespaceName().toString();
        return filter(allElements, new ElementFilter<T>() {
            @Override
            public boolean isAccepted(T element) {
                if (nameKindMatch(element.getName(), nameKind, name)) {
                    switch(kind) {
                        case QUALIFIED:
                            return element.getNamespaceName().toString().endsWith(namespaceName);
                        case UNQUALIFIED:
                            return true;
                        case FULLYQUALIFIED:
                            return nameKindMatch(element.getNamespaceName().toString(), nameKind, namespaceName);
                        default:
                            assert false : kind;
                    }
                }
                return false;
            }
        });
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final QualifiedName qName) {
        return filter(allElements, QuerySupport.Kind.EXACT, qName);
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final String... elementName) {
        return filter(allElements, QuerySupport.Kind.EXACT, elementName);
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final String... elementName) {
        return filter(allElements, new ElementFilter<T>() {
            @Override
            public boolean isAccepted(T element) {
                final PhpElementKind kind = element.getPhpElementKind();
                boolean caseSensitive = EnumSet.of(PhpElementKind.VARIABLE, PhpElementKind.FIELD).contains(kind);
                return (elementName.length == 0 || nameKindMatch(!caseSensitive, element.getName(), nameKind, elementName));
            }
        });
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<? extends T> allElements,
            FileObject fileObject) {
        List<T> retval = new ArrayList<>();
        for (T element : allElements) {
            if (element.getFileObject() == fileObject) {
                retval.add(element);
            }
        }
        return retval;
    }
    @CheckForNull
    public static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final String... elementName) {
        return getFirst(filter(allElements, QuerySupport.Kind.EXACT, elementName));
    }

    @CheckForNull
    public static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final String... elementName) {
        return getFirst(filter(allElements, new ElementFilter<T>() {
            @Override
            public boolean isAccepted(T element) {
                return (elementName.length == 0 || nameKindMatch(element.getName(), nameKind, elementName));
            }
        }));
    }

    @CheckForNull
    public static <T extends ModelElement> T getFirst(Collection<? extends T> allElements,
            FileObject fileObject) {
        List<T> retval = new ArrayList<>();
        for (T element : allElements) {
            if (element.getFileObject() == fileObject) {
                retval.add(element);
            }
        }
        return getFirst(retval);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T extends ModelElement> Collection<? extends T> merge(Collection<? extends T>... all) {
        List<T> retval = new ArrayList<>();
        for (Collection<? extends T> list : all) {
            retval.addAll(list);
        }
        return retval;
    }

    @CheckForNull
    public static FileScope getFileScope(ModelElement element) {
        FileScope retval = (element instanceof FileScope) ? (FileScope) element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (FileScope) ((element instanceof FileScope) ? element : null);
        }
        return retval;
    }

    @CheckForNull
    public static NamespaceScope getNamespaceScope(ModelElement element) {
        NamespaceScope retval = (element instanceof NamespaceScope) ? (NamespaceScope) element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (NamespaceScope) ((element instanceof NamespaceScope) ? element : null);
        }
        return retval;
    }

    @CheckForNull
    public static NamespaceScope getNamespaceScope(FileScope fileScope, int offset) {
        NamespaceScope retval = fileScope.getDefaultDeclaredNamespace();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespaceScope : declaredNamespaces) {
            OffsetRange blockRange = namespaceScope.getBlockRange();
            if (blockRange != null && blockRange.containsInclusive(offset)) {
                if (retval == null || !namespaceScope.isDefaultNamespace()) {
                    retval = namespaceScope;
                }
            }
        }
        return retval;
    }

    @CheckForNull
    public static TypeScope getTypeScope(ModelElement element) {
        TypeScope retval = (element instanceof TypeScope) ? (TypeScope) element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (TypeScope) ((element instanceof TypeScope) ? element : null);
        }
        return retval;
    }
    @CheckForNull
    public static ClassScope getClassScope(ModelElement element) {
        ClassScope retval = (element instanceof ClassScope) ? (ClassScope) element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (ClassScope) ((element instanceof ClassScope) ? element : null);
        }
        return retval;
    }
    @NonNull
    public static IndexScope getIndexScope(ModelElement element) {
        IndexScope retval = (element instanceof IndexScope) ? (IndexScope) element : null;
        ModelElement tmpElement = element;
        while (retval == null && tmpElement != null) {
            tmpElement = tmpElement.getInScope();
            retval = (IndexScope) ((tmpElement instanceof IndexScope) ? tmpElement : null);
        }
        if (retval == null) {
            FileScope fileScope = getFileScope(element);
            assert fileScope != null;
            retval = fileScope.getIndexScope();
        }
        return retval;
    }

    public static <T extends ModelElement> List<? extends T> filter(final Collection<? extends T> instances, final ElementFilter<T> filter) {
        List<T> retval = new ArrayList<>();
        for (T baseElement : instances) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add(baseElement);
            }
        }
        return retval;
    }

    public interface ElementFilter<T extends ModelElement> {
        boolean isAccepted(T element);
    }

    public static boolean nameKindMatch(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(true, text, nameKind, queries);
    }

    private static boolean nameKindMatch(boolean forceCaseInsensitivity, String text, QuerySupport.Kind nameKind, String... queries) {
        boolean result = false;
        for (String query : queries) {
            switch (nameKind) {
                case CAMEL_CASE:
                    if (toCamelCase(text).startsWith(query)) {
                        result =  true;
                    }
                    break;
                case CASE_INSENSITIVE_PREFIX:
                    if (text.toLowerCase().startsWith(query.toLowerCase())) {
                        result =  true;
                    }
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    text = text.toLowerCase();
                    result = regexpMatch(text, query);
                    break;
                case REGEXP:
                    //TODO: might be perf. problem if called for large collections
                    // and ever and ever again would be compiled still the same query
                    result = regexpMatch(text, query);
                    break;
                case EXACT:
                    boolean retval = (forceCaseInsensitivity) ? text.equalsIgnoreCase(query) : text.equals(query);
                    if (retval) {
                        result =  true;
                    }
                    break;
                case PREFIX:
                    if (text.startsWith(query)) {
                        result =  true;
                    }
                    break;
                default:
                    //no-op
            }
        }
        return result;
    }

    private static boolean regexpMatch(String text, String query) {
        boolean result = false;
        Pattern p = Pattern.compile(query);
        if (nameKindMatch(p, text)) {
            result = true;
        }
        return result;
    }

    public static String getCamelCaseName(ModelElement element) {
        return toCamelCase(element.getName());
    }

    public static String toCamelCase(String plainName) {
        char[] retval = new char[plainName.length()];
        int retvalSize = 0;
        for (int i = 0; i < retval.length; i++) {
            char c = plainName.charAt(i);
            if (Character.isUpperCase(c)) {
                retval[retvalSize] = c;
                retvalSize++;
            }
        }
        return String.valueOf(String.valueOf(retval, 0, retvalSize));
    }

    private static boolean nameKindMatch(Pattern p, String text) {
        return p.matcher(text).matches();
    }

    @CheckForNull
    public static FileScope getFileScope(final FileObject fileObject) {
        return getFileScope(fileObject, 0);
    }

    /**
     *
     * @param fileObject
     * @param timeout in milliseconds
     * @return
     */
    @CheckForNull
    public static FileScope getFileScope(final FileObject fileObject, final int timeout) {
        FileScope result = null;
        final Future<FileScope> futureResult = RP.submit(new Callable<FileScope>() {

            @Override
            public FileScope call() throws Exception {
                final FileScope[] fileScope = new FileScope[1];
                try {
                    ParserManager.parse(Collections.singletonList(Source.create(fileObject)), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result parserResult = resultIterator.getParserResult();
                            if (parserResult instanceof PHPParseResult) {
                                PHPParseResult phpResult = (PHPParseResult) parserResult;
                                fileScope[0] = phpResult.getModel().getFileScope();
                            }
                        }
                    });
                } catch (ParseException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
                return fileScope[0];
            }
        });
        try {
            result = futureResult.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return result;
    }

    @CheckForNull
    public static Model getModel(final Source source) {
        return getModel(source, 0);
    }

    /**
     *
     * @param source
     * @param timeout in milliseconds
     * @return
     */
    @CheckForNull
    public static Model getModel(final Source source, final int timeout) {
        Model result = null;
        final Future<Model> futureResult = RP.submit(new Callable<Model>() {

            @Override
            public Model call() throws Exception {
                final Model[] model = new Model[1];
                try {
                    ParserManager.parse(Collections.singletonList(source), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result parserResult = resultIterator.getParserResult();
                            if (parserResult instanceof PHPParseResult) {
                                PHPParseResult phpResult = (PHPParseResult) parserResult;
                                model[0] = phpResult.getModel();
                            }
                        }
                    });
                } catch (ParseException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
                return model[0];
            }
        });
        try {
            result = futureResult.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return result;
    }

    /**
     * Check whether the scope is anonymous function scope.
     *
     * @param scope the scope
     * @return {@code true} if the scope is anonymous function scope,
     * {@code false} otherwise
     */
    public static boolean isAnonymousFunction(Scope scope) {
        return scope instanceof FunctionScope
                && ((FunctionScope) scope).isAnonymous();
    }
}
