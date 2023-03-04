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

package org.netbeans.modules.templates;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.api.templates.FileBuilder;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.util.BaseUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
public class TemplateProcessor extends LayerGeneratingProcessor {

    @Override public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(TemplateRegistration.class.getCanonicalName(), TemplateRegistrations.class.getCanonicalName()));
    }

    @Override protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(TemplateRegistration.class)) {
            TemplateRegistration r = e.getAnnotation(TemplateRegistration.class);
            if (r == null) {
                continue;
            }
            process(e, r, roundEnv);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(TemplateRegistrations.class)) {
            TemplateRegistrations rr = e.getAnnotation(TemplateRegistrations.class);
            if (rr == null) {
                continue;
            }
            for (TemplateRegistration t : rr.value()) {
                process(e, t, roundEnv);
            }
        }
        return true;
    }
    
    private void checkPublicAbstract(Element e, TypeMirror clazz, TypeMirror typeMirror) throws LayerGenerationException {
        if (e.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new LayerGenerationException(clazz + " must not be abstract", e, processingEnv, null, null);
        }
        {
            boolean hasDefaultCtor = false;
            for (ExecutableElement constructor : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                if (constructor.getParameters().isEmpty()) {
                    hasDefaultCtor = true;
                    break;
                }
            }
            if (!hasDefaultCtor) {
                throw new LayerGenerationException(clazz + " must have a no-argument constructor", e, processingEnv, null, null);
            }
        }
        if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(e.asType(), typeMirror)) {
            throw new LayerGenerationException(clazz + " is not assignable to " + typeMirror, e, processingEnv, null, null);
        }
        if (!e.getModifiers().contains(Modifier.PUBLIC)) {
            throw new LayerGenerationException(clazz + " is not public", e, processingEnv, null, null);
        }
        if (((TypeElement) e).getNestingKind().isNested() && !e.getModifiers().contains(Modifier.STATIC)) {
            throw new LayerGenerationException(clazz + " is nested but not static", e, processingEnv, null, null);
        }
    }

    private void process(Element e, TemplateRegistration t, RoundEnvironment renv) throws LayerGenerationException {
        LayerBuilder builder = layer(e);
        String basename;
        boolean createFolder = false;
        if (!t.id().isEmpty()) {
            if (t.content().length > 0) {
                throw new LayerGenerationException("Cannot specify both id and content", e, processingEnv, t);
            }
            basename = t.id();
            if (basename.endsWith("/")) {
                basename = basename.substring(0, basename.length() - 1);
                createFolder = true;
            }
        } else if (t.content().length > 0) {
            basename = basename(t.content()[0]);
        } else {
            if (e.getKind() == ElementKind.CLASS) {
                basename = ((TypeElement) e).getQualifiedName().toString().replace('.', '-');
            } else if (e.getKind() == ElementKind.METHOD) {
                basename = ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString().replace('.', '-') + '-' + e.getSimpleName();
            } else {
                throw new LayerGenerationException("cannot use @Template on a package without specifying content", e, processingEnv, t);
            }
        }
        String folder = "Templates/" + t.folder() + '/';
        LayerBuilder.File f = createFolder ? builder.folder(folder + basename) : builder.file(folder + basename);
        f.boolvalue("template", true);
        f.position(t.position());
        if (!t.displayName().isEmpty()) {
            f.bundlevalue("displayName", t.displayName());
        }
        if (!t.iconBase().isEmpty()) {
            builder.validateResource(t.iconBase(), e, t, "iconBase", true);
            f.stringvalue("iconBase", t.iconBase());
        } else if (t.content().length == 0) {
            throw new LayerGenerationException("Must specify iconBase if content is not specified", e, processingEnv, t);
        }
        if (!t.description().isEmpty()) {
            f.urlvalue("instantiatingWizardURL", contentURI(e, t.description(), builder, t, "description"));
        }

        boolean handlerSpecified = false;
        try {
            t.createHandlerClass();
            // handler was not specified
        } catch (MirroredTypeException me) {
            TypeMirror handlerClass = me.getTypeMirror();
            if (handlerClass instanceof DeclaredType) {
                Element cfth = processingEnv.getElementUtils().getTypeElement(CreateFromTemplateHandler.class.getName());
                TypeMirror cfthType = cfth.asType();
                if (!processingEnv.getTypeUtils().isSameType(cfthType, handlerClass)) {
                    checkPublicAbstract(((DeclaredType) handlerClass).asElement(), handlerClass, cfthType);
                    String handlerClassName = processingEnv.getElementUtils().getBinaryName((TypeElement)((DeclaredType)handlerClass).asElement()).toString();
                    f.newvalue(FileBuilder.ATTR_TEMPLATE_HANDLER, handlerClassName);
                    handlerSpecified = true;
                }
            }
        }

        if (e.getKind() != ElementKind.PACKAGE) {
            if (t.page().isEmpty()) {
                // can annotate either WizardDescriptor.InstantiatingIterator, or CreateFromTemplateHandler.
                try {
                    Class<?> iterClazz = Class.forName("org.openide.WizardDescriptor$InstantiatingIterator"); // NOI18N
                    f.instanceAttribute("instantiatingIterator", iterClazz);
                } catch (LayerGenerationException ex) {
                    if (!handlerSpecified) {
                        f.instanceAttribute(FileBuilder.ATTR_TEMPLATE_HANDLER, CreateFromTemplateHandler.class);
                    }
                } catch (ClassNotFoundException ex) {
                    Messager msg = processingEnv.getMessager();
                    msg.printMessage(Diagnostic.Kind.ERROR,
                        "Either specify 'page' value or implement or return WizardDescriptor.InstantiatingIterator",  // NOI18N
                        e
                    );
                }
            } else {
                registerHTMLWizard(e, builder, f, t);
            }
        }
        if (t.content().length > 0) {
            f.url(contentURI(e, t.content()[0], builder, t, "content").toString());
            for (int i = 1; i < t.content().length; i++) {
                builder.file(folder + basename(t.content()[i])).url(contentURI(e, t.content()[i], builder, t, "content").toString()).position(0).write();
            }
        }
        if (!t.scriptEngine().isEmpty()) {
            f.stringvalue(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, t.scriptEngine());
        }
        if (t.category().length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String c : t.category()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(c);
            }
            f.stringvalue("templateCategory", sb.toString());
        }
        f.boolvalue("requireProject", t.requireProject());
        if (!t.targetName().trim().isEmpty()) {
            f.bundlevalue("targetName", t.targetName());                //NOI18N
        }
        String[] techIds = t.techIds();
        for (int i = 0; i < techIds.length; i++) {
            String id = techIds[i];
            f.stringvalue("techId." + i, id); // NOI18N
        }
        f.write();
    }

    private static String basename(String relativeResource) {
        return relativeResource.replaceFirst(".+/", "").replaceFirst("[.]template$", "");
    }

    private URI contentURI(Element e, String relativePath, LayerBuilder builder, TemplateRegistration t, String annotationMethod) throws LayerGenerationException {
        String path = LayerBuilder.absolutizeResource(e, relativePath);
        builder.validateResource(path, e, t, annotationMethod, false);
        try {
            return BaseUtilities.normalizeURI(new URI("nbresloc", "/" + path, null));
        } catch (URISyntaxException x) {
            throw new LayerGenerationException("could not translate " + path, e, processingEnv, t);
        }
    }

    private void registerHTMLWizard(Element e, LayerBuilder b, LayerBuilder.File f, TemplateRegistration t) throws LayerGenerationException {
        String pg;
        try {
            b.validateResource(t.page(), e, t, "page", true);
            pg = t.page();
        } catch (LayerGenerationException layerGenerationException) {
            pg = LayerBuilder.absolutizeResource(e, t.page());
        }
        b.validateResource(pg, e, t, "page", true);
        f.stringvalue("page", pg);
        if (e.getKind() != ElementKind.METHOD) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                "page() attribute can be used only on static method", e
            );
            return;
        }
        ExecutableElement ee = (ExecutableElement) e;
        if (!ee.getModifiers().contains(Modifier.STATIC)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                "page() attribute can be used only on static method", e
            );
            return;
        }
        TypeElement typeString = processingEnv.getElementUtils().getTypeElement("java.lang.String");
        if (!processingEnv.getTypeUtils().isSameType(typeString.asType(), ee.getReturnType())) {
            if (!isModel(processingEnv.getTypeUtils().asElement(ee.getReturnType()))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                    "page() attribute requires its method to return String or class generated by @Model annotation", e
                );
                return;
            }
        }
        if (!ee.getParameters().isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                "page() attribute requires its method to take no arguments", e
            );
            return;
        }
        f.methodvalue("instantiatingIterator", HTMLWizard.class.getName(), "create");
        TypeElement te = (TypeElement) e.getEnclosingElement();
        Name fqn = processingEnv.getElementUtils().getBinaryName(te);
        f.stringvalue("class", fqn.toString());
        f.stringvalue("method", ee.getSimpleName().toString());
    }
    
    private boolean isModel(Element e) {
        for (Element ee : e.getEnclosedElements()) {
            if ("modelFor".equals(ee.getSimpleName().toString())) {
                return true;
            }
        }
        return false;
    }
}
