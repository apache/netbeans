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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeEvent;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Links the Maven project with its artifact in the local repository.
 */
@ServiceProviders({@ServiceProvider(service=FileOwnerQueryImplementation.class, position=97), @ServiceProvider(service=MavenFileOwnerQueryImpl.class)})
public class MavenFileOwnerQueryImpl implements FileOwnerQueryImplementation {
    private static final String EXTERNAL_OWNERS = "externalOwners";
    
    private final PropertyChangeListener projectListener;
    private final ProjectGroupChangeListener groupListener;
    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

    private static final AtomicReference<Preferences> prefs = new AtomicReference<>(NbPreferences.forModule(MavenFileOwnerQueryImpl.class).node(EXTERNAL_OWNERS));

    private static final Logger LOG = Logger.getLogger(MavenFileOwnerQueryImpl.class.getName());
    
    public MavenFileOwnerQueryImpl() {
        projectListener = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    if (!registerProject((NbMavenProjectImpl) evt.getSource(), true)) {
                        fireChange(new ChangeEvent(this));
                    }
                }
            }
        };
        groupListener = new ProjectGroupChangeListener() {

            @Override
            public void projectGroupChanging(ProjectGroupChangeEvent event) {
                Preferences old = prefs();
                Preferences n = event.getNewGroup() != null 
                        ? event.getNewGroup().preferencesForPackage(MavenFileOwnerQueryImpl.class).node(EXTERNAL_OWNERS)
                        : NbPreferences.forModule(MavenFileOwnerQueryImpl.class).node(EXTERNAL_OWNERS);
                prefs.compareAndSet(old, n);
            }
    
            @Override
            public void projectGroupChanged(ProjectGroupChangeEvent event) {
                //TODO should we check what projects were kept open and register them with current group?
                //some were already registered when projectOpenHook was triggered, 
                //but some might have been kept opened from previous group
                for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
                    NbMavenProjectImpl mp = prj.getLookup().lookup(NbMavenProjectImpl.class);
                    if (mp != null) {
                        registerProject(mp, false);
                    }
                }
                fireChange(new ChangeEvent(this)); //optimization, just one change gets fired.
            }
        };
        ProjectGroup pg = OpenProjects.getDefault().getActiveProjectGroup();
        //initial value is non-group setting but we need to check if a group is active
        if (pg != null) {
            Preferences old = prefs();
            prefs.compareAndSet(old, pg.preferencesForPackage(MavenFileOwnerQueryImpl.class).node(EXTERNAL_OWNERS));
        }
        //not worth making weak, both are singletons kept forever
        OpenProjects.getDefault().addProjectGroupChangeListener(groupListener);
    }
    
    public static MavenFileOwnerQueryImpl getInstance() {
        return Lookup.getDefault().lookup(MavenFileOwnerQueryImpl.class);
    }
    
    public void attachProjectListener(NbMavenProjectImpl project) {
        project.getProjectWatcher().removePropertyChangeListener(projectListener);
        project.getProjectWatcher().addPropertyChangeListener(projectListener);       
    }

    public static String cacheKey(String groupId, String artifactId, String version) {
        return groupId + ':' + artifactId + ":" + version;
    }

    public synchronized void registerCoordinates(String groupId, String artifactId, String version, URL owner, boolean fire) {
        String oldkey = groupId + ':' + artifactId;
        //remove old key if pointing to the same project
        if (owner.toString().equals(prefs().get(oldkey, null))) {
            prefs().remove(oldkey);
        }
        
        String key = cacheKey(groupId, artifactId, version);
        String ownerString = owner.toString();

        String oldString = prefs().get(key, null);
        if (oldString != null && !oldString.equals(ownerString)) {
            // avoid replacing gav to an opened project with gav to a closed project. Closed projects continue to listen and sometimes register themselves
            // - this is to avoid replacing a "live, opened" project registration with a closed project registration, if they compete for the same artifact id.
            Set<String> opened = Arrays.stream(OpenProjects.getDefault().getOpenProjects()).
                    map(p -> p.getProjectDirectory().toURI().toString()).collect(Collectors.toSet());
            if (opened.contains(oldString) && !opened.contains(ownerString)) {
                LOG.log(Level.FINE, "NOT replacing {0} with {1} under {2}, old owner is opened while the new is not.", new Object[] {oldString, owner, key});
                return;
            }
        }

        try {
            for (String k : prefs().keys()) {
                String value;
                try {
                    value = prefs().get(k, null);
                } catch (IllegalArgumentException ex) {
                     // e.g invalid code point JDK-8075156
                    LOG.log(Level.WARNING, "Invalid prefrences key at {0}, msg: {1}", new Object[] { prefs().absolutePath(), ex.getMessage() });
                    continue;
                }
                if (ownerString.equals(value)) {
                    prefs().remove(k);
                    break;
                }
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.FINE, "Error iterating preference to find old mapping", ex);
        }

        prefs().put(key, ownerString);
        LOG.log(Level.FINE, "Registering {0} under {1}", new Object[] {owner, key});
        if (fire) {
            fireChange(new GAVCHangeEvent(this, groupId, artifactId, version));
        }
        
    }
    
    /**
     * 
     * @param project
     * @return true if project was registered, false otherwise 
     */
    public boolean registerProject(NbMavenProjectImpl project, boolean fire) {
        MavenProject model = project.getOriginalMavenProject();
        attachProjectListener(project);
        if (NbMavenProject.isErrorPlaceholder(model)) {
            LOG.log(Level.FINE, "will not register unloadable {0}", project.getPOMFile());
            //TODO we should remove the project's mapping in this case and wait for it to reappear loadable again
            return false;
        }
        try {
            registerCoordinates(model.getGroupId(), model.getArtifactId(), model.getVersion(), Utilities.toURI(project.getPOMFile().getParentFile()).toURL(), fire);
        } catch (MalformedURLException ex) {
        }
        return true;
    }
    
    public void addChangeListener(ChangeListener list) {
        listeners.add(list);
    }
    
    public void removeChangeListener(ChangeListener list) {
        listeners.remove(list);
    }
    
    private void fireChange(ChangeEvent event) {
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }
    
    public @Override Project getOwner(URI uri) {
        LOG.log(Level.FINEST, "getOwner of uri={0}", uri);
        if ("file".equals(uri.getScheme())) { //NOI18N
            File file = Utilities.toFile(uri);
            return getOwner(file);
        }
        return null;
    }
    
    public @Override Project getOwner(FileObject fileObject) {
        LOG.log(Level.FINEST, "getOwner of fileobject={0}", fileObject);
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            return getOwner(file);
        }
        return null;
    }

    /**
     * Utility method to identify a file which might be an artifact in the local repository.
     * @param file a putative artifact
     * @return its coordinates (groupId/artifactId/version), or null if it cannot be identified
     */
    static @CheckForNull String[] findCoordinates(File file) {
        String nm = file.getName(); // commons-math-2.1.jar
        File parentVer = file.getParentFile(); // ~/.m2/repository/org/apache/commons/commons-math/2.1
        if (parentVer != null) {
            File parentArt = parentVer.getParentFile(); // ~/.m2/repository/org/apache/commons/commons-math
            if (parentArt != null) {
                String artifactID = parentArt.getName(); // commons-math
                String version = parentVer.getName(); // 2.1
                if (nm.startsWith(artifactID + '-' + version)) {
                    File parentGroup = parentArt.getParentFile(); // ~/.m2/repository/org/apache/commons
                    if (parentGroup != null) {
                        // Split rest into separate method, to avoid linking EmbedderFactory unless and until needed.
                        return findCoordinates(parentGroup, artifactID, version);
                    }
                }
            }
        }
        return null;
    }
    private static @CheckForNull String[] findCoordinates(File parentGroup, String artifactID, String version) {
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile(); // ~/.m2/repository
        String repoS = repo.getAbsolutePath();
        if (!repoS.endsWith(File.separator)) {
            repoS += File.separatorChar; // ~/.m2/repository/
        }
        String parentGroupS = parentGroup.getAbsolutePath();
        if (parentGroupS.endsWith(File.separator)) {
            parentGroupS = parentGroupS.substring(0, parentGroupS.length() - 1);
        }
        if (parentGroupS.startsWith(repoS)) {
            String groupID = parentGroupS.substring(repoS.length()).replace(File.separatorChar, '.'); // org.apache.commons
            return new String[] {groupID, artifactID, version};
        } else {
            return null;
        }
    }

    private Project getOwner(File file) {
        //#223841 at least one project opened is a stronger condition, embedder gets sometimes reset.
        //once we have the project loaded, not loaded embedder doesn't matter anymore, we have to process.
        // sometimes the embedder is loaded even though a maven project is not yet loaded, it doesn't hurt to proceed then.
        if (!NbMavenProjectFactory.isAtLeastOneMavenProjectAround() && !EmbedderFactory.isProjectEmbedderLoaded()) { 
            return null;
        }

        LOG.log(Level.FINER, "Looking for owner of {0}", file);
        String[] coordinates = findCoordinates(file);
        if (coordinates == null) {
            LOG.log(Level.FINER, "{0} not an artifact in local repo", file);
            return null;
        }
        return getOwner(coordinates[0], coordinates[1], coordinates[2]);
    }

    public Project getOwner(String groupId, String artifactId, String version) {
        LOG.log(Level.FINER, "Checking {0} / {1} / {2}", new Object[] {groupId, artifactId, version});
        String oldKey = groupId + ":" + artifactId;
        String key = cacheKey(groupId, artifactId, version);
        String ownerURI = prefs().get(key, null);
        boolean usingOldKey = false;
        if (ownerURI == null) {
            ownerURI = prefs().get(oldKey, null);
            usingOldKey = true;
        }        
        if (ownerURI != null) {
            boolean stale = true;
            try {
                FileObject projectDir = URLMapper.findFileObject(new URI(ownerURI).toURL());
                if (projectDir != null && projectDir.isFolder()) {
                    Project p = ProjectManager.getDefault().findProject(projectDir);
                    if (p != null) {
                        NbMavenProjectImpl mp = p.getLookup().lookup(NbMavenProjectImpl.class);
                        if (mp != null) {
                            MavenProject model = mp.getOriginalMavenProject();
                            if (model.getGroupId().equals(groupId) && model.getArtifactId().equals(artifactId)) {
                                if (model.getVersion().equals(version)) {
                                    LOG.log(Level.FINE, "Found match {0}", p);
                                    //some projects get registered only via coordinates and we never listen on their changes,
                                    //do it now, when we know the project is loaded
                                    //since we match on GAV not GA now, it's more important to have changes in projects reflected in FOQ
                                    attachProjectListener(mp);
                                    return p;
                                } else {
                                    LOG.log(Level.FINE, "Mismatch on version {0} in {1}", new Object[] {model.getVersion(), ownerURI});
                                    stale = false; // we merely remembered another version
                                    registerProject(mp, true);
                                }
                            } else {
                                LOG.log(Level.FINE, "Mismatch on group and/or artifact ID in {0}", ownerURI);
                                registerProject(mp, true);
                            }
                        } else {
                            LOG.log(Level.FINE, "Not a Maven project {0} in {1}", new Object[] {p, ownerURI});
                        }
                    } else {
                        LOG.log(Level.FINE, "No such project in {0}", ownerURI);
                    }
                } else {
                    LOG.log(Level.FINE, "No such folder {0}", ownerURI);
                }
            } catch (IOException x) {
                LOG.log(Level.FINE, "Could not load project in " + ownerURI, x);
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
            if (stale) {
                if (usingOldKey) {
                    prefs().remove(oldKey);
                } else {
                    prefs().remove(key); // stale    
                }
            }
        } else {
            LOG.log(Level.FINE, "No known owner for {0}", key);
        }
        return null;
    }

    
    //NOTE: called from NBArtifactFixer, cannot contain references to ProjectManager
    public File getOwnerPOM(String groupId, String artifactId, String version) {
        LOG.log(Level.FINER, "Checking {0} / {1} / {2} (POM only)", new Object[] {groupId, artifactId, version});
        String oldKey = groupId + ":" + artifactId;
        String key = cacheKey(groupId, artifactId, version);
        String ownerURI = prefs().get(key, null);
        boolean usingOldKey = false;
        if (ownerURI == null) {
            ownerURI = prefs().get(oldKey, null);
            usingOldKey = true;
        }
        if (ownerURI != null) {
            try {
                URI uri = new URI(ownerURI);
                if ("file".equals(uri.getScheme())) {
                    File pom = Utilities.toFile(uri.resolve("pom.xml"));
                    if (pom.isFile()) {
                        ModelReader reader = EmbedderFactory.getProjectEmbedder().lookupComponent(ModelReader.class);
                        Model model = reader.read(pom, Collections.singletonMap(ModelReader.IS_STRICT, false));
                        Parent parent = model.getParent();
                        if (groupId.equals(model.getGroupId()) || (parent != null && groupId.equals(parent.getGroupId()))) {
                            if (artifactId.equals(model.getArtifactId()) || (parent != null && artifactId.equals(parent.getArtifactId()))) {
                                if (version.equals(model.getVersion()) || (parent != null && version.equals(parent.getVersion()))) {
                                    LOG.log(Level.FINE, "found match {0}", pom);
                                    return pom;
                                } else {
                                    LOG.log(Level.FINE, "mismatch on version in {0}", pom);
                                }
                            } else {
                                LOG.log(Level.FINE, "mismatch on artifactId in {0}", pom);
                            }
                        } else {
                            LOG.log(Level.FINE, "mismatch on groupId in {0}", pom);
                        }
                        // Might actually be a match due to use of e.g. string interpolation, so double-check with live project.
                        FileObject projectDir = URLMapper.findFileObject(new URI(ownerURI).toURL());
                        if (projectDir != null && projectDir.isFolder()) {
                            File pomFile = new File(FileUtil.toFile(projectDir), "pom.xml");
                            //TODO the file instance will not be the same instance passed by project. how does weakhashmap behave in such a case?
                            MavenProject prj = MavenProjectCache.getMavenProject(pomFile, false);
                            if (prj != null && prj.getGroupId().equals(groupId) && prj.getArtifactId().equals(artifactId) && prj.getVersion().equals(version)) {
                                return pom;
                            }
                        } else {
                            LOG.log(Level.FINE, "no live project match for {0}", pom);
                        }
                    } else {
                        LOG.log(Level.FINE, "no such file {0}", pom);
                    }
                } else {
                    LOG.log(Level.FINE, "not a file URI {0}", uri);
                }
            } catch (IOException x) {
                LOG.log(Level.FINE, "Could not load project in " + ownerURI, x);
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
            //not found match results in prefs cleanup.
            if (usingOldKey) {
                prefs().remove(oldKey);
            } else {
                prefs().remove(key); // stale    
            }          
        } else {
            LOG.log(Level.FINE, "No known owner for {0}", key);
        }
        return null;
    }

    static Preferences prefs() {
        return prefs.get();
    }
    
    /**
     * in some situations this ChangeEvent subclass can be fired, allowing listeners
     * to optimize response based on GAV affected.
     */
    public static class GAVCHangeEvent extends ChangeEvent {
        private final String groupId;
        private final String version;
        private final String artifactId;

        public GAVCHangeEvent(@NonNull Object source, @NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
            super(source);
            this.groupId = groupId;
            this.version = version;
            this.artifactId = artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getVersion() {
            return version;
        }

        public String getArtifactId() {
            return artifactId;
        }
        
        public boolean matches(Artifact art) {
            return groupId.equals(art.getGroupId()) && artifactId.equals(art.getArtifactId()) && version.equals(art.getVersion());
        }
        
    }
}
