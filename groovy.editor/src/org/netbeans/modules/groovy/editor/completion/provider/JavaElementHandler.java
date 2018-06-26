/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
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

        CountDownLatch cnt = new CountDownLatch(1);

        Map<MethodSignature, CompletionItem> result = Collections.synchronizedMap(new HashMap<MethodSignature, CompletionItem>());
        try {
            javaSource.runUserActionTask(new MethodCompletionHelper(cnt, javaSource, className, typeParameters,
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
        try {
            javaSource.runUserActionTask(new FieldCompletionHelper(cnt, javaSource, className,
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

        private final String className;

        private final String[] typeParameters;

        private final Set<AccessLevel> levels;

        private final String prefix;

        private final int anchor;

        private final boolean emphasise;

        private final Map<MethodSignature, CompletionItem> proposals;

        private final boolean nameOnly;

        public MethodCompletionHelper(CountDownLatch cnt, JavaSource javaSource, String className,
                String[] typeParameters, Set<AccessLevel> levels, String prefix, int anchor,
                Map<MethodSignature, CompletionItem> proposals, boolean emphasise, boolean nameOnly) {

            this.cnt = cnt;
            this.javaSource = javaSource;
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
                    List<String> params = getParameterListForMethod(element);
                    // FIXME this should be more accurate
                    TypeMirror returnType = element.getReturnType();

                    if (simpleName.toUpperCase(Locale.ENGLISH).startsWith(prefix.toUpperCase(Locale.ENGLISH)) &&
                        !simpleName.contains("$")) {
                        
                        proposals.put(getSignature(te, element, typeParameters, info.getTypes()), CompletionItem.forJavaMethod(
                                className, simpleName, params, returnType, element.getModifiers(), anchor, emphasise, nameOnly));
                    }
                }
            }

            cnt.countDown();
        }
        
        private List<String> getParameterListForMethod(ExecutableElement exe) {
            List<String> parameters = new ArrayList<String>();

            if (exe != null) {
                // generate a list of parameters
                // unfortunately, we have to work around # 139695 in an ugly fashion

                try {
                    List<? extends VariableElement> params = exe.getParameters(); // this can cause NPE's

                    for (VariableElement variableElement : params) {
                        TypeMirror tm = variableElement.asType();

                        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY) {
                            parameters.add(GroovyUtils.stripPackage(tm.toString()));
                        } else {
                            parameters.add(tm.toString());
                        }
                    }
                } catch (NullPointerException e) {
                    // simply do nothing.
                }
            }
            return parameters;
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

        public FieldCompletionHelper(CountDownLatch cnt, JavaSource javaSource, String className,
                Set<AccessLevel> levels, String prefix, int anchor,
                Map<FieldSignature, CompletionItem> proposals, boolean emphasise) {

            this.cnt = cnt;
            this.javaSource = javaSource;
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

                            proposals.put(getSignature(te, element), new CompletionItem.JavaFieldItem(
                                    className, simpleName, type, element.getModifiers(), anchor, emphasise));
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
