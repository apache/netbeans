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
package org.netbeans.modules.maven.repository;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 * @author Anuradha G
 */
public class ArtifactChildren extends ChildFactory<NBVersionInfo> {

    private String artifactId;
    private String groupId;
    private RepositoryInfo info;
    /**
     * creates a new instance of ArtifactChildren from browsing interface
     */
    public ArtifactChildren( RepositoryInfo info,String groupId, String artifactId) {
        this.info = info;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    protected @Override Node createNodeForKey(NBVersionInfo record) {
        boolean hasSources = record.isSourcesExists();
        boolean hasJavadoc = record.isJavadocExists();
        return new VersionNode(info, record, hasJavadoc, hasSources, groupId != null);
    }
    
    protected @Override boolean createKeys(List<NBVersionInfo> toPopulate) {
        Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(groupId, artifactId, Collections.singletonList(info));
        toPopulate.addAll(result.getResults());
        return true;
    }

}
