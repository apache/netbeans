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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.project.api.PhpAnnotations;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CustomTypeAnnnotationLineParser implements AnnotationLineParser {
    private static final AnnotationLineParser INSTANCE = new CustomTypeAnnnotationLineParser();
    private volatile boolean treatUnknownAnnotationsAsTypeAnnotations;

    @AnnotationLineParser.Registration(position = 10000)
    public static AnnotationLineParser getDefault() {
        return INSTANCE;
    }

    private CustomTypeAnnnotationLineParser() {
        treatUnknownAnnotationsAsTypeAnnotations = PhpAnnotations.getDefault().isUnknownAnnotationsAsTypeAnnotations();
        PhpAnnotations.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PhpAnnotations.PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS.equals(evt.getPropertyName())) {
                    treatUnknownAnnotationsAsTypeAnnotations = (boolean) evt.getNewValue();
                }
            }
        });
    }

    @Override
    public AnnotationParsedLine parse(String line) {
        AnnotationParsedLine result = null;
        if (treatUnknownAnnotationsAsTypeAnnotations) {
            result = parseLine(line, result);
        }
        return result;
    }

    private AnnotationParsedLine parseLine(String line, AnnotationParsedLine result) {
        String[] wsSeparatedLine = line.split("[\t ]", 2); //NOI18N
        if (wsSeparatedLine.length > 0) {
            String annotationPart = wsSeparatedLine[0];
            String[] tokens = annotationPart.split("\\(", 2); //NOI18N
            if (tokens.length > 0) {
                String annotation = tokens[0].trim();
                String description = line.substring(annotation.length()).trim();
                Map<OffsetRange, String> types = new HashMap<>();
                types.put(new OffsetRange(0, annotation.length()), annotation);
                result = new AnnotationParsedLine.ParsedLine(annotation, types, description, true);
            }
        }
        return result;
    }

}
