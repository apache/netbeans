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

package org.netbeans.modules.maven.api.archetype;

import java.util.List;

/**
 * Componentized provider of list of available archetypes.
 * It is used in New Maven project wizard to populate the list of available archetypes.
 * The providers are expected to be registered using {@link org.openide.util.lookup.ServiceProvider}.
 * There are 3 default implementations registered: One lists 1 basic archetype
 * (simple and the other lists all archetypes it find in local and remote repository indexes.
 * <p>For special archetypes to be visible in the UI, use {@link ArchetypeWizards#definedArchetype}.
 * @author mkleint
 */
public interface ArchetypeProvider {

    /**
     * return Archetype instances known to this provider. Is called once per
     * New Maven Project wizard invokation.
     * @return list of archetypes
     */
    List<Archetype> getArchetypes();
}
