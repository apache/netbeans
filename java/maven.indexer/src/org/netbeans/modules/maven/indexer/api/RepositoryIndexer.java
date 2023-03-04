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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.indexer.api;

import java.util.Collection;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.indexer.NexusRepositoryIndexerImpl;
import org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation;
import org.openide.util.Lookup;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexQueryProvider;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryIndexer {

    public static void indexRepo(RepositoryInfo repo) {
        assert repo != null;
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if(impl != null) {
            // fires 
            impl.indexRepo(repo);
        } else {
            repo.fireIndexChange();
        }
    }
    
    public static void updateIndexWithArtifacts(RepositoryInfo repo, Collection<Artifact> artifacts) {
        assert repo != null;
        if (artifacts == null || artifacts.isEmpty()) {
            return;
        }
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if(impl != null) {
            impl.updateIndexWithArtifacts(repo, artifacts);
        }
    }

    public static void deleteArtifactFromIndex(RepositoryInfo repo, Artifact artifact) {
        assert repo != null;
        if (artifact == null) {
            return;
        }
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if(impl != null) {
            impl.deleteArtifactFromIndex(repo, artifact);
        }
    }
    
    static RepositoryIndexerImplementation findImplementation(RepositoryInfo repo) {
        Lookup l = Lookup.getDefault();
        Collection<? extends RepositoryIndexQueryProvider> queryProviders = l.lookupAll(RepositoryIndexQueryProvider.class);
        for (RepositoryIndexQueryProvider queryProvider : queryProviders) {
            if(!(queryProvider instanceof NexusRepositoryIndexerImpl) && queryProvider.handlesRepository(repo)) {
                // skip if 
                return null;
            }
        }
        return l.lookup(RepositoryIndexerImplementation.class);
    }
    
}
