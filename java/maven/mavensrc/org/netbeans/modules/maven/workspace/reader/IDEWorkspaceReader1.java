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
package org.netbeans.modules.maven.workspace.reader;

import java.io.File;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

/**
 * netbeansProjectMappings comma separated list of <GAV>=<path> where gav is G:A:V
 * @author mkleint
 */
@Named("ide")
@Singleton
public class IDEWorkspaceReader1 extends AbstractIDEWorkspaceReader implements WorkspaceReader {

    private final WorkspaceRepository repo = new WorkspaceRepository("ide");

    @Override
    public WorkspaceRepository getRepository() {
        return repo;
    }

    @Override
    public File findArtifact(Artifact artifact) {
        return super.findArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension(), artifact.getClassifier());
    }

    @Override
    public List<String> findVersions(Artifact artifact) {
        return super.findVersions(artifact.getGroupId(), artifact.getArtifactId());
    }
}
