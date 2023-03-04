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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.annotation.util.AnnotationUtils;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2OdmInlineAnnotationLineParser implements AnnotationLineParser {

    private static final AnnotationLineParser INSTANCE = new Doctrine2OdmInlineAnnotationLineParser();

    private static final Set<String> INLINE_ANNOTATIONS = new HashSet<>();
    static {
        INLINE_ANNOTATIONS.add("Index"); //NOI18N
    }

    private Doctrine2OdmInlineAnnotationLineParser() {
    }

    @AnnotationLineParser.Registration(position=601)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        final Map<OffsetRange, String> extractInlineTypes = AnnotationUtils.extractInlineAnnotations(line, INLINE_ANNOTATIONS);
        if (!extractInlineTypes.isEmpty()) {
            result = new ParsedLine("", extractInlineTypes, line.trim());
        }
        return result;
    }

}
