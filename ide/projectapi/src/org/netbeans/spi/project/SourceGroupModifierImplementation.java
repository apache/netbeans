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

package org.netbeans.spi.project;

import org.netbeans.api.project.SourceGroup;

/**
 * The SPI side of {@link org.netbeans.api.project.SourceGroupModifier}.
 * Expected to be present in project lookup of project types supporting automated creation
 * of {@link org.netbeans.api.project.SourceGroup} root folders.
 * @since org.netbeans.modules.projectapi 1.24
 * @author mkleint
 */
public interface SourceGroupModifierImplementation {
    /**
     * Creates a {@link org.netbeans.api.project.SourceGroup} of the given type and hint.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     * If the SourceGroup's type/hint is not supported, the implementation shall silently return null and not throw any exceptions.
     * If the SourceGroup of given type/hint already exists it shall be returned as well.
     *
     * @param type constant for type of sources
     * @param hint
     * @return the created or existing SourceGroup or null
     */

    SourceGroup createSourceGroup(String type, String hint);

    /**
     * checks if {@link org.netbeans.api.project.SourceGroup} of the given type and hint can be created.
     * Typically a type is a constant for java/groovy/ruby source roots and hint is a constant for main sources or test sources.
     * Please consult specific APIs fro the supported types/hints. Eg. <code>JavaProjectConstants</code> for java related project sources.
     * If the SourceGroup of given type/hint already exists it shall return true as well.
     *
     * @param type constant for type of sources
     * @param hint
     * @return true if the SourceGroup exists or can be created.
     */
    boolean canCreateSourceGroup(String type, String hint);

}
