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

package org.netbeans.api.annotations.common.proc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.StaticResource;

public class StaticResourceProcessor extends AbstractProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(StaticResource.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
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
