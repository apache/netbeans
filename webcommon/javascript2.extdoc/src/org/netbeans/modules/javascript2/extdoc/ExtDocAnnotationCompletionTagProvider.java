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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.extdoc.completion.DescriptionTag;
import org.netbeans.modules.javascript2.extdoc.completion.IdentDescribedTag;
import org.netbeans.modules.javascript2.extdoc.completion.IdentSimpleTag;
import org.netbeans.modules.javascript2.extdoc.completion.TypeDescribedTag;
import org.netbeans.modules.javascript2.extdoc.completion.TypeNamedTag;
import org.netbeans.modules.javascript2.extdoc.completion.TypeSimpleTag;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocAnnotationCompletionTagProvider extends AnnotationCompletionTagProvider {

    List<AnnotationCompletionTag> annotations = null;

    public ExtDocAnnotationCompletionTagProvider(String name) {
        super(name);
    }

    @Override
    public synchronized List<AnnotationCompletionTag> getAnnotations() {
        if (annotations == null) {
            initAnnotations();
        }
        return annotations;
    }

    private void initAnnotations() {
        annotations = new LinkedList<AnnotationCompletionTag>();
        for (ExtDocElementType type : ExtDocElementType.values()) {
            if (type == ExtDocElementType.UNKNOWN) {
                continue;
            }

            switch (type.getCategory()) {
                case DESCRIPTION:
                    annotations.add(new DescriptionTag(type.toString()));
                    break;
                case IDENT_SIMPLE:
                    annotations.add(new IdentSimpleTag(type.toString()));
                    break;
                case IDENT_DESCRIBED:
                    annotations.add(new IdentDescribedTag(type.toString()));
                    break;
                case TYPE_NAMED:
                    annotations.add(new TypeNamedTag(type.toString()));
                    break;
                case TYPE_SIMPLE:
                    annotations.add(new TypeSimpleTag(type.toString()));
                    break;
                case TYPE_DESCRIBED:
                    annotations.add(new TypeDescribedTag(type.toString()));
                    break;
                default:
                    annotations.add(new AnnotationCompletionTag(type.toString(), type.toString()));
                    break;
            }
        }
    }
}
