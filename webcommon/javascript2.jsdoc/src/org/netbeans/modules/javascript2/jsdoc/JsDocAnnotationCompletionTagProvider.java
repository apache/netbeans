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
package org.netbeans.modules.javascript2.jsdoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.jsdoc.completion.AssingTag;
import org.netbeans.modules.javascript2.jsdoc.completion.DescriptionTag;
import org.netbeans.modules.javascript2.jsdoc.completion.LinkTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeDescribedTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeNamedTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeSimpleTag;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementType;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocAnnotationCompletionTagProvider extends AnnotationCompletionTagProvider {

    private List<AnnotationCompletionTag> annotations;

    public JsDocAnnotationCompletionTagProvider(String name) {
        super(name);
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // List is build as unmodifiable List
    public synchronized List<AnnotationCompletionTag> getAnnotations() {
        if (annotations == null) {
            initAnnotations();
        }
        return annotations;
    }

    private void initAnnotations() {
        List<AnnotationCompletionTag> annotationsBuilder = new ArrayList<>(JsDocElementType.values().length);
        for (JsDocElementType type : JsDocElementType.values()) {
            if (type == JsDocElementType.UNKNOWN || type == JsDocElementType.CONTEXT_SENSITIVE) {
                continue;
            }

            switch (type.getCategory()) {
                case ASSIGN:
                    annotationsBuilder.add(new AssingTag(type.toString()));
                    break;
                case DECLARATION:
                    annotationsBuilder.add(new TypeSimpleTag(type.toString()));
                    break;
                case DESCRIPTION:
                    annotationsBuilder.add(new DescriptionTag(type.toString()));
                    break;
                case LINK:
                    annotationsBuilder.add(new LinkTag(type.toString()));
                    break;
                case NAMED_PARAMETER:
                    annotationsBuilder.add(new TypeNamedTag(type.toString()));
                    break;
                case UNNAMED_PARAMETER:
                    annotationsBuilder.add(new TypeDescribedTag(type.toString()));
                    break;
                default:
                    annotationsBuilder.add(new AnnotationCompletionTag(type.toString(), type.toString()));
                    break;
            }
        }
        annotations = Collections.unmodifiableList(annotationsBuilder);
    }
}
