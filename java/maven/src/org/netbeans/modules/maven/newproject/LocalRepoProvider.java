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

package org.netbeans.modules.maven.newproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.util.lookup.ServiceProvider;

/**
 * Lists archetypes found in local repository index. Will include both old archetypes
 * and archetypeng ones.
 * @author mkleint
 */
@ServiceProvider(service=ArchetypeProvider.class, position=300)
public class LocalRepoProvider implements ArchetypeProvider {
    
    public @Override List<Archetype> getArchetypes() {
        List<Archetype> lst = new ArrayList<Archetype>();
            List<NBVersionInfo> archs = RepositoryQueries.findArchetypesResult(Collections.singletonList(RepositoryPreferences.getInstance().getLocalRepository())).getResults();
            if (archs == null) {
                return lst;
            }
            for (NBVersionInfo art : archs) {
                Archetype arch = new Archetype(!"maven-archetype".equalsIgnoreCase(art.getPackaging())); //NOI18N
                arch.setArtifactId(art.getArtifactId());
                arch.setGroupId(art.getGroupId());
                arch.setVersion(art.getVersion());
                arch.setName(art.getProjectName());
                arch.setDescription(art.getProjectDescription());
                arch.setRepository(RepositorySystem.DEFAULT_LOCAL_REPO_ID);
                lst.add(arch);
            }
       
        return lst;
    }
    
}
