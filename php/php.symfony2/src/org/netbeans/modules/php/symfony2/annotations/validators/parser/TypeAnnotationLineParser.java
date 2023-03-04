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
public class TypeAnnotationLineParser implements AnnotationLineParser {

    private static final String ANNOTATION_NAME = "Type"; //NOI18N

    private static final Set<String> PARAM_REGEX = new HashSet<>();
    static {
        PARAM_REGEX.add("type"); //NOI18N
    }

    @Override
    public AnnotationParsedLine parse(final String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("\\("); //NOI18N
        if (tokens.length > 0 && AnnotationUtils.isTypeAnnotation(tokens[0], ANNOTATION_NAME)) {
            String annotation = tokens[0].trim();
            String description = line.substring(annotation.length()).trim();
            Map<OffsetRange, String> types = new HashMap<>();
            types.put(new OffsetRange(0, annotation.length()), annotation);
            types.putAll(extractTypes(line));
            result = new AnnotationParsedLine.ParsedLine(ANNOTATION_NAME, types, description, true);
        }
        return result;
    }

    private static Map<OffsetRange, String> extractTypes(final String line) {
        Map<OffsetRange, String> result = AnnotationUtils.extractTypesFromParameters(line, PARAM_REGEX);
        for (Map.Entry<OffsetRange, String> entry : result.entrySet()) {
            if (isPhpDatatype(entry.getValue())) {
                result.remove(entry.getKey());
            }
        }
        return result;
    }

    private static boolean isPhpDatatype(final String type) {
        boolean result = false;
        if ("array".equalsIgnoreCase(type) || "bool".equalsIgnoreCase(type) || "callable".equalsIgnoreCase(type)
                || "float".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type) || "int".equalsIgnoreCase(type)
                || "integer".equalsIgnoreCase(type) || "long".equalsIgnoreCase(type) || "null".equalsIgnoreCase(type)
                || "numeric".equalsIgnoreCase(type) || "object".equalsIgnoreCase(type) || "real".equalsIgnoreCase(type)
                || "resource".equalsIgnoreCase(type) || "scalar".equalsIgnoreCase(type) || "string".equalsIgnoreCase(type)) { //NOI18N
            result = true;
        }
        return result;
    }

}
