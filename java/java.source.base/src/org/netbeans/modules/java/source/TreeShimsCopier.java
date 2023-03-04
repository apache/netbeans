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
package org.netbeans.modules.java.source;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TreeShimsCopier extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annos, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getRootElements()) {
            if (el.getKind() != ElementKind.CLASS)
                continue;
            TypeElement type = (TypeElement) el;
            String qualName = type.getQualifiedName().toString();
            String targetPackage = ALLOWED_CLASSES2TARGET_PACKAGE.get(qualName);
            if (targetPackage != null) {
                try {
                    Filer filer = processingEnv.getFiler();
                    FileObject fo = filer.getResource(StandardLocation.SOURCE_PATH, ((PackageElement) type.getEnclosingElement()).getQualifiedName().toString(), type.getSimpleName() + ".java");
                    URI source = fo.toUri();
                    StringBuilder path2Shims = new StringBuilder();
                    int p = qualName.split("\\.").length;
                    for (int i = 0; i < p; i++) {
                        path2Shims.append("../");
                    }
                    path2Shims.append("../java.source.base/src/org/netbeans/modules/java/source/TreeShims.java");
                    URI treeShims = source.resolve(path2Shims.toString());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = treeShims.toURL().openStream()) {
                        int r;

                        while ((r = in.read()) != (-1)) {
                            baos.write(r);
                        }
                    }
                    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    content = content.replace("package org.netbeans.modules.java.source;", "package " + targetPackage + ";");
                    try (OutputStream out = filer.createSourceFile(targetPackage + ".TreeShims", type).openOutputStream()) {
                        out.write(content.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return false;
    }

    private static final Map<String, String> ALLOWED_CLASSES2TARGET_PACKAGE = new HashMap<String, String>() {{
        put("org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider", "org.netbeans.modules.java.hints");
        put("org.netbeans.modules.java.completion.JavaCompletionTask", "org.netbeans.modules.java.completion.impl");
        put("org.netbeans.modules.editor.java.GoToSupport", "org.netbeans.modules.editor.java");
        put("org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase", "org.netbeans.modules.java.editor.base.semantic");
    }};
}
