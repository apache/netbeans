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

package org.netbeans.modules.groovy.editor.completion;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.completion.util.CamelCaseUtil;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.completion.provider.CompletionAccessor;
import org.netbeans.modules.groovy.editor.imports.ImportUtils;
import org.netbeans.modules.groovy.editor.java.JavaElementHandle;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 * Complete the Groovy and Java types available at this position.
 * 
 * This could be either:
 * 1.) Completing all available Types in a given package. This is used for:
 * 1.1) import statements completion
 * 1.2) If you simply want to give the fq-name for something.
 *
 * 2.) Complete the types which are available without having to give a fqn:
 * 2.1.) Types defined in the Groovy File where the completion is invoked. (INDEX)
 * 2.2.) Types located in the same package (source or binary). (INDEX)
 * 2.3.) Types manually imported via the "import" statement. (AST)
 * 2.4.) The Default imports for Groovy, which are a super-set of Java. (NB JavaSource)
 *
 * These are the Groovy default imports:
 *
 * java.io.*
 * java.lang.*
 * java.math.BigDecimal
 * java.math.BigInteger
 * java.net.*
 * java.util.*
 * groovy.lang.*
 * groovy.util.*
 *
 * @author Martin Janicek
 */
public class TypesCompletion extends BaseCompletion {

    // There attributes should be initiated for each complete() method call
    private Map<Object, CompletionProposal> proposals;
    private CompletionContext request;
    private int anchor;
    private boolean constructorCompletion;

    
    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        LOG.log(Level.FINEST, "-> completeTypes"); // NOI18N

        this.proposals = proposals;
        this.request = request;
        this.anchor = anchor;

        if (request.dotContext != null) {
            if (request.dotContext.isFieldsOnly() || request.dotContext.isMethodsOnly()) {
                return false;
            }
        }
        
        final PackageCompletionRequest packageRequest = getPackageRequest(request);

        // todo: we don't handle single dots in the source. In that case we should
        // find the class we are living in. Disable it for now.

        if (packageRequest.basePackage.length() == 0
                && packageRequest.prefix.length() == 0
                && packageRequest.fullString.equals(".")) {
            return false;
        }

        // check for a constructor call
        if (ContextHelper.isConstructorCall(request)) {
            constructorCompletion = true;
        } else {
            constructorCompletion = false;
        }

        // are we dealing with a class xyz implements | {
        // kind of completion?

        boolean onlyInterfaces = false;

        Token<? extends GroovyTokenId> literal = request.context.beforeLiteral;
        if (literal != null) {
            
            // We don't need to complete Types after class definition
            if (literal.id() == GroovyTokenId.LITERAL_class) {
                return false;
            }

            if (literal.id() == GroovyTokenId.LITERAL_implements) {
                LOG.log(Level.FINEST, "Completing only interfaces after implements keyword.");
                onlyInterfaces = true;
            }
        }


        Set<TypeHolder> addedTypes = new HashSet<>();

        // This ModuleNode is used to retrieve the types defined here and the package name.
        ModuleNode moduleNode = ContextHelper.getSurroundingModuleNode(request);
        String currentPackage = getCurrentPackageName(moduleNode);
        JavaSource javaSource = getJavaSourceFromRequest();

        GroovyIndex index = null;
        FileObject fo = request.getSourceFile();
        if (fo != null) {
            index = GroovyIndex.get(QuerySupport.findRoots(fo,
                    Collections.singleton(ClassPath.SOURCE),
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList()));
        }

        // if we are dealing with a basepackage we simply complete all the packages given in the basePackage
        if (packageRequest.basePackage.length() > 0 || request.isBehindImportStatement()) {
            List<TypeHolder> typeList = getTypeHoldersForPackage(javaSource, packageRequest.basePackage, currentPackage);

            LOG.log(Level.FINEST, "Number of types found:  {0}", typeList.size());

            for (TypeHolder singleType : typeList) {
                addToProposalUsingFilter(addedTypes, singleType, onlyInterfaces);
            }

            if (index != null) {
                Set<IndexedClass> classes = index.getClassesFromPackage(packageRequest.basePackage);
                for (IndexedClass indexedClass : classes) {
                    addToProposalUsingFilter(addedTypes, new TypeHolder(indexedClass), onlyInterfaces);
                }
            }

            return true;
        }

        // dont want types for objectExpression.something
        if (request.isBehindDot()) {
            return false;
        }

        // Retrieve the package we are living in from AST and then
        // all classes from that package using the Groovy Index.

        if (moduleNode != null) {
            LOG.log(Level.FINEST, "We are living in package : {0} ", currentPackage);

            if (index != null) {
                String camelCaseFirstWord = CamelCaseUtil.getCamelCaseFirstWord(request.getPrefix());
                Set<IndexedClass> classes = index.getClasses(camelCaseFirstWord, QuerySupport.Kind.PREFIX);

                if (!classes.isEmpty()) {
                    for (IndexedClass indexedClass : classes) {
                        addToProposalUsingFilter(addedTypes, new TypeHolder(indexedClass), onlyInterfaces);
                    }
                }
            }
        }

        List<String> localDefaultImports = new ArrayList<>();

        // Are there any manually imported types?

        if (moduleNode != null) {

            // this gets the list of full-qualified names of imports.
            List<ImportNode> imports = moduleNode.getImports();

            if (imports != null) {
                for (ImportNode importNode : imports) {
                    ElementKind ek;
                    if (importNode.getType().isInterface()) {
                        ek = ElementKind.INTERFACE;
                    } else {
                        ek = ElementKind.CLASS;
                    }

                    addToProposalUsingFilter(addedTypes, new TypeHolder(importNode.getClassName(), ek), onlyInterfaces);
                }
            }

            // this returns a list of String's of wildcard-like included types.
            List<ImportNode> importNodes = moduleNode.getStarImports();

            for (ImportNode wildcardImport : importNodes) {
                String packageName = wildcardImport.getPackageName();
                if (packageName.endsWith(".")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                localDefaultImports.add(packageName);
            }
        }


        // Now we compute the type-proposals for the default imports.
        // First, create a list of default JDK packages. These are reused,
        // so they are defined elsewhere.

        localDefaultImports.addAll(ImportUtils.getDefaultImportPackages());

        // adding types from default import, optionally filtered by
        // prefix

        for (String singlePackage : localDefaultImports) {
            List<TypeHolder> typeList = getTypeHoldersForPackage(javaSource, singlePackage, currentPackage);

            LOG.log(Level.FINEST, "Number of types found:  {0}", typeList.size());

            for (TypeHolder element : typeList) {
                addToProposalUsingFilter(addedTypes, element, onlyInterfaces);
            }
        }

        // Adding single classes
        for (String className : ImportUtils.getDefaultImportClasses()) {
            addToProposalUsingFilter(addedTypes, new TypeHolder(className, ElementKind.CLASS), onlyInterfaces);
        }

        // Adding declared classes
        for (ClassNode declaredClass : ContextHelper.getDeclaredClasses(request)) {
            addToProposalUsingFilter(addedTypes, new TypeHolder(declaredClass.getName(), ElementKind.CLASS), onlyInterfaces);
        }

        return true;
    }

    private String getCurrentPackageName(ModuleNode moduleNode) {
        if (moduleNode != null) {
            return moduleNode.getPackageName();
        } else {
            ClassNode node = ContextHelper.getSurroundingClassNode(request);
            if (node != null) {
                return node.getPackageName();
            }
        }
        return "";
    }

    private JavaSource getJavaSourceFromRequest() {
        ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);
        assert pathInfo != null;

        JavaSource javaSource = JavaSource.create(pathInfo);
        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }
        return javaSource;
    }

    /**
     * Adds the type given in fqn with its simple name to the proposals, filtered by
     * the prefix and the package name.
     * 
     * @param alreadyPresent already presented proposals
     * @param type type we want to add into proposals
     * @param onlyInterfaces true, if we are dealing with only interfaces completion
     */
    private void addToProposalUsingFilter(Set<TypeHolder> alreadyPresent, TypeHolder type, boolean onlyInterfaces) {
        if ((onlyInterfaces && (type.getKind() != ElementKind.INTERFACE)) || alreadyPresent.contains(type)) {
            return;
        }
        
        String fqnTypeName = type.getName();
        String typeName = GroovyUtils.stripPackage(fqnTypeName);

        // If we are in situation: "String s = new String|" we don't want to show
        // String type as a option - we want to show String constructors + types
        // prefixed with String (e.g. StringBuffer)
        if (constructorCompletion && typeName.equalsIgnoreCase(request.getPrefix())) {
            return;
        }
        
        if (type.getHandle() != null
                &&!(type.getHandle().getKind().isClass() || type.getHandle().getKind().isInterface())
                && request.location != CaretLocation.INSIDE_IMPORT) {
            return;
        }

        String ownerFQN = GroovyUtils.getPackageName(fqnTypeName);
        
        // We are dealing with prefix for some class type
        JavaElementHandle jh = null;
        if (type.getHandle() != null) {
            jh = new JavaElementHandle(typeName, ownerFQN, type.getHandle(), Collections.emptyList(), Collections.emptySet());
        }
        
        if (isPrefixed(request, typeName)) {
            alreadyPresent.add(type);
            proposals.putIfAbsent(fqnTypeName, CompletionAccessor.instance().createType(jh, fqnTypeName, typeName, anchor, type.getKind()));
        }

        // We are dealing with CamelCase completion for some class type
        if (CamelCaseUtil.compareCamelCase(typeName, request.getPrefix())) {
            CompletionItem.TypeItem camelCaseProposal = CompletionAccessor.instance().createType(jh, fqnTypeName, typeName, anchor, ElementKind.CLASS);
            proposals.putIfAbsent(fqnTypeName, camelCaseProposal);
        }
    }

    @NonNull
    private List<TypeHolder> getTypeHoldersForPackage(final JavaSource javaSource, final String pkg, final String currentPackage) {
        LOG.log(Level.FINEST, "getElementListForPackageAsString(), Package :  {0}", pkg);

        final List<TypeHolder> result = new ArrayList<>();

        if (javaSource != null) {

            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController info) throws IOException {
                        Elements elements = info.getElements();
                        info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        addPackageElements(elements.getPackageElement(pkg));
                        addTypeElements(elements.getTypeElement(pkg));
                    }

                    private void addPackageElements(PackageElement packageElement) {
                        if (packageElement != null) {
                            List<? extends Element> typelist = packageElement.getEnclosedElements();
                            boolean samePackage = pkg.equals(currentPackage);

                            for (Element element : typelist) {
                                Set<Modifier> modifiers = element.getModifiers();
                                if (modifiers.contains(Modifier.PUBLIC)
                                    || samePackage && (modifiers.contains(Modifier.PROTECTED)
                                    || (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PRIVATE)))) {
                                    
                                    result.add(new TypeHolder(element.toString(), org.netbeans.api.java.source.ElementHandle.create(element)));
                                }
                            }
                        }
                    }

                    private void addTypeElements(TypeElement typeElement) {
                        if (typeElement != null) {
                            List<? extends Element> typelist = typeElement.getEnclosedElements();
                            boolean samePackage = pkg.equals(currentPackage);

                            for (Element element : typelist) {
                                Set<Modifier> modifiers = element.getModifiers();
                                if (modifiers.contains(Modifier.PUBLIC)
                                    || samePackage && (modifiers.contains(Modifier.PROTECTED)
                                    || (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PRIVATE)))) {
                                    
                                        result.add(new TypeHolder(pkg + "." + element.getSimpleName().toString(), org.netbeans.api.java.source.ElementHandle.create(element)));
                                }
                            }
                        }

                    }
                }, true);
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "IOException : {0}", ex.getMessage());
            }
        }
        return result;
    }

    private static class TypeHolder {

        private final String name;
        private final ElementKind kind;
        private final org.netbeans.api.java.source.ElementHandle handle;

        public TypeHolder(IndexedClass indexedClass) {
            this.name = indexedClass.getFqn();

            if (indexedClass.getKind() == org.netbeans.modules.csl.api.ElementKind.CLASS) {
                this.kind = ElementKind.CLASS;
            } else {
                this.kind = ElementKind.INTERFACE;
            }
            this.handle = null;
        }
        
        public TypeHolder(String name, ElementKind kind) {
            this.name = name;
            this.kind = kind;
            this.handle = null;
        }
        
        public TypeHolder(String name, org.netbeans.api.java.source.ElementHandle handle) {
            this.name = name;
            this.handle = handle;
            this.kind = handle.getKind();
        }

        public org.netbeans.api.java.source.ElementHandle getHandle() {
            return handle;
        }

        public ElementKind getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeHolder other = (TypeHolder) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }
    }
}
