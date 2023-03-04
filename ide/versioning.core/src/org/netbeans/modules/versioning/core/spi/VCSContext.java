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
package org.netbeans.modules.versioning.core.spi;

import org.netbeans.modules.versioning.core.SPIAccessor;
import java.io.File;
import org.netbeans.modules.versioning.core.Utils;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.modules.versioning.core.APIAccessor;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 * @author Tomas Stupka
 */
public final class VCSContext {
    
    /**
     * VCSContext that contains no files.
     */
    public static final VCSContext EMPTY = new VCSContext((Node[]) null, emptySet(), emptySet() );
    
    private static final Logger LOG = Logger.getLogger(VCSContext.class.getName());

    /**
     * Caching of current context for performance reasons, also see #72006.
     */
    private static Reference<VCSContext>  contextCached = new WeakReference<VCSContext>(null);    
    private static Reference<Node[]> contextNodesCached = new WeakReference<Node []>(null); 

    private final Lookup    elements;
    
    private final Set<VCSFileProxy> unfilteredRootFiles;
    private final Set<VCSFileProxy> rootFiles;
    private final Set<VCSFileProxy> exclusions;

    private Set<VCSFileProxy>       computedFilesCached;
    private FileFilter      fileFilterCached;

    /**
     * A {@link VCSFileProxy} analogy to {@link java.io.FileFilter}
     */
    public interface FileFilter {
        boolean accept(VCSFileProxy file);
    }
    
    static {
        SPIAccessor.IMPL = new SPIAccessorImpl();
    }
    
    /**
     * Constructs a VCSContext out of a set of files. These files are later available via getRootFiles().
     * 
     * @param rootFiles set of Files
     * @param originalFiles set of original files for which the context shall be created
     * @return VCSContext a context representing supplied set of Files
     */ 
    static VCSContext forFiles(Set<VCSFileProxy> rootFiles, Set<? extends FileObject> originalFiles) {
        return new VCSContext(originalFiles, rootFiles, emptySet());
    }

    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become root files of this context and
     * exclusions list is constructed using sourceRoot.contains() queries.
     * 
     * Nodes' lookups are examined in the following way (the first applied rule wins):
     * - if there's a File, the File is added to set of root files
     * - if there's a Project, project's source roots of type Sources.TYPE_GENERIC are added to set of root files and
     *   all direct children that do not belong to the project (sg.contains() == false) are added to set of exclusions
     * - if there's a FileObject, it is added to set of root files
     * - if there's a DataObject, all dao.files() are added to set of root files 
     * 
     * @param nodes array of Nodes
     * @return VCSContext containing nodes and corresponding files they represent
     */
    // XXX any chance to replace with lookup? 
    public static synchronized VCSContext forNodes(Node[] nodes) {
        if (Arrays.equals(contextNodesCached.get(), nodes)) {
            VCSContext ctx = contextCached.get();
            if (ctx != null) return ctx;
        }
        Set<VCSFileProxy> rootFiles = new HashSet<VCSFileProxy>(nodes.length);
        Set<SourceGroup> sourceGroups = new HashSet<SourceGroup>();
        Map<FileObject, VCSFileProxy> rootFileExclusions = new LinkedHashMap<FileObject, VCSFileProxy>(5);
        int numberOfProjects = 0;
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            File aFile = node.getLookup().lookup(File.class);
            if (aFile != null) {
                rootFiles.add(VCSFileProxy.createFileProxy(aFile));
                continue;
            }
            VCSFileProxy aFileProxy = node.getLookup().lookup(VCSFileProxy.class);
            if (aFileProxy != null) {
                rootFiles.add(aFileProxy);
                continue;
            }
            if (!Boolean.TRUE.equals(node.getValue("VCS_PHYSICAL"))) { //NOI18N
                // in the physical view (Files view), do not build the logical context
                // as in the Projects view
                Project project =  node.getLookup().lookup(Project.class);
                if (project != null) {
                    ++numberOfProjects;
                    addProjectFiles(rootFiles, rootFileExclusions, project, sourceGroups);
                    continue;
                }
            }
            addFileObjects(node, rootFiles);
        }
        if (numberOfProjects > 1) {
            // some projects - especially nested ones include and exclude their 
            // parent's source roots, so when a parent is selected, no nested project
            // should be allowed to exclude the parent's root files.
            removeContainedExclusions(rootFileExclusions, sourceGroups);
        }

        if (rootFiles.isEmpty()) {
            LOG.fine("forNodes: context contains no root files");    //NOI18N
        }
        List<VCSFileProxy> unversionedFiles = new ArrayList<VCSFileProxy>(rootFiles.size());
        Set<VersioningSystem> projectOwners = new HashSet<VersioningSystem>(2);
        for (VCSFileProxy root : rootFiles) {
            if(root != null) {
                VCSSystemProvider.VersioningSystem owner = VersioningManager.getInstance().getOwner(root);
                if (owner == null) {
                    unversionedFiles.add(root);
                } else {
                    projectOwners.add(owner);
                }
            } else {
                LOG.warning("trying to add a null root to context"); // NOI18N
            }
        }
        if(projectOwners.isEmpty()) {
            // all roots are unversioned -> keep them
        } else if(projectOwners.size() == 1) {
            // context contais one owner -> remove unversioned files
            for (VCSFileProxy unversionedFile : unversionedFiles) {
                for (Iterator<Map.Entry<FileObject, VCSFileProxy>> i = rootFileExclusions.entrySet().iterator(); i.hasNext(); ) {
                    VCSFileProxy exclusion = i.next().getValue();
                    if (Utils.isAncestorOrEqual(unversionedFile, exclusion)) {
                        i.remove();
                    }
                }
            }
            rootFiles.removeAll(unversionedFiles);
        } else {
            // more than one owner -> return empty context
            rootFileExclusions.clear();
            rootFiles.clear();
        }

        VCSContext ctx = new VCSContext(nodes, rootFiles, rootFileExclusions.values());
        contextCached = new WeakReference<VCSContext>(ctx);
        contextNodesCached = new WeakReference<Node []>(nodes);
        return ctx;
    }
        
    /**
     * Returns the smallest possible set of all files that lie under Root files and are NOT 
     * under some Excluded file. 
     * Technically, for every file in the returned set all of the following is true:
     * 
     * - the file itself or at least one of its ancestors is a root file/folder
     * - neither the file itself nor any of its ancestors is an exluded file/folder
     * - the file passed through the supplied FileFilter
     *  
     * @param filter custom file filter
     * @return filtered set of files that must pass through the filter
     */
    public synchronized Set<VCSFileProxy> computeFiles(FileFilter filter) {
        if (computedFilesCached == null || filter != fileFilterCached) {
            computedFilesCached = substract(rootFiles, exclusions, filter);
            fileFilterCached = filter;
        }
        return computedFilesCached;
    }
    
    /**
     * Retrieves elements that make up this VCS context. The returned lookup may be empty
     * or may contain any number of the following elements:
     * - instances of Node that were originally used to construct this context object
     *
     * @return Lookup lookup of this VCSContext
     */ 
    public Lookup getElements() {
        return elements;
    }

    /**
     * Retrieves set of files/folders that represent this context.
     * This set contains all files the user selected, unfiltered.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that DO manage folders, such as Clearcase. 
     * 
     * @return Set<VCSFileProxy> set of Files this context represents
     * @see #getRootFiles() 
     */ 
    public Set<VCSFileProxy> getFiles() {
        return unfilteredRootFiles;
    }

    /**
     * Retrieves set of root files/folders that represent this context.
     * This set only contains context roots, not files/folders that are contained within these roots.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that do not manage folders, such as CVS. 
     * 
     * @return Set<VCSFileProxy> set of Files this context represents
     * @see #getFiles() 
     */ 
    public Set<VCSFileProxy> getRootFiles() {
        return rootFiles;
    }

    /**
     * Retrieves set of files/folders that are excluded from this context. Exclusions are files or folders that
     * are descendants of a root folder and should NOT be a part of a versioning operation. For example, an CVS/Update command
     * run on a project that contains a subproject should not touch any files in the subproject. Therefore the VCSContext for
     * the action would contain one root file (the project's root) and one exclusion (subproject root).
     * 
     * @return Set<VCSFileProxy> set of files and folders that are not part of (are excluded from) this context. 
     * All their descendants are excluded too.
     */ 
    public Set<VCSFileProxy> getExclusions() {
        return exclusions;
    }

    /**
     * Determines whether the supplied VCSFileProxy is contained in this context. In other words, the file must be either a root file/folder
     * or be a descendant of a root folder and also must NOT be an excluded file/folder or be a descendant of an excluded folder. 
     * 
     * @param file a VCSFileProxy to test
     * @return true if this context contains the supplied file, false otherwise 
     */ 
    public boolean contains(VCSFileProxy file) {
        outter : for (VCSFileProxy root : rootFiles) {
            if (Utils.isAncestorOrEqual(root, file)) {
                for (VCSFileProxy excluded : exclusions) {
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    static synchronized void flushCached () {
        contextNodesCached.clear();
        contextCached.clear();
    }
        
    private static void addProjectFiles (Collection<VCSFileProxy> rootFiles,
            Map<FileObject, VCSFileProxy> rootFilesExclusions, Project project,
            Set<SourceGroup> srcGroups) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        srcGroups.addAll(Arrays.asList(sourceGroups));
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
            if (rootFile == null) continue;
            if (!srcRootFo.isValid()) {
                LOG.log(Level.WARNING, "addProjectFiles: invalid source root {0}", srcRootFo); //NOI18N
                continue;
            }
            rootFiles.add(rootFile);
            FileObject [] rootChildren = srcRootFo.getChildren();
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                VCSFileProxy child = VCSFileProxy.createFileProxy(rootChildFo);
                // TODO: #60516 deep scan is required here but not performed due to performace reasons
                try {
                    if (!srcRootFo.isValid()) {
                        LOG.log(Level.WARNING, "addProjectFiles: source root {0} changed from valid to invalid", srcRootFo); //NOI18N
                        break;
                    }
                    if (rootChildFo != null && 
                        rootChildFo.isValid() && 
                        !sourceGroup.contains(rootChildFo)) 
                    {
                        child = child.normalizeFile();
                        rootChildFo = child.toFileObject();
                        if(rootChildFo != null && 
                           SharabilityQuery.getSharability(rootChildFo) != Sharability.NOT_SHARABLE) 
                        {
                            rootFilesExclusions.put(rootChildFo, child);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    // #161904
                    Logger logger = LOG;
                    logger.log(Level.WARNING, "addProjectFiles: IAE");
                    logger.log(Level.WARNING, "rootFO: {0}", srcRootFo);
                    if (srcRootFo != sourceGroup.getRootFolder()) {
                        logger.log(Level.WARNING, "root FO has changed");
                    }
                    String children = "[";
                    for (FileObject fo : rootChildren) {
                        children += "\"" + fo.getPath() + "\", ";
                    }
                    children += "]";
                    logger.log(Level.WARNING, "srcRootFo.getChildren(): {0}", children);
                    if(rootChildFo != null) {
                        if (!rootChildFo.isValid()) {
                            logger.log(Level.WARNING, "{0} does not exist ", rootChildFo);
                        }
                        if (!FileUtil.isParentOf(srcRootFo, rootChildFo)) {
                            logger.log(Level.WARNING, "{0} is not under {1}", new Object[]{rootChildFo, srcRootFo});
                        }
                    }
                    logger.log(Level.WARNING, null, ex);
                }
            }
        }
    }

    private static void removeContainedExclusions (Map<FileObject, VCSFileProxy> rootFilesExclusions, Set<SourceGroup> sourceGroups) {
        for (SourceGroup sourceGroup : sourceGroups) {
            for (Iterator<Map.Entry<FileObject, VCSFileProxy>> it = rootFilesExclusions.entrySet().iterator(); it.hasNext(); ) {
                FileObject exclusion = it.next().getKey();
                if (sourceGroup.contains(exclusion)) {
                    it.remove();
                }
            }
        }
    }
    
    private static void addFileObjects(Node node, Set<VCSFileProxy> rootFiles) {
        Collection<? extends NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template<NonRecursiveFolder>(NonRecursiveFolder.class)).allInstances();
        List<VCSFileProxy> nodeFiles = new ArrayList<VCSFileProxy>();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                VCSFileProxy proxy = Utils.createFlatFileProxy(nonRecursiveFolder.getFolder());
                if(proxy != null) {
                    nodeFiles.add(proxy);
                } else {
                    LOG.log(Level.WARNING, "null VCSFileProxy for non recursive folder FileObject {0}", nonRecursiveFolder.getFolder()); // NOI18N
                }
            }
        } else {
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection<VCSFileProxy> doFiles = toFileCollection(dataObject.files());
                    nodeFiles.addAll(doFiles);
                }
            }
        }
        rootFiles.addAll(nodeFiles);
    }
    
    private static Collection<VCSFileProxy> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<VCSFileProxy> files = new HashSet<VCSFileProxy>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
            if(proxy != null) {
                files.add(proxy);
            } else {
                LOG.log(Level.WARNING, "null VCSFileProxy for FileObject {0}", fo); // NOI18N
            }
        }
        files.remove(null);
        return files;
    }    

    private VCSContext(Set<VCSFileProxy> rootFiles, Collection<VCSFileProxy> exclusions, Object... elements) {
        Set<VCSFileProxy> tempRootFiles = new HashSet<VCSFileProxy>(rootFiles);
        Set<VCSFileProxy> tempExclusions = new HashSet<VCSFileProxy>(exclusions);
        this.unfilteredRootFiles = Collections.unmodifiableSet(new HashSet<VCSFileProxy>(tempRootFiles));
        // exclusions that are also root files should be removed
        tempExclusions.removeAll(tempRootFiles);
        while (normalize(tempRootFiles, tempExclusions));
        tempRootFiles.remove(null);
        tempExclusions.remove(null);
        this.rootFiles = Collections.unmodifiableSet(tempRootFiles);
        this.exclusions = Collections.unmodifiableSet(tempExclusions);
        this.elements = Lookups.fixed(elements);
    }

    private VCSContext(Node [] nodes, Set<VCSFileProxy> rootFiles, Collection<VCSFileProxy> exclusions) {
        this(rootFiles, exclusions, nodes != null ? (Object[]) nodes : new Node[0]);
    }

    private VCSContext(Set<? extends FileObject> elements, Set<VCSFileProxy> rootFiles, Collection<VCSFileProxy> exclusions) {
        this(rootFiles, exclusions, elements != null ? elements : Collections.EMPTY_SET);
    }

    private boolean normalize(Set<VCSFileProxy> rootFiles, Set<VCSFileProxy> exclusions) {
        for (Iterator<VCSFileProxy> i = rootFiles.iterator(); i.hasNext();) {
            VCSFileProxy root = i.next();
            for (Iterator<VCSFileProxy> j = exclusions.iterator(); j.hasNext();) {
                VCSFileProxy exclusion = j.next();
                if (Utils.isAncestorOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusions, exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }
    
    private void exclusionRemoved(Set<VCSFileProxy> exclusions, VCSFileProxy exclusion, VCSFileProxy root) {
        VCSFileProxy [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            VCSFileProxy child = exclusionChildren[i];
            if (child != null && !Utils.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    private static Set<VCSFileProxy> substract(Set<VCSFileProxy> roots, Set<VCSFileProxy> exclusions, FileFilter filter) {
        Set<VCSFileProxy> files = new HashSet<VCSFileProxy>(roots);
        Set<VCSFileProxy> checkedFiles = new HashSet<VCSFileProxy>();
        for (VCSFileProxy exclusion : exclusions) {
            assert exclusion != null;
            if (exclusion == null) {
                continue;
            }
            for (;;) {
                VCSFileProxy parent = exclusion.getParentFile();
                /**
                 * only if the parent has not been checked yet - #158221
                 * otherwise skip adding of the siblings - they have been already added
                 */
                if (!checkedFiles.contains(exclusion.getParentFile())) {
                    addSiblings(files, exclusion, filter);
                    checkedFiles.add(parent);
                }
                exclusion = parent;
                files.remove(exclusion);
                if (roots.contains(exclusion)) break;
            }
        }
        files.removeAll(exclusions);
        return files;
    }

    private static void addSiblings(Set<VCSFileProxy> files, VCSFileProxy exclusion, FileFilter filter) {
        if (exclusion.getParentFile() == null) return;  // roots have no siblings
        VCSFileProxy [] siblings = exclusion.getParentFile().listFiles();
        if(siblings != null) {
            for (VCSFileProxy sibling : siblings) {
                if (filter.accept(sibling)) files.add(sibling);
            }
        } else {
            // see issue #213289, but how is this possible?
            LOG.log(Level.WARNING, "no children found for {0}", exclusion.getParentFile()); // NOI18N
        }
        files.remove(exclusion);
    }

    private static Set<VCSFileProxy> emptySet() {
        return Collections.emptySet();
    }

    private void removeDuplicates(Set<VCSFileProxy> files) {
        List<VCSFileProxy> newFiles = new ArrayList<VCSFileProxy>();
        outter: for (Iterator<VCSFileProxy> i = files.iterator(); i.hasNext();) {
            VCSFileProxy file = i.next();
            for (Iterator<VCSFileProxy> j = newFiles.iterator(); j.hasNext();) {
                VCSFileProxy includedFile = j.next();
                if (Utils.isAncestorOrEqual(includedFile, file) && (file.isFile() || !APIAccessor.IMPL.isFlat(includedFile))) continue outter;
                if (Utils.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !APIAccessor.IMPL.isFlat(file))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
}
