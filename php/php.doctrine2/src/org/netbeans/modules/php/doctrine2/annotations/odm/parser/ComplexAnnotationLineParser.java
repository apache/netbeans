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
package org.netbeans.modules.php.doctrine2.annotations.odm.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.annotation.util.AnnotationUtils;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ComplexAnnotationLineParser implements AnnotationLineParser {

    private static final Set<ComplexAnnotation> ANNOTATIONS = new HashSet<>();
    static {
        Set<String> inlineAnnotations = new HashSet<>();
        inlineAnnotations.add("Index"); //NOI18N
        Set<String> typedParamRegexs = new HashSet<>();
        typedParamRegexs.add("repositoryClass"); //NOI18N
        ANNOTATIONS.add(new ComplexAnnotation("Document", inlineAnnotations, typedParamRegexs)); //NOI18N
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("\\("); //NOI18N
        for (ComplexAnnotation annotation : ANNOTATIONS) {
            if (tokens.length > 0 && AnnotationUtils.isTypeAnnotation(tokens[0], annotation.getName())) {
                String annotationName = tokens[0].trim();
                String description = line.substring(annotationName.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotationName.length()), annotationName);
                types.putAll(AnnotationUtils.extractInlineAnnotations(line, annotation.getInlineAnnotations()));
                types.putAll(AnnotationUtils.extractTypesFromParameters(line, annotation.getTypedParamRegexs()));
                result = new AnnotationParsedLine.ParsedLine(annotation.getName(), types, description, true);
                break;
            }
        }
        return result;
    }

    private static class ComplexAnnotation {
        private final String name;
        private final Set<String> inlineAnnotations;
        private final Set<String> typedParamRegexs;

        public ComplexAnnotation(String name, Set<String> inlineAnnotations, Set<String> typedParamRegexs) {
            this.name = name;
            this.inlineAnnotations = inlineAnnotations;
            this.typedParamRegexs = typedParamRegexs;
        }

        String getName() {
            return name;
        }

        Set<String> getInlineAnnotations() {
            return inlineAnnotations;
        }

        Set<String> getTypedParamRegexs() {
            return typedParamRegexs;
        }

    }

}
