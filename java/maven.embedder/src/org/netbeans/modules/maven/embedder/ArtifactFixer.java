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

import java.io.File;
import org.eclipse.aether.artifact.Artifact;

/**
 * Service which can resolve an artifact before a request is sent to the local repo.
 */
public interface ArtifactFixer {

    /**
     * Possibly resolves an artifact. May be called concurrently from multiple
     * threads of the dependency collection.
     * @param artifact an artifact which might or might not be available in local repo
     * @return a file to resolve it to locally, or null to skip
     */
    File resolve(Artifact artifact);

    /**
     * Possibly resolves an artifact, recording any placeholder artifacts it forges
     * into the given capture. May be called concurrently from multiple threads of the
     * dependency collection, so implementations must not rely on thread-locals to
     * receive the sink.
     * <p>
     * The default implementation simply delegates to {@link #resolve(Artifact)}, so
     * existing implementations keep working; they just do not report placeholder
     * artifacts.
     * @param artifact an artifact which might or might not be available in local repo
     * @param capture sink for the placeholder artifacts forged by this method, or
     *        null if the caller does not need them
     * @return a file to resolve it to locally, or null to skip
     */
    default File resolve(Artifact artifact, PlaceholderCapture capture) {
        return resolve(artifact);
    }

}
