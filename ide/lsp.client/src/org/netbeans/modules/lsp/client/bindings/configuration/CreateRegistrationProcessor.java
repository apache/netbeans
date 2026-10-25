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

package org.netbeans.modules.lsp.client.bindings.configuration;

import java.util.Collections;
import java.util.HashSet;
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
import javax.swing.text.EditorKit;
import javax.swing.text.TextAction;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;

/**Based on CreateRegistrationProcessor from ide/textmate.lexer.
 */
@SupportedAnnotationTypes({"org.netbeans.modules.lsp.client.api.LanguageConfigurationRegistration",
                           "org.netbeans.modules.lsp.client.api.LanguageConfigurationRegistrations"})
@ServiceProvider(service = Processor.class)
public class CreateRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement languageConfigurationRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.lsp.client.api.LanguageConfigurationRegistration");

        for (Element el : roundEnv.getElementsAnnotatedWith(languageConfigurationRegistration)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!languageConfigurationRegistration.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                process(el, am);
            }
        }

        TypeElement languageConfigurationRegistrations = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.lsp.client.api.LanguageConfigurationRegistrations");

        for (Element el : roundEnv.getElementsAnnotatedWith(languageConfigurationRegistrations)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!languageConfigurationRegistrations.equals(am.getAnnotationType().asElement())) {
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

        return true;
    }

    private void process(Element toRegister, AnnotationMirror mimeRegistration) throws LayerGenerationException {
        String languageConfiguration = null;
        String mimeType = null;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mimeRegistration.getElementValues().entrySet()) {
            Name simpleName = e.getKey().getSimpleName();
            if (simpleName.contentEquals("languageConfiguration")) {
                languageConfiguration = LayerBuilder.absolutizeResource(toRegister, (String) e.getValue().getValue());
                continue;
            }
            if (simpleName.contentEquals("mimeType")) {
                mimeType = (String) e.getValue().getValue();
                continue;
            }
        }

        if (mimeType != null && languageConfiguration != null) {
            LayerBuilder layer = layer(toRegister);
            layer.file("Editors/" + mimeType + "/EditorKit.instance")
                 .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.api.LanguageFeatures", "editorKit")
                 .stringvalue("instanceOf", EditorKit.class.getName())
                 .stringvalue("mimeType", mimeType)
                 .write();
            layer.file("Editors/" + mimeType + "/languageConfiguration.instance")
                 .url("nbresloc:/" + languageConfiguration)
                 .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.api.LanguageFeatures", "languageConfiguration")
                 .write();
            layer.file("Editors/" + mimeType + "/BracesMatchers/languageConfigurationMatcher.instance")
                 .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.api.LanguageFeatures", "braceMatcher")
                 .stringvalue("instanceOf", BracesMatcherFactory.class.getName())
                 .write();
            layer.file("Editors/" + mimeType + "/typingCompletion.instance")
                 .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.api.LanguageFeatures", "typingCompletion")
                 .stringvalue("instanceOf", TypedTextInterceptor.Factory.class.getName())
                 .write();
            layer.file("Editors/" + mimeType + "/Actions/toggleComment.instance")
                 .methodvalue("instanceCreate", "org.netbeans.modules.lsp.client.api.LanguageFeatures", "toggleCommentAction")
                 .stringvalue("instanceOf", TextAction.class.getName())
                 .write();
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.netbeans.modules.lsp.client.api.LanguageConfigurationRegistration".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
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
