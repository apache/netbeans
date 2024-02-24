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

package org.netbeans.modules.textmate.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
@SupportedAnnotationTypes({"org.netbeans.modules.textmate.lexer.api.GrammarRegistration", "org.netbeans.modules.textmate.lexer.api.GrammarRegistrations",
    "org.netbeans.modules.textmate.lexer.api.GrammarInjectionRegistration", "org.netbeans.modules.textmate.lexer.api.GrammarInjectionRegistrations"})
@ServiceProvider(service = Processor.class)
public class CreateRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement grammarRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.textmate.lexer.api.GrammarRegistration");

        for (Element el : roundEnv.getElementsAnnotatedWith(grammarRegistration)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!grammarRegistration.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                process(el, am);
            }
        }

        TypeElement grammarRegistrations = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.textmate.lexer.api.GrammarRegistrations");

        for (Element el : roundEnv.getElementsAnnotatedWith(grammarRegistrations)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!grammarRegistrations.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (!e.getKey().getSimpleName().contentEquals("value")) continue;

                    for (AnnotationMirror r : NbCollections.iterable(NbCollections.checkedIteratorByFilter(((Iterable) e.getValue().getValue()).iterator(), AnnotationMirror.class, true))) {
                        process(el, r);
                    }
                }
            }
        }

        TypeElement grammarInjectionRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.textmate.lexer.api.GrammarInjectionRegistration");

        for (Element el : roundEnv.getElementsAnnotatedWith(grammarInjectionRegistration)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!grammarInjectionRegistration.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                processInjection(el, am);
            }
        }

        TypeElement grammarInjectionRegistrations = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.textmate.lexer.api.GrammarInjectionRegistrations");

        for (Element el : roundEnv.getElementsAnnotatedWith(grammarInjectionRegistrations)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!grammarInjectionRegistrations.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (!e.getKey().getSimpleName().contentEquals("value")) continue;

                    for (AnnotationMirror r : NbCollections.iterable(NbCollections.checkedIteratorByFilter(((Iterable) e.getValue().getValue()).iterator(), AnnotationMirror.class, true))) {
                        processInjection(el, r);
                    }
                }
            }
        }

        return true;
    }

    private void process(Element toRegister, AnnotationMirror mimeRegistration) throws LayerGenerationException {
        String grammar = null;
        String mimeType = null;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mimeRegistration.getElementValues().entrySet()) {
            Name simpleName = e.getKey().getSimpleName();
            if (simpleName.contentEquals("grammar")) {
                grammar = LayerBuilder.absolutizeResource(toRegister, (String) e.getValue().getValue());
                continue;
            }
            if (simpleName.contentEquals("mimeType")) {
                mimeType = (String) e.getValue().getValue();
                continue;
            }
        }

        if (mimeType != null && grammar != null) {
            if (mimeType.length() != 0) mimeType = "/" + mimeType;

            LayerBuilder layer = layer(toRegister);
            javax.tools.FileObject file = layer.validateResource(grammar, toRegister, null, null, false);
            try (InputStream in = file.openInputStream()) {
                IRegistryOptions opts = new IRegistryOptions() {
                    @Override
                    public String getFilePath(String scopeName) {
                        return null;
                    }
                    @Override
                    public InputStream getInputStream(String scopeName) throws IOException {
                        return null;
                    }
                    @Override
                    public Collection<String> getInjections(String scopeName) {
                        return null;
                    }
                };
                String scopeName = new Registry(opts).loadGrammarFromPathSync(grammar, in).getScopeName();
                String simpleName = grammar.lastIndexOf('/') != (-1) ? grammar.substring(grammar.lastIndexOf('/') + 1) : grammar;
                layer.file("Editors" + mimeType + "/" + simpleName)
                     .url("nbresloc:/" + grammar)
                     .stringvalue(TextmateTokenId.LanguageHierarchyImpl.GRAMMAR_MARK, scopeName).write();    //NOI18N
            } catch (Exception ex) {
                throw (LayerGenerationException) new LayerGenerationException(ex.getMessage()).initCause(ex);
            }
        }
    }

    private void processInjection(Element toRegister, AnnotationMirror injectionRegistration) throws LayerGenerationException {
        String grammar = null;
        String injectTo = null;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : injectionRegistration.getElementValues().entrySet()) {
            Name simpleName = e.getKey().getSimpleName();
            if (simpleName.contentEquals("grammar")) {
                grammar = (String) e.getValue().getValue();
                continue;
            }
            if (simpleName.contentEquals("injectTo")) {
                List<? extends AnnotationValue> values = (List<? extends AnnotationValue>) e.getValue().getValue();
                for (AnnotationValue value : values) {
                    if (injectTo == null) {
                        injectTo = (String) value.getValue();
                    } else {
                        injectTo += "," + value.getValue();
                    }
                }
            }
        }

        if (injectTo != null && grammar != null) {
            LayerBuilder layer = layer(toRegister);
            javax.tools.FileObject file = layer.validateResource(grammar, toRegister, null, null, false);
            try (InputStream in = file.openInputStream()) {
                IRegistryOptions opts = new IRegistryOptions() {
                    @Override
                    public String getFilePath(String scopeName) {
                        return null;
                    }
                    @Override
                    public InputStream getInputStream(String scopeName) throws IOException {
                        return null;
                    }
                    @Override
                    public Collection<String> getInjections(String scopeName) {
                        return null;
                    }
                };
                String scopeName = new Registry(opts).loadGrammarFromPathSync(grammar, in).getScopeName();
                String simpleName = grammar.lastIndexOf('/') != (-1) ? grammar.substring(grammar.lastIndexOf('/') + 1) : grammar;
                layer.file("Editors" + "/" + simpleName)
                     .url("nbresloc:/" + grammar)
                     .stringvalue(TextmateTokenId.LanguageHierarchyImpl.GRAMMAR_MARK, scopeName)
                     .stringvalue(TextmateTokenId.LanguageHierarchyImpl.INJECTION_MARK, injectTo).write();    //NOI18N
            } catch (Exception ex) {
                throw (LayerGenerationException) new LayerGenerationException(ex.getMessage()).initCause(ex);
            }
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.netbeans.modules.textmate.lexer.api.GrammarRegistration".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
            return Collections.emptyList();
        }

        if ("mimeType".contentEquals(attr.getSimpleName())) { // NOI18N
            return completeMimePath(annotated, annotation, attr, userText);
        }

        return Collections.emptyList();
    }
    
    private static final String[] DEFAULT_COMPLETIONS = {"text/plain", "text/xml", "text/x-java"}; // NOI18N
    private Processor COMPLETIONS;
    private Iterable<? extends Completion> completeMimePath(
        Element element, AnnotationMirror annotation,
        ExecutableElement attr, String userText
    ) {
        if (userText == null) {
            userText = "";
        }
        if (userText.startsWith("\"")) {
            userText = userText.substring(1);
        }

        Set<Completion> res = new HashSet<Completion>();
        if (COMPLETIONS == null) {
            String pathCompletions = System.getProperty("org.openide.awt.ActionReference.completion");
            if (pathCompletions != null) {
                ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
                if (l == null) {
                    l = Thread.currentThread().getContextClassLoader();
                }
                if (l == null) {
                    l = CreateRegistrationProcessor.class.getClassLoader();
                }
                try {
                    COMPLETIONS = (Processor)Class.forName(pathCompletions, true, l).getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    // no completions, OK
                    COMPLETIONS = this;
                }
            } else {
                return res;
            }
        }
        if (COMPLETIONS != null && COMPLETIONS != this) {
            COMPLETIONS.init(processingEnv);
            for (Completion completion : COMPLETIONS.getCompletions(element, annotation, attr, "Editors/" + userText)) {
                String v = completion.getValue();
                if (v == null) {
                    continue;
                }
                String[] arr = v.split("/");
                if (arr.length > 3 || arr.length < 2) {
                    continue;
                }
                if (!arr[0].equals("\"Editors")) {
                    continue;
                }
                if (arr[1].length() == 0 || Character.isUpperCase(arr[1].charAt(0))) {
                    // upper case means folders created by @MimeLocation very likelly
                    continue;
                }
                if (arr.length > 2) {
                    res.add(new TypeCompletion('"' + arr[1] + '/' + arr[2]));
                } else {
                    res.add(new TypeCompletion('"' + arr[1] + '/'));
                }
            }
        }
        if (res.isEmpty()) {
            for (String c : DEFAULT_COMPLETIONS) {
                if (c.startsWith(userText)) {
                    res.add(new TypeCompletion("\"" + c));
                }
            }
        }
        
        return res;
    }

    private static final class TypeCompletion implements Completion {

        private final String type;

        public TypeCompletion(String type) {
            this.type = type;
        }

        public String getValue() {
            return type;
        }

        public String getMessage() {
            return null;
        }

    }
}
