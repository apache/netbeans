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
package org.netbeans.modules.groovy.editor.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.codehaus.groovy.transform.ASTTransformation;
import org.netbeans.modules.groovy.editor.api.parser.ApplyGroovyTransformation;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParser;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service=Processor.class)
public class ApplyGroovyTransformationProcessor extends LayerGeneratingProcessor {
    private static final Set<String> APPLY_DEFAULT = new HashSet<>(Arrays.asList(new String[] { "parse" })); // NOI18N

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ApplyGroovyTransformation.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ApplyGroovyTransformation.class)) {
            ApplyGroovyTransformation agt = e.getAnnotation(ApplyGroovyTransformation.class);
            String[] transformations = agt.value();
            Set<String> enableFor = new HashSet<>(Arrays.asList(agt.enable()));
            Set<String> disableFor = new HashSet<>(Arrays.asList(agt.disable()));
                    
            if (transformations.length == 0) {
                TypeMirror astType = processingEnv.getElementUtils().getTypeElement(ASTTransformation.class.getName()).asType();
                if (!processingEnv.getTypeUtils().isAssignable(e.asType(), astType)) {
                    throw new LayerGenerationException("Element " + e + " is not subclass of " + astType + " and transformation class is not specified by the annotation.", e); //NOI18N
                }
                
                String bn = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                transformations = new String[] { bn };
                if (enableFor.isEmpty()) {
                    enableFor = APPLY_DEFAULT;
                }
            } else {
                if (enableFor.isEmpty() && disableFor.isEmpty()) {
                    disableFor = APPLY_DEFAULT;
                }
            }
            
            for (String mt : agt.mimeTypes()) {
                String fnbase = "Editors/" + mt + "/Parser"; // NOI18N
                String en = null;
                
                if (e instanceof TypeElement) {
                    en = ((TypeElement)e).getQualifiedName().toString();
                } else if (e instanceof PackageElement) {
                    en = ((PackageElement)e).getQualifiedName().toString();
                } else {
                    throw new LayerGenerationException("Unexpected annotated element:" + e, e); //NOI18N
                }
                
                File f = null;
                if (!enableFor.isEmpty()) {
                    f = generateFile(f, e, fnbase, en, enableFor, "enable", transformations); // NOI18N
                }
                if (!disableFor.isEmpty()) {
                    f = generateFile(f, e, fnbase, en, disableFor, "disable", transformations); // NOI18N
                }
                if (f != null) {
                    f.write();
                }
            }
        }
        return true;
    }
    
    private File generateFile(File f, Element e, String fnbase, String en, Set<String> items, String att, String[] transformations) throws LayerGenerationException {
        boolean simple = items.size() == 1 && items.contains(ApplyGroovyTransformation.APPLY_PARSE);
        
        if (f == null) {
            f = layer(e).instanceFile(fnbase, en).
                    stringvalue("instanceOf", ParsingCompilerCustomizer.class.getName()). // NOI18N
                    methodvalue("instanceCreate", 
                            GroovyParser.class.getName(), "customizeTransformsFromLayer"); // NOI18N
        }
        if (!simple) {
            f.stringvalue("apply", String.join(",", items)); // NOI18N
        }
        f.stringvalue(att, String.join(",", transformations)); // NOI18N
        return f;
    }
    
}
