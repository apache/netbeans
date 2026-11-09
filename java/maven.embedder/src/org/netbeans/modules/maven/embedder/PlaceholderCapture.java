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

package org.netbeans.modules.maven.embedder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.aether.artifact.Artifact;

/**
 * Thread-safe sink for the placeholder (forged) artifacts created by
 * {@link ArtifactFixer} implementations during one maven operation
 * (e.g. a single invocation of
 * {@link MavenEmbedder#readProjectWithDependencies(org.apache.maven.execution.MavenExecutionRequest, boolean, PlaceholderCapture)}).
 * <p>
 * Artifact fixers are invoked from the threads of the (possibly parallel)
 * dependency collection, while the caller needs the collected artifacts after
 * the operation finishes, therefore the sink is passed down explicitly
 * instead of relying on thread-locals.
 */
public final class PlaceholderCapture {

    private final Set<Artifact> artifacts = ConcurrentHashMap.newKeySet();

    /**
     * Records a placeholder artifact forged by an {@link ArtifactFixer}.
     * May be called concurrently from multiple threads.
     * @param artifact the placeholder artifact
     */
    public void add(Artifact artifact) {
        artifacts.add(artifact);
    }

    /**
     * @return a snapshot of all placeholder artifacts recorded so far
     */
    public Set<Artifact> getArtifacts() {
        return new LinkedHashSet<>(artifacts);
    }

}
