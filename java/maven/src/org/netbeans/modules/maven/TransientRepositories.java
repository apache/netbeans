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

package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;

/**
 * Manages transient repository registrations for a Maven project.
 */
final class TransientRepositories {

    private static final Logger LOGGER = Logger.getLogger(TransientRepositories.class.getName());

    private final NbMavenProject p;
    private final PropertyChangeListener l = new PropertyChangeListener() {
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                doUnregister();
                doRegister();
            }
        }
    };

    TransientRepositories(NbMavenProject p) {
        this.p = p;
    }

    private void doRegister() {
        MavenProject mp = p.getMavenProject();
        for (ArtifactRepository repo : mp.getRemoteArtifactRepositories()) {
            register(repo, mp.getRepositories());
        }
        for (ArtifactRepository repo : mp.getPluginArtifactRepositories()) {
            register(repo, mp.getPluginRepositories());
        }
    }

    void register() {
        doRegister();
        p.addPropertyChangeListener(l);
    }

    private void register(ArtifactRepository repo, List<Repository> definitions) {
        String id = repo.getId();
        String displayName = id;
        for (Repository r : definitions) {
            if (id.equals(r.getId())) {
                String n = r.getName();
                if (n != null) {
                    displayName = n;
                    break;
                }
            }
        }
        List<ArtifactRepository> mirrored = repo.getMirroredRepositories();
        if (mirrored.isEmpty()) {
            try {
                RepositoryPreferences.getInstance().addTransientRepository(this, repo.getId(), displayName, repo.getUrl(), RepositoryInfo.MirrorStrategy.ALL);
            } catch (URISyntaxException x) {
                LOGGER.log(Level.WARNING, "Ignoring repo with malformed URL: {0}", x.getMessage());
            }
        } else {
            for (ArtifactRepository mirr : mirrored) {
                try {
                    RepositoryPreferences.getInstance().addTransientRepository(this, mirr.getId(), mirr.getId(), mirr.getUrl(), RepositoryInfo.MirrorStrategy.ALL);
                } catch (URISyntaxException x) {
                    LOGGER.log(Level.WARNING, "Ignoring repo with malformed URL: {0}", x.getMessage());
                }
            }
        }
    }

    void unregister() {
        p.removePropertyChangeListener(l);
        doUnregister();
    }

    private void doUnregister() {
        RepositoryPreferences.getInstance().removeTransientRepositories(this);
    }

}
