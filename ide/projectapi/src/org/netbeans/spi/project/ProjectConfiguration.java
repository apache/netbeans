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

/**
 * Represents one user-selectable configuration of a particular project.
 * For example, it might represent a choice of main class and arguments.
 * Besides the implementor, only the project UI infrastructure is expected to use this class.
 * <p>An instance of a configuration may be passed in the context argument for
 * an {@link ActionProvider} when called on a main-project-sensitive action.
 * For details see {@link ProjectConfigurationProvider#configurationsAffectAction}.
 *
 * @author Adam Sotona, Jesse Glick
 * @since org.netbeans.modules.projectapi/1 1.11
 * @see ProjectConfigurationProvider
 */
public interface ProjectConfiguration {

    /**
     * Provides a display name by which this configuration may be identified in the GUI.
     * @return a human-visible display name
     */
    String getDisplayName();

}
