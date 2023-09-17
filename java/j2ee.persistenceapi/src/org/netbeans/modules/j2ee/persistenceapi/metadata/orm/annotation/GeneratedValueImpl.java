/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class GeneratedValueImpl implements GeneratedValue {

    private final ParseResult parseResult;

    public GeneratedValueImpl(AnnotationModelHelper helper, AnnotationMirror generatedValueAnnotation) {
        if (((TypeElement) generatedValueAnnotation.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("jakarta.")) {
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectEnumConstant("strategy", helper.resolveType("jakarta.persistence.GenerationType"), parser.defaultValue("AUTO")); // NOI18N
            parser.expectString("generator", parser.defaultValue("")); // NOI18N
            parseResult = parser.parse(generatedValueAnnotation);
        } else {
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectEnumConstant("strategy", helper.resolveType("javax.persistence.GenerationType"), parser.defaultValue("AUTO")); // NOI18N
            parser.expectString("generator", parser.defaultValue("")); // NOI18N
            parseResult = parser.parse(generatedValueAnnotation);
        }
    }

    public void setStrategy(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getStrategy() {
        return parseResult.get("strategy", String.class); // NOI18N
    }

    public void setGenerator(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getGenerator() {
        return parseResult.get("generator", String.class); // NOI18N
    }
}
