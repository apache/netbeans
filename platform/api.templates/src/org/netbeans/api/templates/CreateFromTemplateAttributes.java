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

package org.netbeans.api.templates;

import java.util.Map;

/** This is an interface for <em>smart templating</em>.
 * Implementations of this class can be registered in the global {@link org.openide.util.Lookup}
 * and allows anyone provide additional parameters to each {@link CreateFromTemplateHandler}s
 * when a template is instantiating.
 * <p>
 * Implementations are called in the order of appearance in Lookup. The positions less than 0 are
 * reserved for the platform. Implementations called later can see and override
 * values defined by earlier CreateFromTemplateAttributes.
 * <p>
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 * <p>
 * This interface supersedes {@code CreateFromTemplateAttributesProvider} in {@code openide.loaders} module.
 * 
 * @author Svata Dedic
 */
public interface CreateFromTemplateAttributes {
    /** Called when a template is about to be instantiated to provide additional
     * values to the {@link CreateFromTemplateHandler} that will handle the 
     * template instantiation.
     * <p>
     * If the returned Map defines the same value as some {@link CreateFromTemplateAttributes} registered
     * earlier, the Map's value takes precedence. Parameters supplied by the {@link FileBuilder} cannot be
     * overriden.
     * 
     * @param desc the creation request
     * @return map of named objects, or null
     */
    Map<String,?> attributesFor(CreateDescriptor desc);
}
