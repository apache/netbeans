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
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import static org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_ID;
import static org.apache.maven.repository.RepositorySystem.DEFAULT_REMOTE_REPO_URL;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.spi.JsfReferenceImplementationProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Benjamin Asbach
 */
@ServiceProvider(service = JsfReferenceImplementationProvider.class)
public class MavenJsfReferenceImplementationProvider implements JsfReferenceImplementationProvider {

    static final Map<JsfVersion, String> JSF_VERSION_MAVEN_COORDINATES_MAPPING;
    static {
        EnumMap<JsfVersion, String> map = new EnumMap<>(JsfVersion.class);
        map.put(JsfVersion.JSF_1_0, "javax.faces:jsf-impl:1.1_02"); //seems to be not available in maven central
        map.put(JsfVersion.JSF_1_1, "javax.faces:jsf-impl:1.1_02");
        map.put(JsfVersion.JSF_1_2, "javax.faces:jsf-impl:1.2");
        map.put(JsfVersion.JSF_2_0, "com.sun.faces:jsf-impl:2.0.11");
        map.put(JsfVersion.JSF_2_1, "com.sun.faces:jsf-impl:2.1.29");
        map.put(JsfVersion.JSF_2_2, "com.sun.faces:jsf-impl:2.2.20");
        map.put(JsfVersion.JSF_2_3, "org.glassfish:jakarta.faces:2.3.21");
        map.put(JsfVersion.JSF_3_0, "org.glassfish:jakarta.faces:3.0.5");
        map.put(JsfVersion.JSF_4_0, "org.glassfish:jakarta.faces:4.0.5");
        map.put(JsfVersion.JSF_4_1, "org.glassfish:jakarta.faces:4.1.0-M1");
        JSF_VERSION_MAVEN_COORDINATES_MAPPING = Collections.unmodifiableMap(map);
    }

    @Override
    public Path artifactPathFor(JsfVersion jsfVersion) {
        String[] mavenCoordinates = JSF_VERSION_MAVEN_COORDINATES_MAPPING.get(jsfVersion).split(":");
        if (mavenCoordinates.length != 3) {
            return null;
        }
        String groupId = mavenCoordinates[0];
        String artifactId = mavenCoordinates[1];
        String version = mavenCoordinates[2];

        MavenEmbedder mavenEmbedder = EmbedderFactory.getOnlineEmbedder();

        ArtifactRepository localRepository = mavenEmbedder.getLocalRepository();
        ArtifactRepository remoteRepository = mavenEmbedder.createRemoteRepository(DEFAULT_REMOTE_REPO_URL, DEFAULT_REMOTE_REPO_ID);
        Artifact jsfRIArtifact = mavenEmbedder.createArtifact(groupId, artifactId, version, "jar");

        try {
            mavenEmbedder.resolveArtifact(jsfRIArtifact, Collections.singletonList(remoteRepository), localRepository);
        } catch (ArtifactResolutionException | ArtifactNotFoundException ex) {
            // throws exception on unresolvable artifact, return no path as it is unresolvable
            return null;
        }

        return Optional.ofNullable(jsfRIArtifact)
                .map(Artifact::getFile)
                .map(File::toPath)
                .filter(Files::exists)
                .orElse(null);
    }
}
