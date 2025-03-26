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

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
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
@SupportedAnnotationTypes({"org.netbeans.api.editor.mimelookup.MimeRegistration", "org.netbeans.api.editor.mimelookup.MimeRegistrations", "org.netbeans.spi.editor.mimelookup.MimeLocation"})
@ServiceProvider(service=Processor.class)
public class CreateRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement mimeRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.api.editor.mimelookup.MimeRegistration");

        for (Element el : roundEnv.getElementsAnnotatedWith(mimeRegistration)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!mimeRegistration.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                process(el, am);
            }
        }

        TypeElement mimeRegistrations = processingEnv.getElementUtils().getTypeElement("org.netbeans.api.editor.mimelookup.MimeRegistrations");

        for (Element el : roundEnv.getElementsAnnotatedWith(mimeRegistrations)) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!mimeRegistrations.equals(am.getAnnotationType().asElement())) {
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

        TypeElement mimeLocation = processingEnv.getElementUtils().getTypeElement("org.netbeans.spi.editor.mimelookup.MimeLocation");

        for (TypeElement el : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(mimeLocation))) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                if (!mimeLocation.equals(am.getAnnotationType().asElement())) {
                    continue;
                }

                checkMimeLocation(el, am);
            }
        }

        return true;
    }

    private void process(Element toRegister, AnnotationMirror mimeRegistration) throws LayerGenerationException {
        TypeMirror service = null;
        String mimeType = null;
        int    position = Integer.MAX_VALUE;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mimeRegistration.getElementValues().entrySet()) {
            Name simpleName = e.getKey().getSimpleName();
            if (simpleName.contentEquals("service")) {
                service = (TypeMirror) e.getValue().getValue();
                continue;
            }
            if (simpleName.contentEquals("mimeType")) {
                mimeType = (String) e.getValue().getValue();
                continue;
            }
            if (simpleName.contentEquals("position")) {
                position = (Integer) e.getValue().getValue();
                continue;
            }
        }

        if (mimeType != null) {
            if (mimeType.length() != 0) mimeType = "/" + mimeType;

            String folder = "";
            TypeElement apiTE = (TypeElement) processingEnv.getTypeUtils().asElement(service);
            TypeElement location = processingEnv.getElementUtils().getTypeElement("org.netbeans.spi.editor.mimelookup.MimeLocation");

            OUTER: for (AnnotationMirror am : apiTE.getAnnotationMirrors()) {
                if (!location.equals(am.getAnnotationType().asElement())) continue;

                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().contentEquals("subfolderName")) {
                        folder = "/" + (String) e.getValue().getValue();
                        break OUTER;
                    }
                }
            }

            instantiableClassOrMethod(toRegister, apiTE);
            layer(toRegister).instanceFile("Editors" + mimeType + folder, null, null).position(position).stringvalue("instanceOf", processingEnv.getElementUtils().getBinaryName(apiTE).toString()).write();    //NOI18N
        }
    }

    private void instantiableClassOrMethod(Element anntated, TypeElement apiClass) throws IllegalArgumentException, LayerGenerationException {
        TypeMirror typeMirror = processingEnv.getTypeUtils().getDeclaredType(apiClass);
        
        switch (anntated.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) anntated).toString();
                if (anntated.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", anntated);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(anntated.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", anntated);
                    }
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(anntated.asType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + " is not assignable to " + typeMirror, anntated);
                }
                if (!anntated.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(clazz + " is not public", anntated);
                }
                return;
            }
            case METHOD: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) anntated.getEnclosingElement()).toString();
                String method = anntated.getSimpleName().toString();
                if (!anntated.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + "." + method + " must be static", anntated);
                }
                if (!((ExecutableElement) anntated).getParameters().isEmpty()) {
                    throw new LayerGenerationException(clazz + "." + method + " must not take arguments", anntated);
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) anntated).getReturnType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + "." + method + " is not assignable to " + typeMirror, anntated);
                }
                return;
            }
            default:
                throw new IllegalArgumentException("Annotated element is not loadable as an instance: " + anntated);
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null || !annotated.getKind().isClass()) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.netbeans.api.editor.mimelookup.MimeRegistration".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
            return Collections.emptyList();
        }

        if ("mimeType".contentEquals(attr.getSimpleName())) { // NOI18N
            return completeMimePath(annotated, annotation, attr, userText);
        }
        if (!"service".contentEquals(attr.getSimpleName())) {
            return Collections.emptyList();
        }

        TypeElement jlObject = processingEnv.getElementUtils().getTypeElement("java.lang.Object");

        if (jlObject == null) {
            return Collections.emptyList();
        }

        Collection<Completion> result = new LinkedList<Completion>();
        List<TypeElement> toProcess = new LinkedList<TypeElement>();

        toProcess.add((TypeElement) annotated);

        while (!toProcess.isEmpty()) {
            TypeElement c = toProcess.remove(0);

            result.add(new TypeCompletion(c.getQualifiedName().toString() + ".class"));

            List<TypeMirror> parents = new LinkedList<TypeMirror>();

            parents.add(c.getSuperclass());
            parents.addAll(c.getInterfaces());

            for (TypeMirror tm : parents) {
                if (tm == null || tm.getKind() != TypeKind.DECLARED) {
                    continue;
                }

                TypeElement type = (TypeElement) processingEnv.getTypeUtils().asElement(tm);

                if (!jlObject.equals(type)) {
                    toProcess.add(type);
                }
            }
        }

        return result;
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

    private void checkMimeLocation(TypeElement clazz, AnnotationMirror am) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
            if (!e.getKey().getSimpleName().contentEquals("instanceProviderClass")) continue;

            TypeMirror ipc = (TypeMirror) e.getValue().getValue();

            if (ipc == null || ipc.getKind() != TypeKind.DECLARED) continue; //the compiler should have given the error

            TypeElement instanceProvider = processingEnv.getElementUtils().getTypeElement("org.netbeans.spi.editor.mimelookup.InstanceProvider");

            if (instanceProvider == null) {
                return ;
            }
            
            ExecutableElement createInstance = null;

            for (ExecutableElement ee : ElementFilter.methodsIn(instanceProvider.getEnclosedElements())) {
                if (ee.getSimpleName().contentEquals("createInstance")) { //TODO: check parameters
                    createInstance = ee;
                    break;
                }
            }

            if (createInstance == null) {
                throw new IllegalStateException("No instanceCreate in InstanceProvider!");
            }

            DeclaredType dipc = (DeclaredType) ipc;

            Types tu = processingEnv.getTypeUtils();
            ExecutableType member = (ExecutableType) tu.asMemberOf(dipc, createInstance);
            TypeMirror result = member.getReturnType();
            TypeMirror jlObject = processingEnv.getElementUtils().getTypeElement("java.lang.Object").asType();

            if (!tu.isSameType(tu.erasure(result), jlObject)) {
                if (!tu.isSubtype(tu.erasure(result), tu.erasure(clazz.asType()))) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "The InstanceProvider does not create instances of type " + clazz.getQualifiedName(), clazz, am, e.getValue());
                }
            }

            TypeElement tipc = (TypeElement) dipc.asElement();

            if (!tipc.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "The InstanceProvider implementation is not public.", clazz, am, e.getValue());
            }

            for (ExecutableElement c : ElementFilter.constructorsIn(tipc.getEnclosedElements())) {
                if (c.getParameters().isEmpty() && c.getModifiers().contains(Modifier.PUBLIC)) {
                    //OK
                    return;
                }
            }

            processingEnv.getMessager().printMessage(Kind.ERROR, "The InstanceProvider implementation does not provide a public no-arg constructor.", clazz, am, e.getValue());
        }
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
