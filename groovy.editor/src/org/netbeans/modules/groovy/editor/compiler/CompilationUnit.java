/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.groovy.editor.compiler;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.CancellationException;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParser;
import org.netbeans.modules.groovy.editor.java.ElementSearch;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class CompilationUnit extends org.codehaus.groovy.control.CompilationUnit {

    public CompilationUnit(GroovyParser parser, CompilerConfiguration configuration,
            CodeSource security,
            @NonNull final GroovyClassLoader loader,
            @NonNull final GroovyClassLoader transformationLoader,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final ClassNodeCache classNodeCache) {

        super(configuration, security, loader, transformationLoader);
        this.ast = new CompileUnit(parser, this.classLoader, security, this.configuration, cpInfo, classNodeCache);
    }

    private static class CompileUnit extends org.codehaus.groovy.ast.CompileUnit {

        private final ClassNodeCache cache;
        private final GroovyParser parser;
        private final JavaSource javaSource;


        public CompileUnit(GroovyParser parser, GroovyClassLoader classLoader,
                CodeSource codeSource, CompilerConfiguration config,
                ClasspathInfo cpInfo,
                ClassNodeCache classNodeCache) {
            super(classLoader, codeSource, config);
            this.parser = parser;
            this.cache = classNodeCache;
            this.javaSource = cache.createResolver(cpInfo);
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

            classNode = super.getClass(name);
            if (classNode != null) {
                return classNode;
            }

            if (cache.isNonExistent(name)) {
                return null;
            }

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
                        Elements elements = controller.getElements();
                        TypeElement typeElement = ElementSearch.getClass(elements, name);
                        if (typeElement != null) {
                            final ClassNode node = createClassNode(name, typeElement);
                            if (node != null) {
                                cache.put(name, node);
                            }
                            //else type exists but groovy support cannot create it from javac
                            //delegate to slow class loading, workaround of fix of issue # 206811
                            holder[0] = node;
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

        private ClassNode createClassNode(String name, TypeElement typeElement) {
            ElementKind kind = typeElement.getKind();
            if (kind == ElementKind.ANNOTATION_TYPE) {
                return createAnnotationType(name, typeElement);
            } else if (kind == ElementKind.INTERFACE) {
                return createInterfaceKind(name, typeElement);
            } else {
                return createClassType(name, typeElement);
            }
        }

        private ClassNode createAnnotationType(String name, TypeElement typeElement) {
            return new ClassNode(name, Opcodes.ACC_ANNOTATION, ClassHelper.Annotation_TYPE, null, MixinNode.EMPTY_ARRAY);
        }

        private ClassNode createInterfaceKind(String name, TypeElement typeElement) {
            int modifiers = 0;
            Set<ClassNode> interfaces = new HashSet<ClassNode>();
            Set<GenericsType> generics = new HashSet<>();

            for (TypeParameterElement typeParameter : typeElement.getTypeParameters()) {
                    List<? extends TypeMirror> bounds = typeParameter.getBounds();
                    for (TypeMirror bound : bounds) {
                        ClassNode typeParam = getClass(bound.toString());
                        generics.add(new GenericsType(typeParam));
                    }
                }

            modifiers |= Opcodes.ACC_INTERFACE;
            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                interfaces.add(new ClassNode(Utilities.getClassName(interfaceType).toString(), Opcodes.ACC_INTERFACE, null));
            }
            return createClassNode(name, modifiers, null, interfaces.toArray(new ClassNode[interfaces.size()]), generics);
        }

        private ClassNode createClassType(String name, TypeElement typeElement) {
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

            return createClassNode(name, 0, superClass, new ClassNode[0], generics);
        }

        private ClassNode createClassNode(String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, Set<GenericsType> generics) {
            if ("java.lang.Object".equals(name) && superClass == null) { // NOI18N
                return ClassHelper.OBJECT_TYPE;
            }
            ClassNode classNode = new ClassNode(name, modifiers, superClass, interfaces, MixinNode.EMPTY_ARRAY);
            if (generics != null) {
                classNode.setGenericsTypes(generics.toArray(new GenericsType[generics.size()]));
            }
            return classNode;
        }
    }
}
