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

package org.netbeans.modules.projectapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * processor for LookupProvider.Register annotation.
 * @author mkleint
 */
@ServiceProvider(service=Processor.class)
public class LookupProviderAnnotationProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            LookupProvider.Registration.class.getCanonicalName(),
            ProjectServiceProvider.class.getCanonicalName(),
            LookupMerger.Registration.class.getCanonicalName()
        ));
    }

    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(LookupProvider.Registration.class)) {
            LookupProvider.Registration lpr = e.getAnnotation(LookupProvider.Registration.class);
            if (lpr.projectType().length == 0 && lpr.projectTypes().length == 0) {
                throw new LayerGenerationException("You must specify either projectType or projectTypes", e, processingEnv, lpr);
            }
            for (String type : lpr.projectType()) {
                layer(e).instanceFile("Projects/" + type + "/Lookup", null, LookupProvider.class, lpr, null).write();
            }
            for (LookupProvider.Registration.ProjectType type : lpr.projectTypes()) {
                layer(e).instanceFile("Projects/" + type.id() + "/Lookup", null, LookupProvider.class, type, null).position(type.position()).write();
            }
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ProjectServiceProvider.class)) {
            ProjectServiceProvider psp = e.getAnnotation(ProjectServiceProvider.class);
            List<TypeMirror> services = findServiceAnnotation(e);
            if (services.isEmpty()) {
                throw new LayerGenerationException("Must specify at least one service", e, processingEnv, psp);
            }
            String servicesBinName = null;
            for (TypeMirror service : services) {
                String n = processingEnv.getElementUtils().getBinaryName((TypeElement) processingEnv.getTypeUtils().asElement(service)).toString();
                if (n.equals(LookupMerger.class.getName())) {
                    throw new LayerGenerationException("@ProjectServiceProvider should not be used on LookupMerger; use @LookupMerger.Registration instead", e, processingEnv, psp);
                }
                servicesBinName = servicesBinName == null ? n : servicesBinName + "," + n;
            }
            String[] binAndMethodNames = findPSPDefinition(e, services, psp);
            if (psp.projectType().length == 0 && psp.projectTypes().length == 0) {
                throw new LayerGenerationException("You must specify either projectType or projectTypes", e, processingEnv, psp);
            }
            String fileBaseName = binAndMethodNames[0].replace('.', '-');
            if (binAndMethodNames[1] != null) {
                fileBaseName += "-" + binAndMethodNames[1];
            }
            for (String type : psp.projectType()) {
                LayerBuilder.File f = layer(e).file("Projects/" + type + "/Lookup/" + fileBaseName + ".instance").
                        methodvalue("instanceCreate", LazyLookupProviders.class.getName(), "forProjectServiceProvider").
                        stringvalue("class", binAndMethodNames[0]).
                        stringvalue("service", servicesBinName);
                if (binAndMethodNames[1] != null) {
                    f.stringvalue("method", binAndMethodNames[1]);
                }
                f.write();
            }
            for (LookupProvider.Registration.ProjectType type : psp.projectTypes()) {
                LayerBuilder.File f = layer(e).file("Projects/" + type.id() + "/Lookup/" + fileBaseName + ".instance").
                        methodvalue("instanceCreate", LazyLookupProviders.class.getName(), "forProjectServiceProvider").
                        stringvalue("class", binAndMethodNames[0]).
                        stringvalue("service", servicesBinName).
                        position(type.position());
                if (binAndMethodNames[1] != null) {
                    f.stringvalue("method", binAndMethodNames[1]);
                }
                f.write();
            }
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(LookupMerger.Registration.class)) {
            LookupMerger.Registration lmr = e.getAnnotation(LookupMerger.Registration.class);
            String fileBaseName;
            DeclaredType impl;
            if (e.getKind() == ElementKind.CLASS) {
                fileBaseName = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString().replace('.', '-');
                impl = (DeclaredType) e.asType();
            } else {
                fileBaseName = processingEnv.getElementUtils().getBinaryName((TypeElement) e.getEnclosingElement()).toString().replace('.', '-') +
                        "-" + e.getSimpleName().toString();
                impl = (DeclaredType) ((ExecutableElement) e).getReturnType();
            }
            DeclaredType service = findLookupMergerType(impl);
            if (service == null) {
                throw new LayerGenerationException("Not assignable to LookupMerger<T> for some T", e, processingEnv, lmr);
            }
            String serviceBinName = processingEnv.getElementUtils().getBinaryName((TypeElement) service.asElement()).toString();
            if (lmr.projectType().length == 0 && lmr.projectTypes().length == 0) {
                throw new LayerGenerationException("You must specify either projectType or projectTypes", e, processingEnv, lmr);
            }
            for (String type : lmr.projectType()) {
                layer(e).file("Projects/" + type + "/Lookup/" + fileBaseName + ".instance").
                        methodvalue("instanceCreate", LazyLookupProviders.class.getName(), "forLookupMerger").
                        instanceAttribute("lookupMergerInstance", LookupMerger.class).
                        stringvalue("service", serviceBinName).
                        write();
            }
            for (LookupProvider.Registration.ProjectType type : lmr.projectTypes()) {
                layer(e).file("Projects/" + type.id() + "/Lookup/" + fileBaseName + ".instance").
                        methodvalue("instanceCreate", LazyLookupProviders.class.getName(), "forLookupMerger").
                        instanceAttribute("lookupMergerInstance", LookupMerger.class).
                        stringvalue("service", serviceBinName).
                        position(type.position()).
                        write();
            }
        }
        return true;
    }

    private List<TypeMirror> findServiceAnnotation(Element e) throws LayerGenerationException {
        for (AnnotationMirror ann : e.getAnnotationMirrors()) {
            if (!ProjectServiceProvider.class.getName().equals(ann.getAnnotationType().toString())) {
                continue;
            }
            for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> attr : ann.getElementValues().entrySet()) {
                if (!attr.getKey().getSimpleName().contentEquals("service")) {
                    continue;
                }
                List<TypeMirror> r = new ArrayList<TypeMirror>();
                for (Object item : (List<?>) attr.getValue().getValue()) {
                    TypeMirror type = (TypeMirror) ((AnnotationValue) item).getValue();
                    Types typeUtils = processingEnv.getTypeUtils();
                    for (TypeMirror otherType : r) {
                        for (boolean swap : new boolean[] {false, true}) {
                            TypeMirror t1 = swap ? type : otherType;
                            TypeMirror t2 = swap ? otherType : type;
                            if (typeUtils.isSubtype(t1, t2)) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "registering under both " + typeUtils.asElement(t2).getSimpleName() + " and its subtype " + typeUtils.asElement(t1).getSimpleName() + " will not work if LookupMerger<" + typeUtils.asElement(t2).getSimpleName() + "> is used (#205151)", e, ann, attr.getValue());
                            }
                        }
                    }
                    r.add(type);
                }
                return r;
            }
            throw new LayerGenerationException("No service attr found", e);
        }
        throw new LayerGenerationException("No @ProjectServiceProvider found", e);
    }

    private String[] findPSPDefinition(Element e, List<TypeMirror> services, ProjectServiceProvider psp) throws LayerGenerationException {
        if (e.getKind() == ElementKind.CLASS) {
            TypeElement clazz = (TypeElement) e;
            if (clazz.getNestingKind().isNested() && !clazz.getModifiers().contains(Modifier.STATIC)) {
                throw new LayerGenerationException("An inner class cannot be constructed as a service", e, processingEnv, psp);
            }
            for (TypeMirror service : services) {
                if (!processingEnv.getTypeUtils().isAssignable(clazz.asType(), service)) {
                    throw new LayerGenerationException("Not assignable to " + service, e, processingEnv, psp);
                }
            }
            int constructorCount = 0;
            CONSTRUCTOR: for (ExecutableElement constructor : ElementFilter.constructorsIn(clazz.getEnclosedElements())) {
                if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                List<? extends VariableElement> params = constructor.getParameters();
                if (params.size() > 2) {
                    continue;
                }
                for (VariableElement param : params) {
                    if (!param.asType().equals(processingEnv.getElementUtils().getTypeElement(Project.class.getCanonicalName()).asType()) &&
                            !param.asType().equals(processingEnv.getElementUtils().getTypeElement(Lookup.class.getCanonicalName()).asType())) {
                        continue CONSTRUCTOR;
                    }
                }
                constructorCount++;
            }
            if (constructorCount != 1) {
                throw new LayerGenerationException("Must have exactly one public constructor optionally taking Project and/or Lookup", e, processingEnv, psp);
            }
            if (!clazz.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Class must be public", e, processingEnv, psp);
            }
            return new String[] {processingEnv.getElementUtils().getBinaryName(clazz).toString(), null};
        } else {
            ExecutableElement meth = (ExecutableElement) e;
            for (TypeMirror service : services) {
                if (!processingEnv.getTypeUtils().isAssignable(meth.getReturnType(), service)) {
                    throw new LayerGenerationException("Not assignable to " + service, e, processingEnv, psp);
                }
            }
            if (!meth.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Method must be public", e, processingEnv, psp);
            }
            if (!meth.getModifiers().contains(Modifier.STATIC)) {
                throw new LayerGenerationException("Method must be static", e, processingEnv, psp);
            }
            List<? extends VariableElement> params = meth.getParameters();
            if (params.size() > 2) {
                throw new LayerGenerationException("Method must take at most two parameters", e, processingEnv, psp);
            }
            for (VariableElement param : params) {
                if (!param.asType().equals(processingEnv.getElementUtils().getTypeElement(Project.class.getCanonicalName()).asType()) &&
                        !param.asType().equals(processingEnv.getElementUtils().getTypeElement(Lookup.class.getCanonicalName()).asType())) {
                    throw new LayerGenerationException("Method parameters may be either Lookup or Project", e, processingEnv, psp);
                }
            }
            if (!meth.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Class must be public", e, processingEnv, psp);
            }
            return new String[] {
                processingEnv.getElementUtils().getBinaryName((TypeElement) meth.getEnclosingElement()).toString(),
                meth.getSimpleName().toString()};
        }
    }

    private DeclaredType findLookupMergerType(DeclaredType t) {
        String rawName = processingEnv.getTypeUtils().erasure(t).toString();
        if (rawName.equals(LookupMerger.class.getName())) {
            List<? extends TypeMirror> args = t.getTypeArguments();
            if (args.size() == 1) {
                return (DeclaredType) args.get(0);
            } else {
                return null;
            }
        }
        for (TypeMirror supe : processingEnv.getTypeUtils().directSupertypes(t)) {
            DeclaredType result = findLookupMergerType((DeclaredType) supe);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
