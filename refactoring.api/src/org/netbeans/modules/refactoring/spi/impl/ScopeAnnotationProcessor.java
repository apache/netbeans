/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.spi.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
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
