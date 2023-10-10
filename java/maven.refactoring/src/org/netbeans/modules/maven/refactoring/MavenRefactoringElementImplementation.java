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

package org.netbeans.modules.maven.refactoring;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import static org.netbeans.modules.maven.indexer.api.RepositoryUtil.createArtifact;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

class MavenRefactoringElementImplementation implements RefactoringElementImplementation {

    private static final Logger LOG = Logger.getLogger(MavenRefactoringElementImplementation.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(MavenRefactoringElementImplementation.class);

    private final ReferringClass ref;
    private FileObject file;
    private static final FileObject NO_FILE = FileUtil.getConfigRoot();

    MavenRefactoringElementImplementation(ReferringClass ref) {
        this.ref = ref;
    }
    
    @Override public String getText() {
        return ref.clazz;
    }
    
    @Override public String getDisplayText() {
        return TreeElementFactory.getTreeElement(ref.artifact).getText(true);
    }
    
    @Override public boolean isEnabled() {
        return true;
    }
    
    @Override public void setEnabled(boolean enabled) {}
    
    @Override public void performChange() {}
    
    @Override public void undoChange() {}
    
    @Override public Lookup getLookup() {
        return Lookups.singleton(ref);
    }
    
    @Override public synchronized FileObject getParentFile() {
        if (file == null) {
            try {
                Artifact a = createArtifact(ref.artifact);
                File jar = a.getFile();
                if (!jar.exists()) { //#236842 try to minimize the cases when we need to let maven resolve and download artifacts
                    jar = RepositoryUtil.downloadArtifact(ref.artifact); // probably a no-op, since local hits must have been indexed
                }
                URL jarURL = FileUtil.urlForArchiveOrDir(jar);
                if (jarURL != null) {
                    SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(jarURL);
                    if (result.preferSources()) {
                        FileObject[] roots = result.getRoots();
                        for (FileObject root : roots) {
                            file = root.getFileObject(ref.clazz.replace('.', '/') + ".java");
                            if (file != null) {
                                LOG.log(Level.FINE, "found source file {0}", file);
                                break;
                            }
                        }
                        if (file == null) {
                            if (roots.length > 0) {
                                LOG.log(Level.WARNING, "did not find {0} among {1}", new Object[] {ref.clazz, Arrays.asList(roots)});
                            } else {
                                LOG.log(Level.FINE, "no source roots for {0}", jar);
                            }
                        }
                    } else {
                        LOG.log(Level.FINE, "ignoring non-preferred sources for {0}", jar);
                    }
                } else {
                    LOG.log(Level.WARNING, "no URL for {0}", jar);
                }
                if (file == null) {
                    if (jar.isFile()) {
                        file = URLMapper.findFileObject(new URL("jar:" + jar.toURI() + "!/" + ref.clazz.replace('.', '/') + ".class"));
                        if (file == null) {
                            LOG.log(Level.WARNING, "did not find {0} in {1}", new Object[] {ref.clazz, jar});
                        }
                    } else {
                        LOG.log(Level.WARNING, "{0} does not exist", jar);
                    }
                }
            } catch (Exception x) {
                LOG.log(Level.WARNING, null, x);
            }
            if (file == null) {
                file = NO_FILE;
            }
        }
        return file;
    }
    
    @Override public PositionBounds getPosition() {
        return null;
    }
    
    @Override public int getStatus() {
        return NORMAL;
    }
    
    @Override public void setStatus(int status) {}
    
    @Override public void openInEditor() {
        RP.post(new Runnable() { // resolve(...) may connect to the network
            @Override public void run() {
                FileObject f = getParentFile();
                if (f == NO_FILE) {
                    return;
                }
                if (f.hasExt("class")) {
                    try {
                        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                        Artifact sources = online.createArtifactWithClassifier(ref.artifact.getGroupId(), ref.artifact.getArtifactId(), ref.artifact.getVersion(), ref.artifact.getType(), "sources");
                        // XXX how do we get the exact remote repo from this?
                        // (Local artifact may well have come from a repository which is no longer registered in the IDE, or was never registered.
                        // Unfortunately it seems M3 does not preserve enough information in the local repo's metadata to find artifacts
                        // associated with one which was previously downloaded: _maven.repositories will remember the repositoryId but not its location.)
                        List<ArtifactRepository> remotes = RepositoryPreferences.getInstance().remoteRepositories(online);
                        online.resolveArtifact(sources, remotes, online.getLocalRepository());
                        // XXX this does not make ClassDataObject.OpenSourceCookie work immediately; clicking repeatedly seems to fix it
                    } catch (AbstractArtifactResolutionException x) {
                        LOG.log(Level.FINE, null, x);
                    }
                }
                try {
                    Openable o = DataObject.find(f).getLookup().lookup(Openable.class);
                    if (o != null) {
                        o.open();
                    } else {
                        LOG.log(Level.WARNING, "no Openable on {0}", getParentFile());
                    }
                } catch (DataObjectNotFoundException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
        });
    }
    
    @Override public void showPreview() {}

}
