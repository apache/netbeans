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
            return result.toArray(new URL[0]);
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
