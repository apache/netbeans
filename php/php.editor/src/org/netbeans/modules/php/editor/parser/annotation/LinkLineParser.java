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
package org.netbeans.modules.php.editor.parser.annotation;

import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class LinkLineParser implements AnnotationLineParser {
    static final String ANNOTATION_NAME = "link"; //NOI18N

    @Override
    public AnnotationParsedLine parse(final String line) {
        AnnotationParsedLine result = null;
        String[] tokens = line.split("[ \t]+"); //NOI18N
        if (tokens.length > 0 && ANNOTATION_NAME.equals(tokens[0])) {
            result = new LinkParsedLine(tokens.length > 1 ? tokens[1].trim() : "");
        }
        return result;
    }

}
