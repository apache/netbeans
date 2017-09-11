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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.uiapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registrations;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CompositeCategoryProviderAnnotationProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            Registration.class.getCanonicalName(),
            Registrations.class.getCanonicalName()
        ));
    }

    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Registration.class)) {
            Registration r = e.getAnnotation(Registration.class);
            if (r == null) {
                continue;
            }
            handle(e, r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Registrations.class)) {
            Registrations rr = e.getAnnotation(Registrations.class);
            if (rr == null) {
                continue;
            }
            for (Registration r : rr.value()) {
                handle(e, r);
            }
        }
        return true;
    }

    private void handle(Element e, Registration r) throws LayerGenerationException {
        String path = "Projects/" + r.projectType() + "/Customizer";
        if (r.category().length() > 0) {
            path += "/" + r.category();
        }
        boolean addsFolder = r.categoryLabel().length() > 0;
        if (addsFolder) {
            handleFolder(path, e, r);
        }
        if (e.getKind() == ElementKind.PACKAGE) {
            if (!addsFolder) {
                throw new LayerGenerationException("Must specify categoryLabel", e, processingEnv, r);
            }
        } else {
            File f = layer(e).instanceFile(path, addsFolder ? "Self" : null, CompositeCategoryProvider.class, r, null);
            f.position(addsFolder ? 0 : r.position());
            f.write();
        }
    }

    private void handleFolder(String path, Element e, Registration r) throws LayerGenerationException {
        if (r.category().length() == 0) {
            throw new LayerGenerationException("Must specify category", e, processingEnv, r);
        }
        layer(e).folder(path).bundlevalue("displayName", r.categoryLabel(), r, "categoryLabel").position(r.position()).write();
    }

}
