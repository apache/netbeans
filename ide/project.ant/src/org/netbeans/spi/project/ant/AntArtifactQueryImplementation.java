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

import java.io.File;
import org.netbeans.api.project.ant.AntArtifact;

/**
 * Represents knowledge about the origin of an Ant build product.
 * <p>
 * Normal code does not need to implement this query. A standard implementation
 * first finds an associated {@link org.netbeans.api.project.Project} and
 * then checks to see if it supports {@link org.netbeans.spi.project.ant.AntArtifactProvider}.
 * You would only need to implement this directly in case your project type
 * generated build artifacts outside of the project directory or otherwise not marked
 * as "owned" by you according to {@link org.netbeans.api.project.FileOwnerQuery}.
 * @see org.netbeans.api.project.ant.AntArtifactQuery
 * @author Jesse Glick
 */
public interface AntArtifactQueryImplementation {

    /**
     * Find an Ant build artifact corresponding to a given file.
     * @param file a file on disk (need not currently exist) which might be a build artifact from an Ant script
     * @return an artifact information object, or null if it is not recognized
     */
    AntArtifact findArtifact(File file);
    
}
