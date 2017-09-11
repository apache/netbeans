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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
@SuppressWarnings("DMI_COLLECTION_OF_URLS")
@ProjectServiceProvider(service=BinaryForSourceQueryImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenBinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {

    private final Project project;
    private final Map<URL,Res> results;
    
    public MavenBinaryForSourceQueryImpl(Project prj) {
        project = prj;
        results = new HashMap<URL, Res>();
    }
    
    public @Override BinaryForSourceQuery.Result findBinaryRoots(URL url) {
        if (results.containsKey(url)) {
            return results.get(url);
        }
        if ("file".equals(url.getProtocol())) { //NOI18N
            try {
                File fil = Utilities.toFile(url.toURI());
                fil = FileUtil.normalizeFile(fil);
                Res toReturn = findFor(fil);
                if (toReturn != null) {
                    results.put(url, toReturn);
                }
                return toReturn;
            }
            catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private Res findFor(File fil) {
        MavenProject mav = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        String src = mav.getBuild() != null ? mav.getBuild().getSourceDirectory() : null;
        String testSrc = mav.getBuild() != null ? mav.getBuild().getTestSourceDirectory() : null;
        File srcFile = src != null ? FileUtil.normalizeFile(new File(src)) : null;
        File testSrcFile = testSrc != null ? FileUtil.normalizeFile(new File(testSrc)) : null;
        Res toReturn = checkRoot(fil, srcFile, testSrcFile);
        if (toReturn != null) {
            return toReturn;
        }
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        for (URI res : impl.getResources(false)) {
            toReturn = checkRoot(fil, res, null);
            if (toReturn != null) {
                return toReturn;
            }
        }
        for (URI res : impl.getResources(true)) {
            toReturn = checkRoot(fil, null, res);
            if (toReturn != null) {
                return toReturn;
            }
        }
        for (URI gen : impl.getGeneratedSourceRoots(false)) {
            toReturn = checkRoot(fil, gen, null);
            if (toReturn != null) {
                return toReturn;
            }
        }
        for (URI gen : impl.getGeneratedSourceRoots(true)) {
            toReturn = checkRoot(fil, null, gen);
            if (toReturn != null) {
                return toReturn;
            }
        }
        for (JavaLikeRootProvider rp : project.getLookup().lookupAll(JavaLikeRootProvider.class)) {
            toReturn = checkRoot(fil, FileUtilities.getDirURI(project.getProjectDirectory(), "src/main/" + rp.kind()), FileUtilities.getDirURI(project.getProjectDirectory(), "src/test/" + rp.kind()));
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    private Res checkRoot(File root, File source, File test) {
        if (source != null && source.equals(root)) {
            return new Res(false, project.getLookup().lookup(NbMavenProjectImpl.class));
        }
        if (test != null && test.equals(root)) {
            return new Res(true, project.getLookup().lookup(NbMavenProjectImpl.class));
        }
        return null;
    }

    private Res checkRoot(File root, URI source, URI test) {
        return checkRoot(root,
                         source != null ? FileUtil.normalizeFile(Utilities.toFile(source)) : null,
                         test != null ? FileUtil.normalizeFile(Utilities.toFile(test)) : null);
    }

    
    private static class Res implements BinaryForSourceQuery.Result {
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private NbMavenProjectImpl project;
        private boolean isTest;
        Res(boolean test, NbMavenProjectImpl prj) {
            isTest = test;
            project = prj;

        }
        
        public @Override URL[] getRoots() {
            //#222352 afaik project.getOutputDirectory() is always non null, but apparently
            //FileUtil.urlForArchiveOrDir can return null.
            final List<URL> result = new ArrayList<>(2);
            URL url = FileUtil.urlForArchiveOrDir(project.getProjectWatcher().getOutputDirectory(isTest));
            if (url != null) {
                result.add(url);
            }
            Artifact art = project.getOriginalMavenProject().getArtifact();
            if(art != null) {
                File artFile = FileUtilities.convertArtifactToLocalRepositoryFile(art);
                final URL artUrl = FileUtil.urlForArchiveOrDir(artFile);
                if (artUrl != null) {
                    result.add(artUrl);
                }
            }
            return result.toArray(new URL[result.size()]);
        }   

        public @Override void addChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.add(changeListener);
            }
        }
        
        public @Override void removeChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.remove(changeListener);
            }
        }
        
        void fireChanged() {
            List<ChangeListener> lists = new ArrayList<ChangeListener>();
            synchronized(listeners) {
                lists.addAll(listeners);
            }
            for (ChangeListener listen : lists) {
                listen.stateChanged(new ChangeEvent(this));
            }
        }
    }
    
}
