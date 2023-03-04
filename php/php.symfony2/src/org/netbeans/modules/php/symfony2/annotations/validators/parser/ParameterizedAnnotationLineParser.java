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
public class ParameterizedAnnotationLineParser implements AnnotationLineParser {

    private static final Set<String> ANNOTATIONS = new HashSet<>();
    static {
        ANNOTATIONS.add("NotBlank"); //NOI18N
        ANNOTATIONS.add("Blank"); //NOI18N
        ANNOTATIONS.add("NotNull"); //NOI18N
        ANNOTATIONS.add("Null"); //NOI18N
        ANNOTATIONS.add("True"); //NOI18N
        ANNOTATIONS.add("False"); //NOI18N
        ANNOTATIONS.add("Email"); //NOI18N
        ANNOTATIONS.add("MinLength"); //NOI18N
        ANNOTATIONS.add("MaxLength"); //NOI18N
        ANNOTATIONS.add("Url"); //NOI18N
        ANNOTATIONS.add("Regex"); //NOI18N
        ANNOTATIONS.add("Ip"); //NOI18N
        ANNOTATIONS.add("Max"); //NOI18N
        ANNOTATIONS.add("Min"); //NOI18N
        ANNOTATIONS.add("Date"); //NOI18N
        ANNOTATIONS.add("DateTime"); //NOI18N
        ANNOTATIONS.add("Time"); //NOI18N
        ANNOTATIONS.add("Choice"); //NOI18N
        ANNOTATIONS.add("UniqueEntity"); //NOI18N
        ANNOTATIONS.add("Language"); //NOI18N
        ANNOTATIONS.add("Locale"); //NOI18N
        ANNOTATIONS.add("Country"); //NOI18N
        ANNOTATIONS.add("File"); //NOI18N
        ANNOTATIONS.add("Image"); //NOI18N
        ANNOTATIONS.add("Callback"); //NOI18N
        ANNOTATIONS.add("Valid"); //NOI18N
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split(line.contains("(") ? "\\(" : "[ \t]+"); //NOI18N
        for (String annotationName : ANNOTATIONS) {
            if (tokens.length > 0 && AnnotationUtils.isTypeAnnotation(tokens[0], annotationName)) {
                String annotation = tokens[0].trim();
                String description = line.substring(annotation.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotation.length()), annotation);
                result = new AnnotationParsedLine.ParsedLine(annotationName, types, description, true);
                break;
            }
        }
        return result;
    }

}
