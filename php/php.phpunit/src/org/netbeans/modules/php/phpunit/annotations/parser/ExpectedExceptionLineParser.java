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
package org.netbeans.modules.php.phpunit.annotations.parser;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class ExpectedExceptionLineParser implements AnnotationLineParser {

    static final String ANNOTATION_NAME = "expectedException"; //NOI18N


    @Override
    public AnnotationParsedLine parse(final String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("[ \t]+"); //NOI18N
        if (tokens.length > 0 && ANNOTATION_NAME.equalsIgnoreCase(tokens[0])) {
            result = handleAnnotation(line, tokens);
        }
        return result;
    }

    private AnnotationParsedLine handleAnnotation(String line, String[] tokens) {
        String description = "";
        Map<OffsetRange, String> types = new HashMap<>();
        if (tokens.length > 1) {
            description = line.substring(tokens[0].length()).trim();
            int start = ANNOTATION_NAME.length() + countSpacesToFirstNonWhitespace(line.substring(ANNOTATION_NAME.length()));
            int end = start + tokens[1].length();
            types.put(new OffsetRange(start, end), line.substring(start, end));
        }
        return new ParsedLine(ANNOTATION_NAME, types, description, true);
    }

    private static int countSpacesToFirstNonWhitespace(final String line) {
        int result = 0;
        for (int i = 0; i < line.length(); i++) {
            if (Character.isWhitespace(line.charAt(i))) {
                result++;
            } else {
                break;
            }
        }
        return result;
    }

}
