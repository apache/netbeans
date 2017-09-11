/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
