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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyValidatorsAnnotationLineParser implements AnnotationLineParser {

    private static final AnnotationLineParser INSTANCE = new SymfonyValidatorsAnnotationLineParser();

    private static final List<AnnotationLineParser> PARSERS = new ArrayList<>();
    static {
        PARSERS.add(new ParameterizedAnnotationLineParser());
        PARSERS.add(new TypeAnnotationLineParser());
        PARSERS.add(new EncapsulatingAnnotationLineParser());
    }

    private SymfonyValidatorsAnnotationLineParser() {
    }

    @AnnotationLineParser.Registration(position=350)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        for (AnnotationLineParser annotationLineParser : PARSERS) {
            result = annotationLineParser.parse(line);
            if (result != null) {
                break;
            }
        }
        return result;
    }

}
