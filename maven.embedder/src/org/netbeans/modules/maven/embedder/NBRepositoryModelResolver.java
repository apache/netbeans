/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.embedder;


import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.RepositorySystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Anuradha G
 */
class NBRepositoryModelResolver
        implements ModelResolver {

    private final MavenEmbedder embedder;
    private List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();


    NBRepositoryModelResolver(MavenEmbedder embedder) {
        this.embedder = embedder;
    }

    private NBRepositoryModelResolver(NBRepositoryModelResolver original) {
        this(original.embedder);
        this.remoteRepositories = new ArrayList<ArtifactRepository>(original.remoteRepositories);
    }

    @Override
    public ModelResolver newCopy() {
        return new NBRepositoryModelResolver(this);
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
        addRepository(repository, false);
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version)
            throws UnresolvableModelException {
        Artifact artifactParent = embedder.lookupComponent(RepositorySystem.class).createProjectArtifact(groupId, artifactId, version);
        try {
            embedder.resolve(artifactParent, remoteRepositories, embedder.getLocalRepository());
        } catch (ArtifactResolutionException ex) {
            Exceptions.printStackTrace(ex);
             throw new UnresolvableModelException(ex.getMessage(),  groupId , artifactId , version );
        } catch (ArtifactNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            throw new UnresolvableModelException( ex.getMessage(),  groupId , artifactId , version );
        }

        return new FileModelSource(artifactParent.getFile());
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
        RepositorySystem repositorySystem = embedder.lookupComponent(RepositorySystem.class);
        try {
            ArtifactRepository repo = repositorySystem.buildArtifactRepository(repository);
            if(replace) { 
                remoteRepositories.remove(repo);
            }
            remoteRepositories.add(repo);
            remoteRepositories = repositorySystem.getEffectiveRepositories( remoteRepositories );
        } catch (org.apache.maven.artifact.InvalidRepositoryException ex) {
            throw new InvalidRepositoryException(ex.toString(), repository, ex);
        }
    }
}
