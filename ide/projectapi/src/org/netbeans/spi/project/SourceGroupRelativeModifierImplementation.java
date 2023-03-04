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
 * Intermediate level for more structured projects, where the simple
 * type-based information are not sufficient to create an appropriate folder
 * structure. 
 * <p/>
 * Prototypically used in J2SE Modular projects, where tests or sources belong
 * to different modules, and it is critical to create the folder in the "correct"
 * one.
 * <p/>
 * The project can be partitioned to several (hiearchical) parts. SourceGroups for
 * certain types/hints can be created in some of those parts (see {@link SourceGroupModifierImplementation#canCreateSourceGroup}.
 * For example, java modular projects contains modules, a module may contain several places where sources are expected - these
 * form the part hierarchy. When the original SourceGroup is specific enough, the hierarchy argument may be
 * missing or can be even ignored by the modifier implementation - provided that the newly created folders have the correct
 * relationship to the original source group.
 * <p/>
 * Similar structure may be used in other types of projects. {@code projectParts} are abstract uninterpreted identifiers, so 
 * the implementation / project may choose any semantics suitable for the project type.
 * @author sdedic
 * @since 1.68
 */
public interface SourceGroupRelativeModifierImplementation {
    /**
     * Returns Modifier, which is bound to a specific location or conceptual part of the project.
     * @param existingGroup existing location or concept within the project
     * @param projectPart identifies part of the project. The meaning depends on the "existingGroup"
     * @return modifier able to create folders, or {@code null}, if the specified project part does not exist
     */
    public SourceGroupModifierImplementation    relativeTo(SourceGroup existingGroup, String... projectPart);
}
