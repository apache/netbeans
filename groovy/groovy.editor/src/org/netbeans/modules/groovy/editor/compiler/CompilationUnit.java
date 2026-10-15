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

package org.netbeans.modules.groovy.editor.compiler;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.control.ClassNodeResolver.LookupResult;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParser;
import org.netbeans.modules.groovy.editor.java.ElementSearch;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class CompilationUnit extends org.codehaus.groovy.control.CompilationUnit {
    protected final Snapshot mainSnapshot;
    
    static CompilerConfiguration processConfiguration(CompilerConfiguration configuration, boolean isIndexing) {
        Map<String, Boolean> opts = configuration.getOptimizationOptions();
        opts.put("classLoaderResolving", Boolean.FALSE); // NOI18N
        return configuration;
    }
    
    public CompilationUnit(GroovyParser parser, CompilerConfiguration configuration,
            CodeSource security,
            @NonNull final GroovyClassLoader loader,
            @NonNull final GroovyClassLoader transformationLoader,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final ClassNodeCache classNodeCache) {
        this(parser, configuration, security, loader, transformationLoader, cpInfo, classNodeCache, true, null);
    }
    
    public CompilationUnit(GroovyParser parser, CompilerConfiguration configuration,
            CodeSource security,
            @NonNull final GroovyClassLoader loader,
            @NonNull final GroovyClassLoader transformationLoader,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final ClassNodeCache classNodeCache, boolean isIndexing, Snapshot snapshot) {
    
        super(processConfiguration(configuration, isIndexing), 
                security, loader, transformationLoader);
        this.mainSnapshot = snapshot;
        Map<String, Boolean> opts = this.configuration.getOptimizationOptions();
        opts.put("classLoaderResolving", Boolean.FALSE);
        this.configuration.setOptimizationOptions(opts);
        this.ast = new CompileUnit(parser, this.classLoader, 
                (n) -> {
                    LookupResult lr = getClassNodeResolver().resolveName(n, this);
                    if (lr != null && lr.isClassNode()) {
                        return lr.getClassNode();
                    } else {
                        return null;
                    }
                },
                security, this.configuration, cpInfo, classNodeCache);
    }
    
    private static class CompileUnit extends org.codehaus.groovy.ast.CompileUnit {
        private final Function<String, ClassNode> classResolver;
        private final ClassNodeCache cache;
        private final GroovyParser parser;
        private final JavaSource javaSource;
        private final HashMap<String, ClassNode> temp = new HashMap<>();
        
        public CompileUnit(GroovyParser parser, GroovyClassLoader classLoader,
                Function<String, ClassNode> classResolver,
                CodeSource codeSource, CompilerConfiguration config,
                ClasspathInfo cpInfo,
                ClassNodeCache classNodeCache) {
            super(classLoader, codeSource, config);
            this.parser = parser;
            this.cache = classNodeCache;
            this.javaSource = cache.createResolver(cpInfo);
            this.classResolver = classResolver;
        }


        @Override
        public ClassNode getClass(final String name) {
            if (parser.isCancelled()) {
                throw new CancellationException();
            }
            
            ClassNode classNode = cache.get(name);
            if (classNode != null) {
                return classNode;
            }

            classNode = temp.get(name);
            if (classNode != null) {
                return classNode;
            }

            classNode = super.getClass(name);
            if (classNode != null) {
                return classNode;
            }

            classNode = classResolver.apply(name);
            if (classNode != null) {
                cache.put(name, classNode);
                return classNode;
            }
            
            if (cache.isNonExistent(name)) {
                return null;
            }
            
            // The following code is legacy and ClassNodes it creates are not fully populated with properties, fields and methods.
            // they may be fine for type resolution, but definitely unsuitable for attribution of the AST, as they cannot resolve referenced
            // members. Barely useful as proxies that are redirect()ed.
            
            try {
                // if it is a groovy file it is useless to load it with java
                // at least until VirtualSourceProvider will do te job ;)
//                if (getClassLoader().getResourceLoader().loadGroovySource(name) != null) {
//                    return null;
//                }
                final ClassNode[] holder = new ClassNode[1];
                Task<CompilationController> task = new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Elements elements = controller.getElements();
                        TypeElement typeElement = ElementSearch.getClass(elements, name);
                        if (typeElement != null) {
                            try {
                                final ClassNode node = createClassNode(name, 0, null, ClassNode.EMPTY_ARRAY, null);
                                temp.put(name, node);
                                initClassNode(node, typeElement);
                                if (node != null) {
                                    cache.put(name, node);
                                }
                                //else type exists but groovy support cannot create it from javac
                                //delegate to slow class loading, workaround of fix of issue # 206811
                                holder[0] = node;
                            } finally {
                                temp.remove(name);
                            }
                        } else {
                            cache.put(name, null);
                        }
                    }
                };
                javaSource.runUserActionTask(task, true);
                return holder[0];
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private void initClassNode(ClassNode node, TypeElement typeElement) {
            ElementKind kind = typeElement.getKind();
            if (kind == ElementKind.ANNOTATION_TYPE) {
                initAnnotationType(node, typeElement);
            } else if (kind == ElementKind.INTERFACE) {
                initInterfaceKind(node, typeElement);
            } else {
                initClassType(node, typeElement);
            }
        }

        private void initAnnotationType(ClassNode node, TypeElement typeElement) {
            node.setModifiers(Opcodes.ACC_ANNOTATION);
            node.setSuperClass(ClassHelper.Annotation_TYPE);
            initTypeInterfaces(node, typeElement);
        }
        
        private void initTypeInterfaces(ClassNode node, TypeElement typeElement) {
            Set<ClassNode> interfaces = new HashSet<ClassNode>();
            Set<GenericsType> generics = new HashSet<>();

            for (TypeParameterElement typeParameter : typeElement.getTypeParameters()) {
                    List<? extends TypeMirror> bounds = typeParameter.getBounds();
                    for (TypeMirror bound : bounds) {
                        ClassNode typeParam = getClass(bound.toString());
                        generics.add(new GenericsType(typeParam));
                    }
            }
            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                interfaces.add(new ClassNode(Utilities.getClassName(interfaceType).toString(), Opcodes.ACC_INTERFACE, null));
            }
            node.setInterfaces(interfaces.toArray(new ClassNode[0]));
            node.setGenericsTypes(generics.toArray(new GenericsType[0]));
        }

        private void initInterfaceKind(ClassNode node, TypeElement typeElement) {
            int modifiers = 0;
            modifiers |= Opcodes.ACC_INTERFACE;
            node.setModifiers(modifiers);
            initTypeInterfaces(node, typeElement);
        }

        private void initClassType(ClassNode node, TypeElement typeElement) {
            // initialize supertypes
            // super class is required for try {} catch block exception type
            Stack<DeclaredType> supers = new Stack<DeclaredType>();
            Set<GenericsType> generics = new HashSet<>();
            while (typeElement != null && typeElement.asType().getKind() != TypeKind.NONE) {

                for (TypeParameterElement typeParameter : typeElement.getTypeParameters()) {
                    List<? extends TypeMirror> bounds = typeParameter.getBounds();
                    
                    BOUNDS_LOOP: for (TypeMirror bound : bounds) {
                        ClassNode typeParam = getClass(bound.toString());

                        for (GenericsType generic : generics) {
                            if (generic.getType().equals(typeParam)) {
                                continue BOUNDS_LOOP;
                            }
                        }
                        generics.add(new GenericsType(typeParam));
                    }
                }

                TypeMirror type = typeElement.getSuperclass();
                if (type.getKind() != TypeKind.DECLARED) {
                    break;
                }

                DeclaredType superType = (DeclaredType) typeElement.getSuperclass();
                supers.push(superType);

                Element element = superType.asElement();
                if ((element.getKind() == ElementKind.CLASS
                        || element.getKind() == ElementKind.ENUM) && (element instanceof TypeElement)) {

                    typeElement = (TypeElement) element;
                    continue;
                }

                typeElement = null;
            }

            ClassNode superClass = null;
            while (!supers.empty()) {
                superClass = createClassNode(Utilities.getClassName(supers.pop()).toString(), 0, superClass, new ClassNode[0], generics);
            }

            node.setSuperClass(superClass);
            node.setGenericsTypes(generics.toArray(new GenericsType[0]));
        }

        private ClassNode createClassNode(String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, Set<GenericsType> generics) {
            ClassNode classNode = new ClassNode(name, modifiers, superClass, interfaces, MixinNode.EMPTY_ARRAY);
            if (generics != null) {
                classNode.setGenericsTypes(generics.toArray(new GenericsType[0]));
            }
            return classNode;
        }
    }
    
    protected void runSourceVisitor(String visitorName, Consumer<SourceUnit> callback) {
        for (SourceUnit su : sources.values()) {
            callback.accept(su);
        }
    }
    
    private static Method getCachedClassPathMethod;
    
    public static ClassPath pryOutCachedClassPath(ClasspathInfo cpInfo, ClasspathInfo.PathKind kind) {
        try {
            if (getCachedClassPathMethod == null) {
                Method m = ClasspathInfo.class.getDeclaredMethod("getCachedClassPath", ClasspathInfo.PathKind.class);
                m.setAccessible(true);
                getCachedClassPathMethod = m;
            } else if (getCachedClassPathMethod.getDeclaringClass() == String.class) {
                return cpInfo.getClassPath(kind);
            }
            return (ClassPath)getCachedClassPathMethod.invoke(cpInfo, kind);
        } catch (ReflectiveOperationException | SecurityException ex) {
            Exceptions.printStackTrace(ex);
            try {
                getCachedClassPathMethod = String.class.getMethod("toString");
            } catch (ReflectiveOperationException | SecurityException ex2) {
                Exceptions.printStackTrace(ex2);
            }
            return cpInfo.getClassPath(kind);
        }
    }
}
