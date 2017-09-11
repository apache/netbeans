/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
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
            process(e, r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(TemplateRegistrations.class)) {
            TemplateRegistrations rr = e.getAnnotation(TemplateRegistrations.class);
            if (rr == null) {
                continue;
            }
            for (TemplateRegistration t : rr.value()) {
                process(e, t);
            }
        }
        return true;
    }

    private void process(Element e, TemplateRegistration t) throws LayerGenerationException {
        LayerBuilder builder = layer(e);
        String basename;
        if (!t.id().isEmpty()) {
            if (t.content().length > 0) {
                throw new LayerGenerationException("Cannot specify both id and content", e, processingEnv, t);
            }
            basename = t.id();
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
        LayerBuilder.File f = builder.file(folder + basename);
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
        if (e.getKind() != ElementKind.PACKAGE) {
            if (t.page().isEmpty()) {
                try {
                    Class<?> iterClazz = Class.forName("org.openide.WizardDescriptor$InstantiatingIterator"); // NOI18N
                    f.instanceAttribute("instantiatingIterator", iterClazz);
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
            return new URI("nbresloc", "/" + path, null).normalize();
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
