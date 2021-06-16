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

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import java.io.IOException;
import java.util.ArrayList;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.netbeans.modules.groovy.editor.java.JavaElementHandle;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class JavaElementHandler {

    private static final Logger LOG = Logger.getLogger(GroovyElementsProvider.class.getName());

    private final ParserResult info;

    private JavaElementHandler(ParserResult info) {
        this.info = info;
    }

    public static JavaElementHandler forCompilationInfo(ParserResult info) {
        return new JavaElementHandler(info);
    }

    // FIXME ideally there should be something like nice CompletionRequest once public and stable
    // then this class could implement some common interface
    public Map<MethodSignature, CompletionItem> getMethods(String className,
            String prefix, int anchor, String[] typeParameters, boolean emphasise, Set<AccessLevel> levels, boolean nameOnly) {
        JavaSource javaSource = createJavaSource();
        
        if (javaSource == null) {
            return Collections.emptyMap();
        }
        
        FileObject f = info.getSnapshot().getSource().getFileObject();

        CountDownLatch cnt = new CountDownLatch(1);

        Map<MethodSignature, CompletionItem> result = Collections.synchronizedMap(new HashMap<MethodSignature, CompletionItem>());
        try {
            javaSource.runUserActionTask(new MethodCompletionHelper(cnt, javaSource, f, className, typeParameters,
                    levels, prefix, anchor, result, emphasise, nameOnly), true);
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
                    Collections.singleton(AccessLevel.PUBLIC), prefix, anchor, result, emphasise), true);
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

        public MethodCompletionHelper(CountDownLatch cnt, JavaSource javaSource, FileObject groovySource, String className, 
                String[] typeParameters, Set<AccessLevel> levels, String prefix, int anchor,
                Map<MethodSignature, CompletionItem> proposals, boolean emphasise, boolean nameOnly) {

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
        }

        @Override
        public void run(CompilationController info) throws Exception {
            Elements elements = info.getElements();
            ElementAcceptor acceptor = new ElementAcceptor() {

                public boolean accept(Element e, TypeMirror type) {
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
                                signatureOf(info, element), Utilities.modelModifiersToGsf(element.getModifiers()));
                        
                        CompletionItem ci = CompletionAccessor.instance().createJavaMethod(
                                className, simpleName, getParametersForElement(info, element), returnTypeString, 
                                element.getModifiers(), anchor, emphasise, nameOnly);

                        proposals.put(getSignature(te, element, typeParameters, info.getTypes()), 
                                CompletionAccessor.instance().assignHandle(ci, h)
                        );
                    }
                }
            }

            cnt.countDown();
        }
        
        private List<String> signatureOf(CompilationInfo info, ExecutableElement exe) {
            List<String> fqns = new ArrayList<>(exe.getParameters().size());
            for (VariableElement v : exe.getParameters()) {
                fqns.add(info.getTypeUtilities().getTypeName(v.asType(), TypeUtilities.TypeNameOptions.PRINT_FQN).
                        toString());
            }
            return fqns;
        }
        
        private List<MethodParameter> getParametersForElement(CompilationController info, ExecutableElement exe) {
            List<MethodParameter> result = new ArrayList<>();
            if (exe == null) {
                return result;
            }
            List<? extends VariableElement> params = exe.getParameters(); // this can cause NPE's

            for (VariableElement variableElement : params) {
                TypeMirror tm = variableElement.asType();
                String fullName = info.getTypeUtilities().getTypeName(tm, TypeUtilities.TypeNameOptions.PRINT_FQN).toString();
                if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY) {
                    result.add(new MethodParameter(fullName, GroovyUtils.stripPackage(tm.toString()), variableElement.getSimpleName().toString()));
                } else {
                    result.add(new MethodParameter(fullName, fullName, variableElement.getSimpleName().toString()));
                }
            }
            return result;
        }
        
        private MethodSignature getSignature(TypeElement classElement, ExecutableElement element, String[] typeParameters, Types types) {
            String name = element.getSimpleName().toString();
            String[] parameters = new String[element.getParameters().size()];

            for (int i = 0; i < parameters.length; i++) {
                VariableElement var = element.getParameters().get(i);
                TypeMirror type = var.asType();
                String typeString = null;

                if (type.getKind() == TypeKind.TYPEVAR) {
                    List<? extends TypeParameterElement> declaredTypeParameters = element.getTypeParameters();
                    if (declaredTypeParameters.isEmpty()) {
                        declaredTypeParameters = classElement.getTypeParameters();
                    }
                    int j = -1;
                    for (TypeParameterElement typeParam : declaredTypeParameters) {
                        j++;
                        if (typeParam.getSimpleName().toString().equals(type.toString())) {
                            break;
                        }
                    }
// FIXME why we were doing this for signatures ??
//                    if (j >= 0 && j < typeParameters.length) {
//                        typeString = typeParameters[j];
//                    } else {
                        typeString = types.erasure(type).toString();
//                    }
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
        
        public FieldCompletionHelper(CountDownLatch cnt, JavaSource javaSource, FileObject groovySource, String className,
                Set<AccessLevel> levels, String prefix, int anchor,
                Map<FieldSignature, CompletionItem> proposals, boolean emphasise) {

            this.cnt = cnt;
            this.javaSource = javaSource;
            this.groovySource = groovySource;
            this.className = className;
            this.levels = levels;
            this.prefix = prefix;
            this.anchor = anchor;
            this.proposals = proposals;
            this.emphasise = emphasise;
        }

        public void run(CompilationController info) throws Exception {

            Elements elements = info.getElements();
            if (elements != null) {
                ElementAcceptor acceptor = new ElementAcceptor() {

                    public boolean accept(Element e, TypeMirror type) {
                        if (e.getKind() != ElementKind.FIELD) {
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
