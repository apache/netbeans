/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                        online.resolve(sources, remotes, online.getLocalRepository());
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
