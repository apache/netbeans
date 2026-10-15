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

package org.openide.loaders;

import java.util.Map;

/** This is an interface for <em>smart templating</em>.
 * Implementations of this class can be registered in the global {@link org.openide.util.Lookup}
 * and allows anyone provide additional parameters to each {@link CreateFromTemplateHandler}s
 * when a template is instantiating.
 * Read more in the <a href="@TOP@/architecture-summary.html#loaders-script">howto document</a>.
 * <p>
 * Since templating system need not to depend on Data Systems APIs, the relevant interfaces
 * were moved to the {@code openide.filesystems.templates} module. This interface has been kept
 * for backward compatibility and DataSystems provide a compatibility bridge, which allows
 * old providers to participate. Module writers are encouraged to implement 
 * {@link org.netbeans.api.templates.CreateFromTemplateAttributes}
 * instead.
 * 
 * @author Jaroslav Tulach
 * @since 6.3
 * @since deprecated from 7.59
 * @deprecated Use {@link org.netbeans.api.templates.CreateFromTemplateAttributes} instead.
 */
@Deprecated
public interface CreateFromTemplateAttributesProvider {
    /** Called when a template is about to be instantiated to provide additional
     * values to the {@link CreateFromTemplateHandler} that will handle the 
     * template instantiation.
     * 
     * @param template the template that is being processed
     * @param target the destination folder
     * @param name the name of the object to create
     * @return map of named objects, or null
     */
    Map<String,?> attributesFor(DataObject template, DataFolder target, String name);
}
