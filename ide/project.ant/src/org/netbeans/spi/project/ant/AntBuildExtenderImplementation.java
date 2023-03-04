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

package org.netbeans.spi.project.ant;

import java.util.List;
import org.netbeans.api.project.Project;

/**
 * A project type's spi for {@link org.netbeans.api.project.ant.AntBuildExtender}'s wiring.
 * A typical setup in the project type includes:
 * <ul>
 * <li>Provide an instance of {@link org.netbeans.api.project.ant.AntBuildExtender} in project's lookup for use by 3rd
 * party modules.</<li>
 * <li>Use the new {@link org.netbeans.spi.project.support.ant.GeneratedFilesHelper#GeneratedFilesHelper(AntProjectHelper,AntBuildExtender)} constructor to
 *  create the helper for generating build related files.</<li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public interface AntBuildExtenderImplementation {
    
    
    
    /**
     * A declarative list of targets that are intended by the project type to be used
     * for extensions to plug into.
     * @return list of target names
     */
    List<String> getExtensibleTargets();

    /**
     * Returns Ant Project instance.
     * @return The project that this instance of AntBuildExtenderImplementation describes
     */
    Project getOwningProject();
}
