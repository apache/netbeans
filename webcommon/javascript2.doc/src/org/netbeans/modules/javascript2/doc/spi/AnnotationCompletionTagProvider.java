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
package org.netbeans.modules.javascript2.doc.spi;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Encapsulates a JS annotations completion provider.
 *
 * <p>This class allows providing support for completion of JS annotations.</p>
 *
 * <p>Annotations are available per every {@code JsDocumentationProvider}. For <b>framework specific</b> annotations, use
 * {@link org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationProvider#getAnnotationsProvider()}.</p>

 * @see org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationProvider#getAnnotationsProvider()
 */
public abstract class AnnotationCompletionTagProvider {

    private final String name;

    /**
     * Create a new JS annotations provider with a name.
     *
     * @param  name <b>short, localized</b> name of this JS annotations provider (e.g., "JsDoc");
     *         never {@code null}
     * @throws NullPointerException if the {@code name} parameter is {@code null}
     */
    public AnnotationCompletionTagProvider(@NonNull String name) {
        Parameters.notNull("name", name); // NOI18N
        this.name = name;
    }

    /**
     * Get the <b>short, localized</b> name of this JS annotations provider.
     *
     * @return name; never {@code null}
     */
    public final String getName() {
        return name;
    }

    /**
     * Get all supported annotations.
     * 
     * @return all supported annotations
     */
    public abstract List<AnnotationCompletionTag> getAnnotations();

}