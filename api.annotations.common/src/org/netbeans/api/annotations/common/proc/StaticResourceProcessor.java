/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.api.annotations.common.proc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.StaticResource;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StaticResourceProcessor extends AbstractProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(StaticResource.class.getCanonicalName());
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element e : roundEnv.getElementsAnnotatedWith(StaticResource.class)) {
                StaticResource sr = e.getAnnotation(StaticResource.class);
                if (sr == null) {
                    continue;
                }
                Object v = ((VariableElement) e).getConstantValue();
                if (!(v instanceof String)) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@StaticResource may only be used on a String constant", e);
                    continue;
                }
                String resource = (String) v;
                // remainder adapted from LayerBuilder, but cannot reference that here
                if (sr.relative()) {
                    try {
                        resource = new URI(null, findPackage(e).replace('.', '/') + "/", null).resolve(new URI(null, resource, null)).getPath();
                    } catch (URISyntaxException x) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.getMessage(), e);
                        continue;
                    }
                }
                if (resource.startsWith("/")) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "do not use leading slashes on resource paths", e);
                    continue;
                }
                if (sr.searchClasspath()) {
                    boolean ok = false;
                    for (JavaFileManager.Location loc : new JavaFileManager.Location[] {StandardLocation.SOURCE_PATH, /* #181355 */ StandardLocation.CLASS_OUTPUT, StandardLocation.CLASS_PATH, StandardLocation.PLATFORM_CLASS_PATH}) {
                        try {
                            processingEnv.getFiler().getResource(loc, "", resource).openInputStream().close();
                            ok = true;
                        } catch (IOException ex) {
                            continue;
                        }
                    }
                    if (!ok) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "cannot find resource " + resource, e);
                    }
                } else {
                    try {
                        try {
                            processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", resource).openInputStream().close();
                        } catch (FileNotFoundException x) {
                            processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", resource).openInputStream().close();
                        }
                    } catch (IOException x) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "cannot find resource " + resource, e);
                    }
                }
            }
        }
        return true;
    }

    private static String findPackage(Element e) {
        switch (e.getKind()) {
        case PACKAGE:
            return ((PackageElement) e).getQualifiedName().toString();
        default:
            return findPackage(e.getEnclosingElement());
        }
    }

}
