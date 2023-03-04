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

package org.netbeans.modules.maven.classpath;

import java.awt.Image;
import org.netbeans.modules.maven.api.FileUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import static org.netbeans.modules.maven.classpath.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Implementation of Sources interface for maven projects.
 * generic and java are necessary for proper workings of the project, the rest is custom thing..
 * IMHO at least..
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service={Sources.class, SourceGroupModifierImplementation.class, OtherSourcesExclude.class}, projectType="org-netbeans-modules-maven")
public class MavenSourcesImpl implements Sources, SourceGroupModifierImplementation, OtherSourcesExclude {
    public static final String TYPE_OTHER = "Resources"; //NOI18N
    public static final String TYPE_TEST_OTHER = "TestResources"; //NOI18N
    public static final String TYPE_GEN_SOURCES = "GeneratedSources"; //NOI18N
    public static final String NAME_PROJECTROOT = "ProjectRoot"; //NOI18N
    public static final String NAME_XDOCS = "XDocs"; //NOI18N
    public static final String NAME_SOURCE = "1SourceRoot"; //NOI18N
    public static final String NAME_TESTSOURCE = "2TestSourceRoot"; //NOI18N
    public static final String NAME_GENERATED_SOURCE = "6GeneratedSourceRoot"; //NOI18N
    public static final String NAME_GENERATED_TEST_SOURCE = "7GeneratedSourceRoot"; //NOI18N
    private static final @StaticResource String OTHERS_BADGE = "org/netbeans/modules/maven/others-badge.png";
    
    
    private final Project proj;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener pcl = new PropertyChangeListener() {
        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (proj.getLookup().lookup(NbMavenProject.class).isUnloadable()) {
                return; //let's just continue with the old value, stripping classpath for broken project and re-creating it later serves no greater good.
            }
             //explicitly listing both RESOURCE and PROJECT properties, it's unclear if both are required but since some other places call addWatchedPath but don't listen it's likely required
            if (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) || NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                checkChanges(true, true);
            }
        }
    };
    
    private final Map<String, SourceGroup> javaGroup;
    private final Map<File, SourceGroup> genSrcGroup;
    private final Map<File, OtherGroup> otherMainGroups;
    private final Map<File, OtherGroup> otherTestGroups;

    
    private final Object lock = new Object();
    
    public MavenSourcesImpl(Project proj) {
        this.proj = proj;
        javaGroup = new TreeMap<String, SourceGroup>();
        genSrcGroup = new TreeMap<File, SourceGroup>();
        otherMainGroups = new TreeMap<File, OtherGroup>();
        otherTestGroups = new TreeMap<File, OtherGroup>();
    }
    
    private NbMavenProjectImpl project() {
        return proj.getLookup().lookup(NbMavenProjectImpl.class);
    }

    @Messages({
        "SG_Sources=Source Packages",
        "SG_Test_Sources=Test Packages"
    })
    private void checkChanges(boolean fireChanges, boolean checkAlsoNonJavaStuff) {        
        boolean changed = false;
        synchronized (lock) {
            NbMavenProjectImpl project = project();
            MavenProject mp = project.getOriginalMavenProject();
            NbMavenProject watcher = project.getProjectWatcher();
            File folder = FileUtilities.convertStringToFile(mp.getBuild().getSourceDirectory());
            changed = changed | checkSourceGroupCache(folder, NAME_SOURCE, SG_Sources(), javaGroup, watcher);
            folder = FileUtilities.convertStringToFile(mp.getBuild().getTestSourceDirectory());
            changed = changed | checkSourceGroupCache(folder, NAME_TESTSOURCE, SG_Test_Sources(), javaGroup, watcher);
            changed = changed | checkGeneratedGroupsCache();
            if (checkAlsoNonJavaStuff) {
                changed = changed | checkOtherGroupsCache(project.getOtherRoots(false), false);
                changed = changed | checkOtherGroupsCache(project.getOtherRoots(true), true);
            }
        }
        if (changed) {
            if (fireChanges) {
                cs.fireChange();
            }
        }
    }

    public @Override void addChangeListener(ChangeListener changeListener) {
        if (!cs.hasListeners()) {
            NbMavenProject.addPropertyChangeListener(project(), pcl);
        }
        cs.addChangeListener(changeListener);
    }
    
    public @Override void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
        if (!cs.hasListeners()) {
            NbMavenProject.removePropertyChangeListener(project(), pcl);
        }
    }
    
    public @Override SourceGroup[] getSourceGroups(String str) {
        if (Sources.TYPE_GENERIC.equals(str)) {
            return new SourceGroup[] { GenericSources.group(proj, proj.getProjectDirectory(), NAME_PROJECTROOT, 
                    ProjectUtils.getInformation(proj).getDisplayName(), null, null) };
        }
        if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(str)) {
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                // don't fire event at all..
                checkChanges(false, false);
                toReturn.addAll(javaGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_GEN_SOURCES.equals(str)) {
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            synchronized (lock) {
                checkGeneratedGroupsCache();
                toReturn.addAll(genSrcGroup.values());
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (TYPE_OTHER.equals(str) || TYPE_TEST_OTHER.equals(str)) {
            // TODO not all these are probably resources.. maybe need to split in 2 groups..
            boolean test = TYPE_TEST_OTHER.equals(str);
            List<SourceGroup> toReturn = new ArrayList<SourceGroup>();
            File[] roots = project().getOtherRoots(test);
            synchronized (lock) {
                // don't fire event synchronously..
                checkOtherGroupsCache(roots, test);
                if (test && !otherTestGroups.isEmpty()) {
                    toReturn.addAll(otherTestGroups.values());
                } else if (!test && !otherMainGroups.isEmpty()) {
                    toReturn.addAll(otherMainGroups.values());
                }
            }
            SourceGroup[] grp = new SourceGroup[toReturn.size()];
            grp = toReturn.toArray(grp);
            return grp;
        }
        if (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(str)) {
            return getOrCreateResourceSourceGroup(false, false);
        }
//        logger.warn("unknown source type=" + str);
        return new SourceGroup[0];
    }

    @Messages("SG_Project_Resources=Project Resources")
    private SourceGroup[] getOrCreateResourceSourceGroup(boolean test, boolean create) {
        URI[] uris = project().getResources(test);
        if (uris.length > 0) {
            List<URI> virtuals = new ArrayList<URI>();
            List<SourceGroup> existing = new ArrayList<SourceGroup>();
            for (URI u : uris) {
                FileObject fo = FileUtilities.convertURItoFileObject(u);
                if (fo == null) {
                    virtuals.add(u);
                } else if (fo.isFolder()) {
                    existing.add(GenericSources.group(proj, fo, "resources",  //NOI18N
                        SG_Project_Resources(), null, null));
                }
            }
            if (create && existing.isEmpty()) {
                File root = Utilities.toFile(virtuals.get(0));
                try {
                    FileObject fo = FileUtil.createFolder(root);
                    existing.add(GenericSources.group(proj, fo, "resources",  //NOI18N
                            SG_Project_Resources(), null, null));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }
            //TODO we should probably add includes/excludes to source groups.
            return existing.toArray(new SourceGroup[0]);
        } else {
            //TODO add <Resources> element to pom??
        }
        return new SourceGroup[0];
    }
    

    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    private boolean checkSourceGroupCache(@NullAllowed File rootF, String name, String displayName, Map<String, SourceGroup> groups, NbMavenProject watcher) {
        FileObject root;
        if (rootF != null) {
            watcher.addWatchedPath(Utilities.toURI(rootF));
            root = FileUtil.toFileObject(rootF);
        } else {
            root = null;
        }
        SourceGroup group = groups.get(name);
        if ((root == null || root.isData()) && group != null) {
            groups.remove(name);
            return true;
        }
        if (root == null || root.isData()) {
            return false;
        }
        boolean changed = false;
        if (group == null) {
            group = GenericSources.group(proj, root, name, displayName, null, null);
            groups.put(name, group);
            changed = true;
        } else {
            if (!group.getRootFolder().equals(root)) {
                group = GenericSources.group(proj, root, name, displayName, null, null);
                groups.put(name, group);
                changed = true;
            }
        }
        return changed;
    }


    private boolean checkGeneratedGroupsCache() {
        boolean changed = false;
        List<File> checked = new ArrayList<File>();
        for (boolean test : new boolean[] {false, true}) {
            for (URI u : project().getGeneratedSourceRoots(test)) {
                File file = FileUtil.normalizeFile(Utilities.toFile(u));
                FileObject folder = FileUtil.toFileObject(file);
                changed |= checkGeneratedGroupCache(folder, file, file.getName(), test);
                checked.add(file);
            }
        }
        Set<File> currs = new HashSet<File>();
        currs.addAll(genSrcGroup.keySet());
        for (File curr : currs) {
            if (!checked.contains(curr)) {
                genSrcGroup.remove(curr);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * consult the SourceGroup cache, return true if anything changed..
     */
    @Messages({
        "# {0} - name suffix", "SG_Generated_Sources=Generated Sources ({0})",
        "# {0} - name suffix", "SG_Generated_Test_Sources=Generated Test Sources ({0})"
    })
    private boolean checkGeneratedGroupCache(FileObject root, File rootFile, String nameSuffix, boolean test) {
        SourceGroup group = genSrcGroup.get(rootFile);
        if ((root == null || root.isData()) && group != null) {
            genSrcGroup.remove(rootFile);
            return true;
        }
        if (root == null || root.isData()) {
            return false;
        }
        boolean changed = false;
        String name = (test ? NAME_GENERATED_TEST_SOURCE : NAME_GENERATED_SOURCE) + nameSuffix;
        String displayName = test ? SG_Generated_Test_Sources(nameSuffix) : SG_Generated_Sources(nameSuffix);
        if (group == null) {
            group = new GeneratedGroup(project(), root, name, displayName);
            genSrcGroup.put(rootFile, group);
            changed = true;
        } else {
            if (!group.getRootFolder().isValid() || !group.getRootFolder().equals(root)) {
                group = new GeneratedGroup(project(), root, name, displayName);
                genSrcGroup.put(rootFile, group);
                changed = true;
            }
        }
        return changed;
    }

    private boolean checkOtherGroupsCache(File[] roots, boolean test) {
        boolean ch = false;
        Set<File> toRemove = new HashSet<File>(test ? otherTestGroups.keySet() : otherMainGroups.keySet());
        toRemove.removeAll(Arrays.asList(roots));

        URI[] res = project().getResources(test);
        Set<File> resources = new HashSet<File>();
        for (URI ur : res) {
            resources.add(Utilities.toFile(ur));
        }

        for (File f : roots) {
            ch = ch | checkOtherGroup(f, resources, test);
        }
        for (File f : toRemove) {
            //now this shall remove the nonexisting ones and even mark the change..
            ch = ch | checkOtherGroup(f, resources, test);
        }
        return ch;
    }

    private boolean checkOtherGroup(File rootFile, Set<File> resourceRoots, boolean test) {
        FileObject root = FileUtil.toFileObject(rootFile);
        if (root != null && !root.isFolder()) {
            root = null;
        }
        Map<File, OtherGroup> map = test ? otherTestGroups : otherMainGroups;
        OtherGroup grp = map.get(rootFile);
        boolean isResourceNow = resourceRoots.contains(rootFile);
        boolean wasResourceBefore = grp != null && grp.getResource() != null;
        if ((root == null && grp != null) ||  (root != null && grp != null && wasResourceBefore && !isResourceNow)) {
            map.remove(rootFile);
            return true;
        }
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (grp == null || !grp.getRootFolder().isValid() || !grp.getRootFolder().equals(root) ||
                isResourceNow != wasResourceBefore) {
            grp = new OtherGroup(project(), root, "Resource" + (test ? "Test":"Main") + root.getNameExt(), root.getName(), test); //NOI18N
            map.put(rootFile, grp);
            changed = true;
        }
        return changed;
    }

    public @Override SourceGroup createSourceGroup(String type, String hint) {
        assert type != null;
        MavenProject mp = project().getOriginalMavenProject();
        File folder = null;
        if (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(type)) {
            boolean main = JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint);
            SourceGroup[] grps = getOrCreateResourceSourceGroup(!main, true);
            if (grps.length > 0) {
                return grps[0];
            }
            return null;
        }
        if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type)) {
            if (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint)) {
                folder = FileUtilities.convertStringToFile(mp.getBuild().getSourceDirectory());
            }
            if (JavaProjectConstants.SOURCES_HINT_TEST.equals(hint)) {
                folder = FileUtilities.convertStringToFile(mp.getBuild().getTestSourceDirectory());
            }
        }
        if (folder != null) {
            FileObject fo;
            try {
                fo = FileUtil.createFolder(folder);
            } catch (IOException x) { // XXX not allowed to rethrow
                Logger.getLogger(MavenSourcesImpl.class.getName()).log(Level.INFO, null, x);
                return null;
            }
            SourceGroup[] grps = getSourceGroups(type);
            for (SourceGroup sg : grps) {
                if (fo.equals(sg.getRootFolder())) {
                    return sg;
                }
            }
            //shall we somehow report it?
        }

        return null;
    }

    public @Override boolean canCreateSourceGroup(String type, String hint) {
        return   (JavaProjectConstants.SOURCES_TYPE_RESOURCES.equals(type) ||
                  JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type))
              && (JavaProjectConstants.SOURCES_HINT_MAIN.equals(hint) ||
                  JavaProjectConstants.SOURCES_HINT_TEST.equals(hint));
    }

    @Override
    public Set<Path> excludedFolders() {
        Set<Path> result = new HashSet<>();
        FileObject mainDir = project().getProjectDirectory().getFileObject("src/main/java");
        FileObject testDir = project().getProjectDirectory().getFileObject("src/test/java");

        if (mainDir != null) {
            result.add(FileUtil.toFile(mainDir).toPath());
        }
        if (testDir != null) {
            result.add(FileUtil.toFile(testDir).toPath());
        }
        return result;
    }

    
    public static final class OtherGroup implements SourceGroup {
        
        private final FileObject rootFolder;
        private final File rootFile;
        private final String name;
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        private final NbMavenProjectImpl project;
        private final Resource resource;
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        @Messages("SG_Root_not_defined=<Root not defined>")
        OtherGroup(NbMavenProjectImpl p, FileObject rootFold, String nm, String displayNm, boolean test) {
            project = p;
            rootFolder = rootFold;
            rootFile = FileUtil.toFile(rootFolder);
            resource = checkResource(rootFold, 
                    test ? project.getOriginalMavenProject().getTestResources() :
                           project.getOriginalMavenProject().getResources());
            if (resource != null) {
                Image badge = ImageUtilities.loadImage(OTHERS_BADGE, true); //NOI18N
//                ImageUtilities.addToolTipToImage(badge, "Resource root as defined in POM.");
                icon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(false), badge, 8, 8));
                openedIcon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(true), badge, 8, 8));
                name = FileUtilities.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), FileUtilities.convertStringToFile(resource.getDirectory()));
                displayName = name;
            } else {
                icon = ImageUtilities.image2Icon(NodeUtils.getTreeFolderIcon(false));
                openedIcon = ImageUtilities.image2Icon(NodeUtils.getTreeFolderIcon(true));
                name = nm;
                displayName = displayNm != null ? displayNm : SG_Root_not_defined();
            }
        }
        
        public @Override FileObject getRootFolder() {
            return rootFolder;
        }
        
        public File getRootFolderFile() {
            return rootFile;
        }

        public Resource getResource() {
            return resource;
        }
        
        public @Override String getName() {
            return name;
        }
        
        public @Override String getDisplayName() {
            if (resource != null && resource.getTargetPath() != null) {
                return displayName + " -> " + resource.getTargetPath();
            }
            return displayName;
        }
        
        public @Override Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public @Override boolean contains(FileObject file)  {
             if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                return false;
            }
            if (project != null) {
                if (file.isFolder() && file != project.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                if (FileOwnerQuery.getOwner(file) != project) {
                    return false;
                }
            }
            File f = FileUtil.toFile(file);
            if (f != null) {
                // MIXED, UNKNOWN, and SHARABLE -> include it
                return SharabilityQuery.getSharability(f) != SharabilityQuery.NOT_SHARABLE;
            } else {
                // Not on disk, include it.
                return true;
            }

        }
        
        public @Override void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }
        
        public @Override void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }

        private Resource checkResource(FileObject rootFold, List<Resource> list) {
            for (Resource elem : list) {
                String dir = elem.getDirectory();
                if (dir == null) { // #203635
                    continue;
                }
                URI uri = FileUtilities.getDirURI(project.getProjectDirectory(), dir);
                FileObject fo = FileUtilities.convertURItoFileObject(uri);
                if (fo != null && fo.equals(rootFold)) {
                    return elem;
                }
            }
            return null;
        }
        
    }
    
    /**
     * MEVENIDE-536 - cannot use default implementation of SourceGroup because it
     * won't include non-shareable folders..
     */ 
    public static final class GeneratedGroup implements SourceGroup {
        
        private final FileObject rootFolder;
        private final String name;
        private final String displayName;
        private final Icon icon = null;
        private final Icon openedIcon = null;
        private NbMavenProjectImpl project;
        
        GeneratedGroup(NbMavenProjectImpl p, FileObject rootFold, String nm, String displayNm/*,
                Icon icn, Icon opened*/) {
            project = p;
            rootFolder = rootFold;
            name = nm;
            displayName = displayNm != null ? displayNm : SG_Root_not_defined();
//            icon = icn;
//            openedIcon = opened;
        }
        
        public @Override FileObject getRootFolder() {
            return rootFolder;
        }
        
        public @Override String getName() {
            return name;
        }
        
        public @Override String getDisplayName() {
            return displayName;
        }
        
        public @Override Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public @Override boolean contains(FileObject file)  {
             if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                return false;
            }
            if (project != null) {
                if (file.isFolder() && file != project.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                if (FileOwnerQuery.getOwner(file) != project) {
                    return false;
                }
            }
            return true;
        }
        
        public @Override void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }
        
        public @Override void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }
        
    }
    
}
