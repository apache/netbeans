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

package org.netbeans.modules.groovy.editor.completion.provider;

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.netbeans.modules.groovy.editor.java.JavaElementHandle;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class JavaElementHandler {

    private static final Logger LOG = Logger.getLogger(GroovyElementsProvider.class.getName());

    private final ParserResult info;
    private final CompletionContext context;

    private JavaElementHandler(ParserResult info, CompletionContext context) {
        this.info = info;
        this.context = context;
    }

    public static JavaElementHandler forCompilationInfo(ParserResult info, CompletionContext context) {
        return new JavaElementHandler(info, context);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    public Map<MethodSignature, CompletionItem> getMethods(String className, String prefix, int anchor, String[] typeParameters, boolean emphasise, Set<AccessLevel> levels, boolean nameOnly) {
        JavaSource javaSource = createJavaSource();
        
        if (javaSource == null) {
            return Collections.emptyMap();
        }
        
        FileObject f = info.getSnapshot().getSource().getFileObject();

        CountDownLatch cnt = new CountDownLatch(1);

        Map<MethodSignature, CompletionItem> result = Collections.synchronizedMap(new HashMap<MethodSignature, CompletionItem>());
        try {
            javaSource.runUserActionTask(new MethodCompletionHelper(cnt, javaSource, f, className, typeParameters,
                    levels, prefix, anchor, result, emphasise, nameOnly, context.isStaticMembers()), true);
        } catch (IOException ex) {
            LOG.log(Level.FINEST, "Problem in runUserActionTask :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }

        try {
            cnt.await();
        } catch (InterruptedException ex) {
            LOG.log(Level.FINEST, "InterruptedException while waiting on latch :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }
        return result;
    }

    public Map<FieldSignature, CompletionItem> getFields(String className,
            String prefix, int anchor, boolean emphasise) {
        JavaSource javaSource = createJavaSource();

        if (javaSource == null) {
            return Collections.emptyMap();
        }

        CountDownLatch cnt = new CountDownLatch(1);

        Map<FieldSignature, CompletionItem> result = Collections.synchronizedMap(new HashMap<FieldSignature, CompletionItem>());
        FileObject f = info.getSnapshot().getSource().getFileObject();
        try {
            javaSource.runUserActionTask(new FieldCompletionHelper(cnt, javaSource, f, className,
                    Collections.singleton(AccessLevel.PUBLIC), prefix, anchor, result, emphasise, context.isStaticMembers()), true);
        } catch (IOException ex) {
            LOG.log(Level.FINEST, "Problem in runUserActionTask :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }

        try {
            cnt.await();
        } catch (InterruptedException ex) {
            LOG.log(Level.FINEST, "InterruptedException while waiting on latch :  {0}", ex.getMessage());
            return Collections.emptyMap();
        }
        return result;
    }

    private JavaSource createJavaSource() {
        FileObject fileObject = info.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return null;
        }

        // get the JavaSource for our file.
        JavaSource javaSource = JavaSource.create(ClasspathInfo.create(fileObject));

        if (javaSource == null) {
            LOG.log(Level.FINEST, "Problem retrieving JavaSource from ClassPathInfo, exiting.");
            return null;
        }

        return javaSource;
    }

    private static class MethodCompletionHelper implements Task<CompilationController> {

        private final CountDownLatch cnt;

        private final JavaSource javaSource;

        private final FileObject groovySource;

        private final String className;

        private final String[] typeParameters;

        private final Set<AccessLevel> levels;

        private final String prefix;

        private final int anchor;

        private final boolean emphasise;

        private final Map<MethodSignature, CompletionItem> proposals;

        private final boolean nameOnly;
        
        private final boolean staticMethods;

        public MethodCompletionHelper(CountDownLatch cnt, JavaSource javaSource, FileObject groovySource, String className, 
                String[] typeParameters, Set<AccessLevel> levels, String prefix, int anchor,
                Map<MethodSignature, CompletionItem> proposals, boolean emphasise, boolean nameOnly, boolean staticMethods) {

            this.cnt = cnt;
            this.javaSource = javaSource;
            this.groovySource = groovySource;
            this.className = className;
            this.typeParameters = typeParameters;
            this.levels = levels;
            this.prefix = prefix;
            this.anchor = anchor;
            this.proposals = proposals;
            this.emphasise = emphasise;
            this.nameOnly = nameOnly;
            this.staticMethods = staticMethods;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            Elements elements = info.getElements();
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
            ElementAcceptor acceptor = new ElementAcceptor() {

                public boolean accept(Element e, TypeMirror type) {
                    if (staticMethods && !e.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)) {
                        return false;
                    }
                    if (e.getKind() != ElementKind.METHOD) {
                        return false;
                    }
                    for (AccessLevel level : levels) {
                        if (level.getJavaAcceptor().accept(e, type)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement te = elements.getTypeElement(className);
            if (te != null) {
                for (ExecutableElement element : ElementFilter.methodsIn(te.getEnclosedElements())) {
                    if (!acceptor.accept(element, te.asType())) {
                        continue;
                    }

                    String simpleName = element.getSimpleName().toString();
                    // FIXME this should be more accurate
                    TypeMirror returnType = element.getReturnType();
                    String returnTypeString = info.getTypeUtilities().getTypeName(returnType, TypeUtilities.TypeNameOptions.PRINT_FQN).toString();
                    if (simpleName.toUpperCase(Locale.ENGLISH).startsWith(prefix.toUpperCase(Locale.ENGLISH)) &&
                        !simpleName.contains("$")) {
                        
                        JavaElementHandle h = new JavaElementHandle(
                                simpleName, className, ElementHandle.create(element),
                                signatureOf(te, element, info.getTypes()), Utilities.modelModifiersToGsf(element.getModifiers()));
                        
                        CompletionItem ci = CompletionAccessor.instance().createJavaMethod(
                                className, simpleName, getParametersForElement(info, te, element), returnTypeString, 
                                element.getModifiers(), anchor, emphasise, nameOnly);

                        proposals.put(getSignature(te, element, info.getTypes()), 
                                CompletionAccessor.instance().assignHandle(ci, h)
                        );
                    }
                }
            }

            cnt.countDown();
        }
        
        private List<String> signatureOf(TypeElement classElement, ExecutableElement exe, Types types) {
            MethodSignature sign = getSignature(classElement, exe, types);
            return Arrays.asList(sign.getParameters());
        }
        
        private List<MethodParameter> getParametersForElement(CompilationController info, TypeElement classElement, ExecutableElement exe) {
            List<MethodParameter> result = new ArrayList<>();
            if (exe == null) {
                return result;
            }
            List<? extends VariableElement> params = exe.getParameters(); // this can cause NPE's
            
            for (VariableElement variableElement : params) {
                TypeMirror tm = variableElement.asType();
                String fullName;
                String typeName;
                
                if (tm.getKind() == TypeKind.TYPEVAR) {
                    fullName = substituteActualType(tm, classElement, exe, info.getTypes());
                    typeName = GroovyUtils.stripPackage(fullName);
                } else if (tm.getKind() == TypeKind.ARRAY &&
                        variableElement == params.get(params.size() - 1) &&
                        exe.isVarArgs()) {
                    tm = ((ArrayType)tm).getComponentType();
                    fullName = info.getTypeUtilities().getTypeName(tm, TypeUtilities.TypeNameOptions.PRINT_FQN).toString() + "..."; // NOI18N
                    typeName = info.getTypeUtilities().getTypeName(tm).toString() + "..."; // NOI18N
                } else { 
                    fullName = info.getTypeUtilities().getTypeName(tm, TypeUtilities.TypeNameOptions.PRINT_FQN).toString();
                    typeName = info.getTypeUtilities().getTypeName(tm).toString();
                }
                result.add(new MethodParameter(fullName, typeName, variableElement.getSimpleName().toString()));
            }
            return result;
        }
        
        private String substituteActualType(TypeMirror type, TypeElement classElement, ExecutableElement element, Types types) {
            List<? extends TypeParameterElement> declaredTypeParameters = element.getTypeParameters();
            if (declaredTypeParameters.isEmpty()) {
                // FIXME: this will not work well, if BOTH the class AND the method declares type parameters.
                declaredTypeParameters = classElement.getTypeParameters();
            } 
            int j = -1;
            for (TypeParameterElement typeParam : declaredTypeParameters) {
                j++;
                if (typeParam.getSimpleName().toString().equals(type.toString())) {
                    break;
                }
            }
            String typeString;
            if (j >= 0 && j < typeParameters.length) {
                typeString = typeParameters[j];
                // HACK HACK: currently the CC signatures contains typevar names. If the substituted type is not specific,
                // let's leave the old (also buggy) behaviour rather than presenting Object everywhere.
                if ("java.lang.Object".equals(typeString)) {
                    typeString = type.toString();
                }
            } else {
                typeString = type.toString();
            }
            return typeString;
        }
        
        private MethodSignature getSignature(TypeElement classElement, ExecutableElement element, Types types) {
            String name = element.getSimpleName().toString();
            String[] parameters = new String[element.getParameters().size()];

            for (int i = 0; i < parameters.length; i++) {
                VariableElement var = element.getParameters().get(i);
                TypeMirror type = var.asType();
                String typeString = null;

                if (type.getKind() == TypeKind.TYPEVAR) {
                    typeString = substituteActualType(type, classElement, element, types);
                } else {
                    typeString = type.toString();
                }

                int index = typeString.indexOf('<');
                if (index >= 0) {
                    typeString = typeString.substring(0, index);
                }
                parameters[i] = typeString;
            }
            return new MethodSignature(name, parameters);
        }
    }

    private static class FieldCompletionHelper implements Task<CompilationController> {

        private final CountDownLatch cnt;

        private final JavaSource javaSource;

        private final String className;

        private final Set<AccessLevel> levels;

        private final String prefix;

        private final int anchor;

        private final boolean emphasise;

        private final Map<FieldSignature, CompletionItem> proposals;
        
        private final FileObject groovySource;
        
        private final boolean staticMembers;
        
        public FieldCompletionHelper(CountDownLatch cnt, JavaSource javaSource, FileObject groovySource, String className,
                Set<AccessLevel> levels, String prefix, int anchor,
                Map<FieldSignature, CompletionItem> proposals, boolean emphasise, boolean staticMembers) {

            this.cnt = cnt;
            this.javaSource = javaSource;
            this.groovySource = groovySource;
            this.className = className;
            this.levels = levels;
            this.prefix = prefix;
            this.anchor = anchor;
            this.proposals = proposals;
            this.emphasise = emphasise;
            this.staticMembers = staticMembers;
        }

        public void run(CompilationController info) throws Exception {

            Elements elements = info.getElements();
            if (elements != null) {
                info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ElementAcceptor acceptor = new ElementAcceptor() {

                    public boolean accept(Element e, TypeMirror type) {
                        if (!e.getKind().isField()) {
                            return false;
                        }
                        if (staticMembers && !e.getModifiers().contains(javax.lang.model.element.Modifier.STATIC)) {
                            return false;
                        }
                        for (AccessLevel level : levels) {
                            if (level.getJavaAcceptor().accept(e, type)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement te = elements.getTypeElement(className);
                if (te != null) {
                    for (VariableElement element : ElementFilter.fieldsIn(te.getEnclosedElements())) {
                        if (!acceptor.accept(element, te.asType())) {
                            continue;
                        }

                        String simpleName = element.getSimpleName().toString();
                        TypeMirror type = element.asType();

                        if (simpleName.toUpperCase(Locale.ENGLISH).startsWith(prefix.toUpperCase(Locale.ENGLISH))) {
                            if (LOG.isLoggable(Level.FINEST)) {
                                LOG.log(Level.FINEST, simpleName + " " + type.toString());
                            }
                            
                            JavaElementHandle jh = new JavaElementHandle(
                                    simpleName, className, ElementHandle.create(element), null, 
                                    Utilities.modelModifiersToGsf(element.getModifiers()));

                            CompletionItem ci = new CompletionItem.JavaFieldItem(
                                        className, simpleName, type, element.getModifiers(), anchor, emphasise);
                            proposals.put(getSignature(te, element), 
                                CompletionAccessor.instance().assignHandle(ci, jh));
                        }
                    }
                }
            }

            cnt.countDown();
        }

        private FieldSignature getSignature(TypeElement classElement, VariableElement element) {
            String name = element.getSimpleName().toString();
            return new FieldSignature(name);
        }
    }
}
