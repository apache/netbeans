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
package org.netbeans.modules.php.symfony2.annotations.validators.parser;

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
public class EncapsulatingAnnotationLineParser implements AnnotationLineParser {

    private static final Map<String, Set<String>> ANNOTATIONS = new HashMap<>();
    static {
        Set<String> inlineAnnotations = new HashSet<>();
        inlineAnnotations.add("NotBlank"); //NOI18N
        inlineAnnotations.add("Blank"); //NOI18N
        inlineAnnotations.add("NotNull"); //NOI18N
        inlineAnnotations.add("Null"); //NOI18N
        inlineAnnotations.add("True"); //NOI18N
        inlineAnnotations.add("False"); //NOI18N
        inlineAnnotations.add("Email"); //NOI18N
        inlineAnnotations.add("MinLength"); //NOI18N
        inlineAnnotations.add("MaxLength"); //NOI18N
        inlineAnnotations.add("Url"); //NOI18N
        inlineAnnotations.add("Regex"); //NOI18N
        inlineAnnotations.add("Ip"); //NOI18N
        inlineAnnotations.add("Max"); //NOI18N
        inlineAnnotations.add("Min"); //NOI18N
        inlineAnnotations.add("Date"); //NOI18N
        inlineAnnotations.add("DateTime"); //NOI18N
        inlineAnnotations.add("Time"); //NOI18N
        inlineAnnotations.add("Choice"); //NOI18N
        inlineAnnotations.add("UniqueEntity"); //NOI18N
        inlineAnnotations.add("Language"); //NOI18N
        inlineAnnotations.add("Locale"); //NOI18N
        inlineAnnotations.add("Country"); //NOI18N
        inlineAnnotations.add("File"); //NOI18N
        inlineAnnotations.add("Image"); //NOI18N
        inlineAnnotations.add("Callback"); //NOI18N
        inlineAnnotations.add("Valid"); //NOI18N
        ANNOTATIONS.put("Collection", inlineAnnotations); //NOI18N
        ANNOTATIONS.put("All", inlineAnnotations); //NOI18N
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("\\("); //NOI18N
        for (Map.Entry<String, Set<String>> entry : ANNOTATIONS.entrySet()) {
            if (tokens.length > 0 && AnnotationUtils.isTypeAnnotation(tokens[0], entry.getKey())) {
                String annotation = tokens[0].trim();
                String description = line.substring(annotation.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotation.length()), annotation);
                types.putAll(AnnotationUtils.extractInlineAnnotations(line, entry.getValue()));
                result = new AnnotationParsedLine.ParsedLine(entry.getKey(), types, description, true);
                break;
            }
        }
        return result;
    }

}
