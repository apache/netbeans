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
package org.netbeans.modules.javascript2.extdoc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Provider for the ExtDoc documentations.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocDocumentationProvider implements JsDocumentationProvider {

    private static Set<String> supportedTags;

    private static final List<AnnotationCompletionTagProvider> ANNOTATION_PROVIDERS =
            Arrays.<AnnotationCompletionTagProvider>asList(new ExtDocAnnotationCompletionTagProvider("ExtDoc"));

    private static final SyntaxProvider SYNTAX_PROVIDER = new ExtDocSyntaxProvider();

    @Override
    public JsDocumentationHolder createDocumentationHolder(Snapshot snapshot) {
        return new ExtDocDocumentationHolder(this, snapshot);
    }

    @Override
    public synchronized Set getSupportedTags() {
        if (supportedTags == null) {
            supportedTags = new HashSet<String>(ExtDocElementType.values().length);
            for (ExtDocElementType type : ExtDocElementType.values()) {
                supportedTags.add(type.toString());
            }
            supportedTags.remove("unknown");
            supportedTags.remove("description");
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
