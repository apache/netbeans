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
package org.netbeans.modules.micronaut.completion;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.micronaut.db.Utils;

/**
 *
 * @author Dusan Balek
 */
public class MicronautJavaMappingPropertiesCompletion implements Processor {

    private static final Set<String> supportedAnnotationTypes = Set.of("io.micronaut.context.annotation.Mapper.Mapping");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        if (member != null && annotation != null) {
            TypeMirror tm = element.asType();
            if (tm.getKind() == TypeKind.EXECUTABLE) {
                TypeMirror type = null;
                switch (member.getSimpleName().toString()) {
                    case "from":
                        List<? extends TypeMirror> paramTypes = ((ExecutableType) tm).getParameterTypes();
                        if (paramTypes.size() == 1) {
                            type = paramTypes.get(0);
                        }
                        break;
                    case "to":
                        type = ((ExecutableType) tm).getReturnType();
                        break;
                }
                if (type != null && type.getKind() == TypeKind.DECLARED) {
                    TypeElement te = (TypeElement) ((DeclaredType) type).asElement();
                    if (Utils.getAnnotation(te.getAnnotationMirrors(), "io.micronaut.serde.annotation.Serdeable") != null) {
                        String format = "\"%s\"";
                        return ElementFilter.fieldsIn(te.getEnclosedElements()).stream().map(ve -> {
                            return new Completion() {
                                @Override
                                public String getValue() {
                                    return String.format(format, ve.getSimpleName().toString());
                                }
                                @Override
                                public String getMessage() {
                                    return null;
                                }
                            };
                        }).collect(Collectors.toList());
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
    }
}
