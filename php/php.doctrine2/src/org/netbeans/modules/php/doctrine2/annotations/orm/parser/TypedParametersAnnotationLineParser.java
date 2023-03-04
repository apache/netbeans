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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

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
public class TypedParametersAnnotationLineParser implements AnnotationLineParser {

    private static final Map<String, Set<String>> ANNOTATIONS = new HashMap<>();
    static {
        Set<String> entityRegexs = new HashSet<>();
        entityRegexs.add("repositoryClass"); //NOI18N
        ANNOTATIONS.put("Entity", entityRegexs); //NOI18N

        Set<String> discriminatorMapRegexs = new HashSet<>();
        discriminatorMapRegexs.add("\\\"\\s*\\w+\\s*\\\""); //NOI18N
        ANNOTATIONS.put("DiscriminatorMap", discriminatorMapRegexs); //NOI18N

        Set<String> manyToOneRegexs = new HashSet<>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("ManyToOne", manyToOneRegexs); //NOI18N

        Set<String> manyToManyRegexs = new HashSet<>();
        manyToManyRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("ManyToMany", manyToManyRegexs); //NOI18N

        Set<String> oneToOneRegexs = new HashSet<>();
        oneToOneRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("OneToOne", oneToOneRegexs); //NOI18N

        Set<String> oneToManyRegexs = new HashSet<>();
        oneToManyRegexs.add("targetEntity"); //NOI18N
        ANNOTATIONS.put("OneToMany", oneToManyRegexs); //NOI18N
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
                types.putAll(AnnotationUtils.extractTypesFromParameters(line, entry.getValue()));
                result = new AnnotationParsedLine.ParsedLine(entry.getKey(), types, description, true);
                break;
            }
        }
        return result;
    }

}
