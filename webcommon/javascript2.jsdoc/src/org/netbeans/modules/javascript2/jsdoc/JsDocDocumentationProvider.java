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
package org.netbeans.modules.javascript2.jsdoc;

import java.util.*;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementType;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Provider of the jsDoc documentation type.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocDocumentationProvider implements JsDocumentationProvider {

    private Set<String> supportedTags;

    private static final List<AnnotationCompletionTagProvider> ANNOTATION_PROVIDERS =
            Arrays.<AnnotationCompletionTagProvider>asList(new JsDocAnnotationCompletionTagProvider("JsDoc"));
    private static final SyntaxProvider SYNTAX_PROVIDER = new JsDocSyntaxProvider();

    @Override
    public JsDocumentationHolder createDocumentationHolder(Snapshot snapshot) {
        return new JsDocDocumentationHolder(this, snapshot);
    }

    @Override
    public synchronized Set getSupportedTags() {
        if (supportedTags == null) {
            supportedTags = new HashSet<>(JsDocElementType.values().length);
            for (JsDocElementType type : JsDocElementType.values()) {
                supportedTags.add(type.toString());
            }
            supportedTags.remove("unknown");
            supportedTags.remove("contextSensitive");
        }
        return supportedTags;
    }

    @Override
    public List<AnnotationCompletionTagProvider> getAnnotationsProvider() {
        return ANNOTATION_PROVIDERS;
    }

    @Override
    public SyntaxProvider getSyntaxProvider() {
        return SYNTAX_PROVIDER;
    }
}
