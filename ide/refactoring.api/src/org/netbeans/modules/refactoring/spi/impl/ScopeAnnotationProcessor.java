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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.netbeans.modules.refactoring.spi.ui.ScopeReferences;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@SupportedAnnotationTypes({"org.netbeans.modules.refactoring.spi.ui.ScopeDescription",
"org.netbeans.modules.refactoring.spi.ui.ScopeReference",
"org.netbeans.modules.refactoring.spi.ui.ScopeReferences"})
@ServiceProvider(service = Processor.class)
public class ScopeAnnotationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (!roundEnv.processingOver()) {
            generateTypeList(roundEnv);
        }

        return false;
    }

    private void generateTypeList(RoundEnvironment roundEnv) throws LayerGenerationException {
        TypeElement scopeRegistration = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.refactoring.spi.ui.ScopeProvider.Registration");
        TypeElement scopeReference = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.refactoring.spi.ui.ScopeReference");
        TypeElement scopeReferences = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.refactoring.spi.ui.ScopeReferences");
        TypeMirror customProvider = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.refactoring.spi.ui.ScopeProvider.CustomScopeProvider").asType();
        TypeMirror provider = processingEnv.getElementUtils().getTypeElement("org.netbeans.modules.refactoring.spi.ui.ScopeProvider").asType();
        if (scopeRegistration == null || scopeReference == null || scopeReferences == null) {
            return;
        }
        Types typeUtils = processingEnv.getTypeUtils();

        for (Element annotated : roundEnv.getElementsAnnotatedWith(scopeRegistration)) {
            boolean custom;
            if (typeUtils.isSubtype(annotated.asType(), customProvider)) {
                custom = true;
            } else if (typeUtils.isSubtype(annotated.asType(), provider)) {
                custom = false;
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ERR_SUPER_TYPE, annotated);
                continue;
            }
            ScopeProvider.Registration ar = annotated.getAnnotation(ScopeProvider.Registration.class);
            LayerBuilder builder = layer(annotated);
            LayerBuilder.File f = builder.file("Scopes/ScopeDescriptions/" + ar.id() + ".instance");
            f.bundlevalue("displayName", ar.displayName(), ar, "displayName");
            f.stringvalue("id", ar.id());
            f.stringvalue("iconBase", ar.iconBase());
            f.intvalue("position", ar.position());
            f.newvalue("delegate", getFQN((TypeElement) annotated));
            f.stringvalue("instanceClass", "org.netbeans.modules.refactoring.spi.ui.ScopeProvider");
            if (custom) {
                f.methodvalue("instanceCreate", "org.netbeans.modules.refactoring.spi.impl.DelegatingCustomScopeProvider", "create");
            } else {
                f.methodvalue("instanceCreate", "org.netbeans.modules.refactoring.spi.impl.DelegatingScopeProvider", "create");
            }
            f.write();
        }
        for (Element annotated : roundEnv.getElementsAnnotatedWith(ScopeReferences.class)) {
            final LayerBuilder builder = layer(annotated);
            ScopeReferences refs = annotated.getAnnotation(ScopeReferences.class);
            if (refs == null) {
                continue;
            }
            for (ScopeReference ar : refs.value()) {
                processReference(ar, annotated, builder);
            }
        }

        for (Element annotated : roundEnv.getElementsAnnotatedWith(scopeReference)) {
            ScopeReference ar = annotated.getAnnotation(ScopeReference.class);
            processReference(ar, annotated, layer(annotated));
        }
    }
    private static final String ERR_SUPER_TYPE = "The class must extend org.netbeans.modules.refactoring.api.AbstractAnnotatedRefactoring";
    private static final String ERR_ID_NEEDED = "This annotation needs to be used together with ScopeDescription, or you need to specify the id.";

    private String getFQN(TypeElement clazz) {
        return processingEnv.getElementUtils().getBinaryName(clazz).toString();
    }

    private void processReference(ScopeReference ar, Element annotated, LayerBuilder builder) {
        String id = ar.id();
        if (id.isEmpty()) {
            ScopeProvider.Registration desc = annotated.getAnnotation(ScopeProvider.Registration.class);
            if (desc == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ERR_ID_NEEDED, annotated);
            } else {
                id = desc.id();
            }
        }
        LayerBuilder.File f = builder.file("Scopes/" + ar.path() + "/" + id + ".shadow");
        f.stringvalue("originalFile", "Scopes/ScopeDescriptions/" + id + ".instance");
        f.write();
    }
}
