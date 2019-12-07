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

package org.netbeans.modules.openide.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.lookup.implspi.AbstractServiceProviderProcessor;

public class ServiceProviderProcessor extends AbstractServiceProviderProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            ServiceProvider.class.getCanonicalName(),
            ServiceProviders.class.getCanonicalName()
        ));
    }

    /** public for ServiceLoader */
    public ServiceProviderProcessor() {}

    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
            ServiceProvider sp = el.getAnnotation(ServiceProvider.class);
            if (sp == null) {
                continue;
            }
            register(el, ServiceProvider.class, sp);
        }
        for (Element el : roundEnv.getElementsAnnotatedWith(ServiceProviders.class)) {
            ServiceProviders spp = el.getAnnotation(ServiceProviders.class);
            if (spp == null) {
                continue;
            }
            for (ServiceProvider sp : spp.value()) {
                register(el, ServiceProviders.class, sp);
            }
        }
        return true;
    }

    private void register(Element clazz, Class<? extends Annotation> annotation, ServiceProvider svc) {
        try {
            svc.service();
            assert false;
            return;
        } catch (MirroredTypeException e) {
            register(clazz, annotation, e.getTypeMirror(), svc.path(), svc.position(), svc.supersedes());
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element annotated, AnnotationMirror annotation, ExecutableElement attr, String userText) {
        if (processingEnv == null || annotated == null || !annotated.getKind().isClass()) {
            return Collections.emptyList();
        }

        if (   annotation == null
            || !"org.openide.util.lookup.ServiceProvider".contentEquals(((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName())) {
            return Collections.emptyList();
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
