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
import java.util.Set;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface JsDocumentationProvider {

    /**
     * Parses and gets {@code JsDocumentationHolder} with processed documentation comments.
     *
     * @param snapshot to be parsed and stored into holder
     * @return JsDocumentationHolder
     */
    JsDocumentationHolder createDocumentationHolder(Snapshot snapshot);

    /**
     * Gets all tags supported by the documentation tool (like @author, @link, ...)
     *
     * @return set of all supported tags
     */
    Set<String> getSupportedTags();

    /**
     * Get list of {@link AnnotationCompletionTagProvider annotations providers} for this JavaScript documentation tool.
     *
     * @return list of annotations providers, never {@code null}
     */
    List<? extends AnnotationCompletionTagProvider> getAnnotationsProvider();

    /**
     * Get {@link SyntaxProvider syntax provider} for this JavaScript documentation tool.
     *
     * @return syntax provider of this documentation tool, can return {@code null) - then is used default SyntaxProvider
     */
    SyntaxProvider getSyntaxProvider();

}
