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

package org.netbeans.modules.maven.indexer.spi.impl;

import java.util.Collection;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;

/**
 * 
 * Used internally.
 * 
 * Implementation of repository indexer (repository manager).
 * There is one implementation based on apache indexer.
 * 
 * @author Milos Kleint
 */
public interface RepositoryIndexerImplementation {
    
    /**
     * Index local repository or retrieve remote prepopulated index for local use.
     * @param repo
     */
    void indexRepo(RepositoryInfo repo);
    
    void updateIndexWithArtifacts(RepositoryInfo repo, Collection<Artifact> artifacts);

    void deleteArtifactFromIndex(RepositoryInfo repo, Artifact artifact);

}
