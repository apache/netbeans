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

import org.netbeans.api.project.ant.AntArtifact;

/**
 * Interface to be implemented by projects which can supply a list
 * of Ant build artifacts.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface AntArtifactProvider {

    /**
     * Get a list of supported build artifacts.
     * Typically the entries would be created using
     * {@link org.netbeans.spi.project.support.ant.AntProjectHelper#createSimpleAntArtifact}.
     * @return a list of build artifacts produced by this project;
     *         the target names must be distinct, and if this provider is in a
     *         project's lookup, {@link AntArtifact#getProject} must return the
     *         same project; list of artifacts for one project cannot contain
     *         two artifacts with the same {@link AntArtifact#getID ID}
     */
    AntArtifact[] getBuildArtifacts();
    
}
