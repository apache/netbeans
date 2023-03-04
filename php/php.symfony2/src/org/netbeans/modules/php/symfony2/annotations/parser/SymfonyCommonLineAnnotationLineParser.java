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
package org.netbeans.modules.php.symfony2.annotations.parser;

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
public class SymfonyCommonLineAnnotationLineParser implements AnnotationLineParser {

    private static final AnnotationLineParser INSTANCE = new SymfonyCommonLineAnnotationLineParser();

    private static final Set<String> INLINE_ANNOTATIONS = new HashSet<>();
    static {
        INLINE_ANNOTATIONS.add("NotBlank"); //NOI18N
        INLINE_ANNOTATIONS.add("Blank"); //NOI18N
        INLINE_ANNOTATIONS.add("NotNull"); //NOI18N
        INLINE_ANNOTATIONS.add("Null"); //NOI18N
        INLINE_ANNOTATIONS.add("True"); //NOI18N
        INLINE_ANNOTATIONS.add("False"); //NOI18N
        INLINE_ANNOTATIONS.add("Email"); //NOI18N
        INLINE_ANNOTATIONS.add("MinLength"); //NOI18N
        INLINE_ANNOTATIONS.add("MaxLength"); //NOI18N
        INLINE_ANNOTATIONS.add("Url"); //NOI18N
        INLINE_ANNOTATIONS.add("Regex"); //NOI18N
        INLINE_ANNOTATIONS.add("Ip"); //NOI18N
        INLINE_ANNOTATIONS.add("Max"); //NOI18N
        INLINE_ANNOTATIONS.add("Min"); //NOI18N
        INLINE_ANNOTATIONS.add("Date"); //NOI18N
        INLINE_ANNOTATIONS.add("DateTime"); //NOI18N
        INLINE_ANNOTATIONS.add("Time"); //NOI18N
        INLINE_ANNOTATIONS.add("Choice"); //NOI18N
        INLINE_ANNOTATIONS.add("UniqueEntity"); //NOI18N
        INLINE_ANNOTATIONS.add("Language"); //NOI18N
        INLINE_ANNOTATIONS.add("Locale"); //NOI18N
        INLINE_ANNOTATIONS.add("Country"); //NOI18N
        INLINE_ANNOTATIONS.add("File"); //NOI18N
        INLINE_ANNOTATIONS.add("Image"); //NOI18N
        INLINE_ANNOTATIONS.add("Callback"); //NOI18N
        INLINE_ANNOTATIONS.add("Valid"); //NOI18N
    }

    private static final Set<String> TYPED_PARAMETERS = new HashSet<>();
    static {
        TYPED_PARAMETERS.add("type"); //NOI18N
    }

    // #258783 - register after Doctrine annotations (@Column)
    @AnnotationLineParser.Registration(position=1000)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        Map<OffsetRange, String> types = new HashMap<>();
        types.putAll(AnnotationUtils.extractInlineAnnotations(line, INLINE_ANNOTATIONS));
        types.putAll(AnnotationUtils.extractTypesFromParameters(line, TYPED_PARAMETERS));
        if (!types.isEmpty()) {
            result = new AnnotationParsedLine.ParsedLine("", types, line.trim());
        }
        return result;
    }

}
