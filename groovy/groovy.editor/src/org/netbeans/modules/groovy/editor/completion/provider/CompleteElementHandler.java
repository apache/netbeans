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

package org.netbeans.modules.groovy.editor.completion.provider;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class CompleteElementHandler {

    private final CompletionContext context;
    private final ParserResult info;
    private final GroovyIndex index;

    
    public CompleteElementHandler(CompletionContext context) {
        this.context = context;
        this.info = context.getParserResult();

        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (fo != null) {
            // FIXME index is broken when invoked on start
            this.index = GroovyIndex.get(QuerySupport.findRoots(fo, Collections.singleton(ClassPath.SOURCE), null, null));
        } else {
            this.index = null;
        }
    }

    public Map<MethodSignature, CompletionItem> getMethods() {
        final ClassNode source = context.getSurroundingClass();
        final ClassNode node = context.declaringClass;
        
        if (node == null) {
            return Collections.emptyMap();
        }
        
        Map<MethodSignature, CompletionItem> result = getMethodsInner(
                source, 
                node,
                context.getPrefix(), 
                context.getAnchor(),
                0,
                AccessLevel.create(source, node),
                context.dotContext != null && context.dotContext.isMethodsOnly());

        return result;
    }

    public Map<FieldSignature, CompletionItem> getFields() {
        final ClassNode source = context.getSurroundingClass();
        final ClassNode node = context.declaringClass;
        
        if (node == null) {
            return Collections.emptyMap();
        }
        
        Map<FieldSignature, CompletionItem> result = getFieldsInner(
                source, 
                node,
                context.getPrefix(), 
                context.getAnchor(),
                0);

        return result;
    }

    // FIXME configure acess levels
    private Map<MethodSignature, CompletionItem> getMethodsInner(
            ClassNode source,
            ClassNode node,
            String prefix,
            int anchor,
            int level,
            Set<AccessLevel> access,
            boolean nameOnly) {

        boolean leaf = (level == 0);
        Set<AccessLevel> modifiedAccess = AccessLevel.update(access, source, node);

        Map<MethodSignature, CompletionItem> result = new TreeMap<>(new Comparator<MethodSignature>() {

            @Override
            public int compare(MethodSignature method1, MethodSignature method2) {
                // Different method name --> just compare as normal Strings
                if (!method1.getName().equals(method2.getName())) {
                    return method1.getName().compareTo(method2.getName());
                }
                // Method with lower 'parameter count' should be always first
                if (method1.getParameters().length < method2.getParameters().length) {
                    return -1;
                }
                if (method1.getParameters().length > method2.getParameters().length) {
                    return 1;
                }
                // Same number of parameters --> compare param by param as normal Strings
                for (int i = 0; i < method1.getParameters().length; i++) {
                    String param1 = method1.getParameters()[i];
                    String param2 = method2.getParameters()[i];
                    
                    int comparedValue = param1.compareTo(param2);
                    if (comparedValue != 0) {
                        return comparedValue;
                    }
                }
                // This should happened only if there are two absolutely identical methods
                return 0;
            }
        });
        ClassDefinition definition = loadDefinition(node);
        
        ClassNode typeNode = definition.getNode();
        String typeName = typeNode.getName();
        
        // In cases like 1.^ we have current type name "int" but indexer won't find anything for such a primitive
        if ("int".equals(typeName)) { // NOI18N
            typeName = "java.lang.Integer"; // NOI18N
        }
        context.setTypeName(typeName);

        GroovyElementsProvider groovyProvider = new GroovyElementsProvider();
        fillSuggestions(groovyProvider.getMethods(context), result);
        
        // we can't go groovy and java - helper methods would be visible
        if (result.isEmpty()) {
            ClassNode redirected = typeNode.redirect();

            String[] typeParameters = new String[(redirected.isUsingGenerics() && redirected.getGenericsTypes() != null)
                    ? redirected.getGenericsTypes().length : 0];
            GenericsType[] origTypes = typeNode.getGenericsTypes();
            for (int i = 0; i < typeParameters.length; i++) {
                GenericsType genType = redirected.getGenericsTypes()[i];
                
                // a generic type with some explicit type parameters may redirect to a real type that specifies placeholders
                // in place of the explicit type parameter values. So continue to take placeholders from the real type,
                // byt replace actual type parameters.
                if (origTypes != null && i < origTypes.length) {
                    if (genType.getType() != null && genType.isPlaceholder() &&
                        !origTypes[i].isPlaceholder()) {
                        genType = origTypes[i];
                    }
                }
                if (genType.getUpperBounds() != null) {
                    typeParameters[i] = Utilities.translateClassLoaderTypeName(genType.getUpperBounds()[0].getName());
                } else {
                    typeParameters[i] = Utilities.translateClassLoaderTypeName(genType.getName());
                }
            }

            fillSuggestions(JavaElementHandler.forCompilationInfo(info)
                    .getMethods(typeName, prefix, anchor, typeParameters,
                            leaf, modifiedAccess, nameOnly), result);
        }

        CompletionProviderHandler providerHandler = new CompletionProviderHandler();
        fillSuggestions(providerHandler.getMethods(context), result);
        fillSuggestions(providerHandler.getStaticMethods(context), result);

        if (typeNode.getSuperClass() != null) {
            fillSuggestions(getMethodsInner(source, typeNode.getSuperClass(), prefix, anchor, level + 1, modifiedAccess, nameOnly), result);
        } else if (leaf) {
            fillSuggestions(JavaElementHandler.forCompilationInfo(info).getMethods("java.lang.Object", prefix, anchor, new String[]{}, false, modifiedAccess, nameOnly), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces()) {
            fillSuggestions(getMethodsInner(source, inter, prefix, anchor, level + 1, modifiedAccess, nameOnly), result);
        }
        
        fillSuggestions(TransformationHandler.getMethods(index, typeNode, prefix, anchor), result);
        
        return result;
    }

    private Map<FieldSignature, CompletionItem> getFieldsInner(ClassNode source, ClassNode node, String prefix, int anchor, int level) {
        boolean leaf = (level == 0);

        /* Move this whole block away, context information should be in CompletionContext */
        ClassDefinition definition = loadDefinition(node);
        ClassNode typeNode = definition.getNode();
        String typeName = typeNode.getName();
        // In cases like 1.^ we have current type name "int" but indexer won't find anything for such a primitive
        if ("int".equals(typeName)) { // NOI18N
            typeName = "java.lang.Integer"; // NOI18N
        }
        context.setTypeName(typeName);
        /**/
        
        Map<FieldSignature, CompletionItem> result = new HashMap<>();

        GroovyElementsProvider groovyProvider = new GroovyElementsProvider();
        fillSuggestions(groovyProvider.getFields(context), result);
        fillSuggestions(groovyProvider.getStaticFields(context), result);

        fillSuggestions(JavaElementHandler.forCompilationInfo(info).getFields(typeNode.getName(), prefix, anchor, leaf), result);

        CompletionProviderHandler providerHandler = new CompletionProviderHandler();
        fillSuggestions(providerHandler.getFields(context), result);
        fillSuggestions(providerHandler.getStaticFields(context), result);

        if (typeNode.getSuperClass() != null) {
            fillSuggestions(getFieldsInner(source, typeNode.getSuperClass(), prefix, anchor, level + 1), result);
        } else if (leaf) {
            fillSuggestions(JavaElementHandler.forCompilationInfo(info).getFields("java.lang.Object", prefix, anchor, false), result); // NOI18N
        }

        for (ClassNode inter : typeNode.getInterfaces()) {
            fillSuggestions(getFieldsInner(source, inter, prefix, anchor, level + 1), result);
        }
        
        fillSuggestions(TransformationHandler.getFields(index, typeNode, prefix, anchor), result);

        return result;
    }

    private ClassDefinition loadDefinition(ClassNode node) {
        if (index == null) {
            return new ClassDefinition(node, null);
        }

        Set<IndexedClass> classes = index.getClasses(node.getName(), QuerySupport.Kind.EXACT);

        if (!classes.isEmpty()) {
            IndexedClass indexed = classes.iterator().next();
            ASTNode astNode = ASTUtils.getForeignNode(indexed);
            if (astNode instanceof ClassNode) {
                return new ClassDefinition((ClassNode) astNode, indexed);
            }
        }

        return new ClassDefinition(node, null);
    }

    private static <T> void fillSuggestions(Map<T, ? extends CompletionItem> input, Map<T, ? super CompletionItem> result) {
        for (Map.Entry<T, ? extends CompletionItem> entry : input.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static class ClassDefinition {

        private final ClassNode node;

        private final IndexedClass indexed;

        public ClassDefinition(ClassNode node, IndexedClass indexed) {
            this.node = node;
            this.indexed = indexed;
        }

        public ClassNode getNode() {
            return node;
        }

        public FileObject getFileObject() {
            return indexed != null ? indexed.getFileObject() : null;
        }
    }
}
