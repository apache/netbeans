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

package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.BaseUtilities;

/**
 * Resolves URLs from a Maven repository using the online embedder.
 * <dl>
 * <dt>{@code m2:/org.grp:art:1.0:jar}
 * <dd>simple JAR artifact from Central
 * <dt>{@code m2:/org.grp:art:1.0:jar:javadoc}
 * <dd>Javadoc secondary artifact
 * </dl>
 * Currently only resolves artifacts from Central.
 * Could put repo ID in host field, but would require this to be moved to maven.indexer to use RepositoryPreferences.
 */
@URLStreamHandlerRegistration(position = 99, protocol="m2")
public class MavenProtocolHandler extends URLStreamHandler {

    protected @Override URLConnection openConnection(URL u) throws IOException {
        String path = u.getPath();
        if (!path.startsWith("/")) {
            throw new IOException(path);
        }
        String stuff = path.substring(1);
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        Artifact a;
        String[] pieces = stuff.split(":");
        if (pieces.length == 4) {
            a = online.createArtifact(pieces[0], pieces[1], pieces[2], pieces[3]);
        } else if (pieces.length == 5) {
            a = online.createArtifactWithClassifier(pieces[0], pieces[1], pieces[2], pieces[3], pieces[4]);
        } else {
            throw new IOException(stuff);
        }
        try {
            online.resolve(a, Collections.<ArtifactRepository>singletonList(online.createRemoteRepository(RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID)), online.getLocalRepository());
        } catch (Exception x) {
            throw new IOException(stuff + ": " + x, x);
        }
        File f = a.getFile();
        if (!f.isFile()) {
            throw new IOException("failed to download " + stuff);
        }
        Logger.getLogger(MavenProtocolHandler.class.getName()).log(Level.FINE, "resolved {0} -> {1}", new Object[] {stuff, f});
        return BaseUtilities.toURI(f).toURL().openConnection();
    }

}
