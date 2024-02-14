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
package org.netbeans.modules.gradle.java.queries;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleDependency.ModuleDependency;
import org.netbeans.modules.gradle.api.GradleProjects;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.JavaActionProvider;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "LABEL_GradleSourceDefiner=Download using Gradle",
    "DESC_GradleSourceDefiner=Uses Gradle task of an open project to download the resources."
})
@ServiceProvider(service=SourceJavadocAttacherImplementation.Definer.class, position=100)
public class GradleSourceAttacherImpl implements SourceJavadocAttacherImplementation.Definer2 {
    /**
     * Name of the replaceable argument for {@link #COMMAND_DL_JAVADOC}} and {@link #COMMAND_DL_SOURCES}.
     */
    private static final String REQUESTED_COMPONENT = "requestedComponent";    

    /**
     * Name of the 'download.sources' standard action
     */
    private static final String COMMAND_DL_SOURCES = JavaActionProvider.COMMAND_DL_SOURCES;

    /**
     * Name of the 'download.javadoc' standard action
     */
    private static final String COMMAND_DL_JAVADOC = JavaActionProvider.COMMAND_DL_JAVADOC;

    /**
     * Caches last queried URL. The workflow is to filter Definers that work with the given URL
     * first, then actually performs the download.
     */
    private volatile Reference<Cached>   lastChecked = new WeakReference<>(null);
    
    @Override
    public boolean accepts(URL root) {
        return cachedRootInfo(root) != null;
    }

    @Override
    public String getDisplayName() {
        return Bundle.LABEL_GradleSourceDefiner();
    }

    @Override
    public String getDescription() {
        return Bundle.DESC_GradleSourceDefiner();
    }
    
    @Override
    public List<? extends URL> getSources(URL root, Callable<Boolean> cancel) {
        return new Worker(root, true, cancel).download();
    }

    @Override
    public List<? extends URL> getJavadoc(URL root, Callable<Boolean> cancel) {
        return new Worker(root, false, cancel).download();
    }
    
    private File findGradleCachedArtifact(URL r) {
        if (!FileUtil.isArchiveArtifact(r)) {
            // support JARs only
            return null;
        }
        URL archive = FileUtil.getArchiveFile(r);
        if (archive == null) {
            return null;
        }
        FileObject fo = URLMapper.findFileObject(archive);
        if (fo == null) {
            return null;
        }
        File maybeArtifact = FileUtil.toFile(fo);
        return GradleProjects.isGradleCacheArtifact(maybeArtifact) ? maybeArtifact : null;
    }
    
    private static class E {
        final Project  project;
        final ModuleDependency dep;

        public E(Project project, ModuleDependency dep) {
            this.project = project;
            this.dep = dep;
        }
    }
    
    private static class Cached {
        final URL u;
        final E[] projectsAndModules;

        public Cached(URL u, E[] projectsAndModules) {
            this.u = u;
            this.projectsAndModules = projectsAndModules;
        }
    }
    
    private E[] findOwnerProjects(URL u, File f) {
        Cached c = lastChecked.get();
        if (c != null && 
                u.toString().equals(c.u.toString())) {
            return c.projectsAndModules;
        }
        List<E> fallbacks = new ArrayList<>();
        List<E> result = new ArrayList<>();
        for (Project candidate : OpenProjects.getDefault().getOpenProjects()) {
            /*
            boolean trusted = ProjectTrust.getDefault().isTrusted(candidate);
            */
            GradleBaseProject gbp = GradleBaseProject.get(candidate);
            if (gbp == null) {
                continue;
            }
            boolean resolved = gbp.isResolved();
            ModuleDependency dep = checkGradleContains(candidate, f);
            if (dep != null) {
                (resolved ? result : fallbacks).add(new E(candidate, dep));
            }
        }
        result.addAll(fallbacks);
        c = new Cached(u, result.toArray(new E[0]));
        lastChecked = new WeakReference<>(c);
        return c.projectsAndModules;
    }
    
    private ModuleDependency checkGradleContains(Project p, File f) {
        GradleBaseProject gbp = GradleBaseProject.get(p);
        if (p == null) {
            return null;
        }
        
        // project that does not offer to download is useless...
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            return null;
        }
        
        Collection<String> s = Arrays.asList(ap.getSupportedActions());
        if (!(s.contains(COMMAND_DL_JAVADOC) || s.contains(COMMAND_DL_SOURCES))) {
            return null;
        }

        File absF = f.getAbsoluteFile();
        Stream<ModuleDependency> mods = gbp.getConfigurations().values().stream().
                    flatMap(cfg -> cfg.getModules().stream());
        return mods.filter(m -> m.getArtifacts().contains(absF)).findFirst().orElse(null);
    }

    E[] cachedRootInfo(URL u) {
        Cached c = lastChecked.get();
        if (c != null && u.toString().equals(c.u.toString())) {
            return c.projectsAndModules;
        } else {
            File a = findGradleCachedArtifact(u);
            if (a == null) {
                return null;
            }
            E[] result = findOwnerProjects(u, a);
            return result != null && result.length > 0 ? result : null;
        }
    }
    
    class Worker extends  ActionProgress implements Runnable {
        private final URL root;
        private final E[] locations;
        private final Callable<Boolean> cancel;
        private final boolean sources;
        private final Semaphore sync = new Semaphore(0);
        private final AttachmentListener listener;
        private volatile Boolean result;

        public Worker(URL root, boolean sources, Callable<Boolean> cancel) {
            this.root = root;
            this.sources = sources;
            this.cancel = cancel;
            this.listener = null;
            this.locations = cachedRootInfo(root);
        }
        
        public Worker(URL root, boolean sources, E[] locations, AttachmentListener listener) {
            this.root = root;
            this.sources = sources;
            this.cancel = () -> false; // not supported in this mode
            this.locations = locations;
            this.listener = listener;
        }

        @Override
        public void run() {
            download();
        }
        
        List<? extends URL> download() {
            boolean ok = false;
            try {
                List<? extends URL> ret = download0();
                ok = ret != null && !ret.isEmpty();
                return ret;
            } finally {
                if (listener != null) {
                    if (ok) {
                        listener.attachmentSucceeded();
                    } else {
                        listener.attachmentFailed();
                    }
                }
            }
        }
            
        List<? extends URL> download0() {
            if (locations == null) {
                return Collections.emptyList();
            }
            for (E l : locations) {
                try {
                    if (cancel.call()) {
                        return Collections.emptyList();
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return Collections.emptyList();
                }
                if (invokeWaitDownloadAction(l.project.getLookup().lookup(ActionProvider.class), 
                        sources ? COMMAND_DL_SOURCES : COMMAND_DL_JAVADOC, 
                        l.dep)
                    ) {
                    // now should be reachable by SFBQ:
                    FileObject[] fobs = SourceForBinaryQuery.findSourceRoots(root).getRoots();
                    if (fobs.length != 0) {
                        List<URL> res = new ArrayList<>();
                        for (FileObject ff : fobs) {
                            URL u = URLMapper.findURL(ff, URLMapper.INTERNAL);
                            if (u != null) {
                                res.add(u);
                            }
                        }
                        if (!res.isEmpty()) {
                            return res;
                        }
                    }
                }
            }
            return Collections.emptyList();
        }
        
        boolean invokeWaitDownloadAction(ActionProvider ap, String command, ModuleDependency dep) {
            if (ap == null /* || !Arrays.asList(ap.getSupportedActions()).contains(command) */) {
                return false;
            }
            // assume the action is invoked asynchronously.
            try {
                Lookup ctx = Lookups.fixed(
                                this,
                                RunUtils.simpleReplaceTokenProvider(REQUESTED_COMPONENT, dep.getId())
                );
                if (!ap.isActionEnabled(command, ctx)) {
                    // disabled action will not even report action end through the Listener.
                    return false;
                }
                ap.invokeAction(command, ctx);
            } catch (IllegalArgumentException ex) {
                // since we cannot test whether action exists / is enabled, catch the exception
                // if the action is really unsupported
                return false;
            }
            while (true) {
                try {
                    if (sync.tryAcquire(300, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (InterruptedException ex) {
                }
                try {
                    if (cancel.call()) {
                        // cancelled, but CANNOT cancel the project action. See NETBEANS-
                        return false;
                    }
                } catch (Exception ex1) {
                    Exceptions.printStackTrace(ex1);
                    return false;
                }
            }
            return result == Boolean.TRUE;
        }

        @Override
        protected void started() {
        }

        @Override
        public void finished(boolean success) {
            result = success;
            sync.release();
        }
    }
}
