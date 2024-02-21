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

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.DocBaseNodeFactory.VisibilityQueryDataFilter;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=500)
public final class ConfFilesNodeFactory implements NodeFactory {

    /** Creates a new instance of ConfFilesNodeFactory */
    public ConfFilesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = (WebProject) p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new ConfFilesNodeList(project);
    }

    private static class ConfFilesNodeList implements NodeList<String>, PropertyChangeListener {

        private static final String CONF_FILES = "confFiles"; //NOI18N
        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        ConfFilesNodeList(WebProject proj) {
            project = proj;
            WebLogicalViewProvider logView = (WebLogicalViewProvider) project.getLookup().lookup(WebLogicalViewProvider.class);
            assert logView != null;
        }

        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(CONF_FILES);
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (CONF_FILES.equals(key)) {
                return new ConfFilesNode(project);
            }
            assert false : "No node for key: " + key;
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
    }

    private static final class ConfFilesNode extends org.openide.nodes.AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

        private static final Image CONFIGURATION_FILES_BADGE = ImageUtilities.loadImage("org/netbeans/modules/web/project/ui/resources/config-badge.gif", true); // NOI18N

        // icon badging >>>
        private Set files;
        private Map<FileSystem, FileStatusListener> fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private final Project project;
        private  Node iconDelegate;

        public ConfFilesNode(Project prj) {
            this(prj, Children.create(ConfFilesChildrenFactory.forProject(prj), true));
        }

        private ConfFilesNode(Project prj, Children children) {
            super(children, createLookup(prj, children));
            this.project = prj;
            setName("configurationFiles"); // NOI18N
            iconDelegate = DataFolder.findFolder (FileUtil.getConfigRoot()).getNodeDelegate();
        }

        private static Lookup createLookup(Project project,
                Children children) {

            if (project.getProjectDirectory().isValid()) {
                SearchInfoDefinition searchInfo;
                searchInfo = SearchInfoDefinitionFactory.createSearchInfoBySubnodes(children);
                return Lookups.fixed(project, searchInfo);
            } else {
                return Lookups.fixed(project);
            }
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Image computeIcon(boolean opened, int type) {
            Image image;

            image = opened ? iconDelegate.getOpenedIcon(type) : iconDelegate.getIcon(type);
            image = ImageUtilities.mergeImages(image, CONFIGURATION_FILES_BADGE, 7, 7);

            return image;        
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ConfFilesNodeFactory.class, "LBL_Node_Config"); //NOI18N
        }

        @Override
        public javax.swing.Action[] getActions(boolean context) {
            return new javax.swing.Action[]{SystemAction.get(FindAction.class)};
        }

        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }

        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }

            synchronized (privateLock) {
                if ((!iconChange && event.isIconChange()) || (!nameChange && event.isNameChange())) {
                    Iterator it = files.iterator();
                    while (it.hasNext()) {
                        FileObject fo = (FileObject) it.next();
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(50); // batch by 50 ms
        }

        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }

        protected void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project); // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }

        private void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }

        protected void setFiles(Set files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }

            fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
            this.files = files;
            if (files == null) {
                return;
            }
            Iterator it = files.iterator();
            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(Exceptions.attachMessage(e, "Can not get " + fo + " filesystem, ignoring...")); // NO18N
                }
            }
        }
    }
    
    /**
     * Additional fix for IZ#170098 - 39s - expanding nodes can take ages 
     * @author ads
     *
     */
    private static final class ConfFilesChildrenFactory extends 
        ChildFactory.Detachable<FileObject>
    {
        private static final String[] WELL_KNOWN_FILES = {"web.xml", "webservices.xml", 
            "struts-config.xml", "faces-config.xml", "portlet.xml", "navigator.xml", 
            "managed-beans.xml", "beans.xml"}; //NOI18N
        
        private static final java.util.Comparator<FileObject> COMPARATOR = 
            new NodeComparator();
        
        private ConfFilesChildrenFactory(ProjectWebModule webModule) {
            myWebModule = webModule;
            myKeys = Collections.emptySet();
        }
        
        public static ConfFilesChildrenFactory forProject(Project project) {
            ProjectWebModule pwm = (ProjectWebModule) project.getLookup().lookup(
                    ProjectWebModule.class);
            return new ConfFilesChildrenFactory(pwm);
        }
        
        void update(){
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    refresh( false );
                }
            });
        }
        
        @Override
        protected void removeNotify() {
            removeListeners();
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        public Node createNodeForKey( FileObject fo ) {
            Node node = null;

            try {
                DataObject dataObject = DataObject.find(fo);
                node = dataObject.getNodeDelegate().cloneNode();
                if (fo.isFolder()) {
                    DataFolder dataFolder = DataFolder.findFolder(fo);
                    node = new FilterNode(node, dataFolder
                            .createNodeChildren(new VisibilityQueryDataFilter(
                                    null)));
                }
            }
            catch (DataObjectNotFoundException dnfe) {
            }

            return node;
        }
        

        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
         */
        @Override
        protected boolean createKeys( List<FileObject> keys ) {
            boolean result = false;
            myKeys = new HashSet<FileObject>( );
            if ( addWellKnownFiles( ) ){
                result = true;
            }
            if ( !result && addConfDirectoryFiles( ) ){
                result =  true;
            }
            if ( !result && addPersistenceXmlDirectoryFiles( ) ){
                result = true;
            }
            if ( !result && addServerSpecificFiles( ) ){
                result = true;
            }
            if ( !result && addFrameworkFiles(  ) ){
                result = true;
            }
            if ( !result && addWebFragments(  ) ){
                result = true;
            }
            keys.addAll( myKeys );
            keys.sort(COMPARATOR);
            return true;
        }
        
        private void removeListeners() {
            getWebModule().removeConfigurationFilesListener(
                    myServerSpecificFilesListener);

            FileObject webInf = getWebModule().getWebInf(true);
            if (webInf != null) {
                getWebModule().getWebInf().removeFileChangeListener(myWebInfListener);
            }
            if (myConfDir != null) {
                myConfDir.removeFileChangeListener(myAnyFileListener);
            }
            if ( myPersistenceXmlDir != null ){
                myPersistenceXmlDir.removeFileChangeListener(myAnyFileListener);
            }
        }
        
        private boolean addWellKnownFiles(  ) {
            FileObject webInf = getWebModule().getWebInf(true);
            if (webInf == null) {
                return false;
            }
            boolean result = false;
            for (int i = 0; i < WELL_KNOWN_FILES.length; i++) {
                FileObject fo = webInf.getFileObject(WELL_KNOWN_FILES[i]);
                if (fo != null) {
                    myKeys.add(fo);
                }
                if ( Thread.interrupted() ){
                    result = true;
                    break;
                }
            }

            webInf.addFileChangeListener(myWebInfListener);
            return result;
        }
        
        private boolean addConfDirectoryFiles( ) {
            myConfDir = getWebModule().getConfDir();
            if (myConfDir == null) {
                return false;
            }
            boolean result = false;
            FileObject[] children = myConfDir.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (VisibilityQuery.getDefault().isVisible(children[i])) {
                    myKeys.add(children[i]);
                }
                if ( Thread.interrupted()){
                    result = true;
                    break;
                }
            }

            myConfDir.addFileChangeListener(myAnyFileListener);
            return result;
        }
        
        private boolean addPersistenceXmlDirectoryFiles(  ) {
            myPersistenceXmlDir = getWebModule().getPersistenceXmlDir();
            if (myPersistenceXmlDir == null ||
                    (myConfDir != null && FileUtil.toFile(myPersistenceXmlDir).
                            equals(FileUtil.toFile(myConfDir)))) 
            {
                return false;
            }
            boolean result = false;
            FileObject[] children = myPersistenceXmlDir.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (VisibilityQuery.getDefault().isVisible(children[i])) {
                    myKeys.add(children[i]);
                }
                if ( Thread.interrupted() ){
                    result = true;
                    break;
                }
            }

            myPersistenceXmlDir.addFileChangeListener(myAnyFileListener);
            return result;
        }
        
        private boolean addServerSpecificFiles( ) {
            FileObject[] files = getWebModule().getConfigurationFiles();

            boolean result = false;
            for (int i = 0; i < files.length; i++) {
                myKeys.add(files[i]);
                if ( Thread.interrupted() ){
                    result = true;
                    break;
                }
            }

            getWebModule().addConfigurationFilesListener(myServerSpecificFilesListener);
            return result;
        }
        
        private boolean addFrameworkFiles(  ) {
            List<WebFrameworkProvider> providers = WebFrameworks.getFrameworks();
            boolean result = false;
            start :
            for (int i = 0; i < providers.size(); i++) {
                WebFrameworkProvider provider = (WebFrameworkProvider) providers.get(i);
                FileObject wmBase = getWebModule().getDocumentBase();
                File[] files = null;
                if (wmBase != null) {
                    WebModule wm = WebModule.getWebModule(wmBase);
                    if (wm != null) {
                        files = provider.getConfigurationFiles(wm);
                    }
                }
                if (files != null) {
                    for (int j = 0; j < files.length; j++) {
                        FileObject fo = FileUtil.toFileObject(files[j]);
                        if (fo != null) {
                            myKeys.add(fo);
                            // XXX - do we need listeners on these files?
                            //fo.addFileChangeListener(anyFileListener);
                        }
                        if ( Thread.interrupted()) {
                            result = true;
                            break start;
                        }
                    }
                }
            }
            return result;
        }
        
        private boolean addWebFragments( ) { 
            try {
                List<FileObject> frags = getWebModule().getMetadataModel().
                    runReadAction(new MetadataModelAction<WebAppMetadata, 
                            List<FileObject>>() 
                    {
                        public List<FileObject> run(WebAppMetadata metadata) 
                            throws Exception 
                        {
                            return metadata.getFragmentFiles();
                        }
                });
                myKeys.addAll(frags);
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            FileObject[] roots = getWebModule().getSourceRoots();
            if (roots != null) {
                for (FileObject root : roots) {
                    ClassPath cp = getWebModule().getClassPathProvider().
                        findClassPath(root, ClassPath.COMPILE);
                    if (cp != null) {
                        cp.removePropertyChangeListener(myClassPathListener);
                        cp.addPropertyChangeListener(myClassPathListener);
                    }
                }
            }
            return Thread.interrupted();
        }
        
        private ProjectWebModule getWebModule(){
            return myWebModule;
        }
        
        private boolean isWellKnownFile(String name) {
            for (int i = 0; i < WELL_KNOWN_FILES.length; i++) {
                if (name.equals(WELL_KNOWN_FILES[i])) {
                    return true;
                }
            }
            return false;
        }
        
        private final FileChangeListener myWebInfListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    refresh(false);
                }
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                // if the old file name was in keys, the new file name
                // is now there (since it's the same FileObject)
                if ( myKeys.contains(fe.getFile())) {
                    refresh( false );
                } else {
                    // the key is not contained, so add it if it's well-known
                    if (isWellKnownFile(fe.getFile().getNameExt())) {
                        refresh( false );
                    }
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    refresh(false);
                }
            }
        };
        
        private final FileChangeListener myAnyFileListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                refresh( false );
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                refresh( false );
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                refresh(false);
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                refresh(false);
            }
        };
        
        private final ConfigurationFilesListener myServerSpecificFilesListener = 
            new ConfigurationFilesListener() 
        {

            public void fileCreated(FileObject fo) {
                refresh(false);
            }

            public void fileDeleted(FileObject fo) {
                refresh(false);
            }
        };
        
        private final ClassPathChangeListener myClassPathListener = 
                new ClassPathChangeListener(this);
        
        private ProjectWebModule myWebModule;
        private Set<FileObject> myKeys;
        private FileObject myConfDir;
        private FileObject myPersistenceXmlDir;
    }

    /*
     * Original Children realization before above fix for IZ#170098. 
     * private static final class ConfFilesChildren extends Children.Keys<FileObject> {

        private static final String[] wellKnownFiles = {"web.xml", "webservices.xml", 
            "struts-config.xml", "faces-config.xml", "portlet.xml", "navigator.xml", 
            "managed-beans.xml"}; //NOI18N
        private final ProjectWebModule pwm;
        private final HashSet<FileObject> keys;
        private final java.util.Comparator<FileObject> comparator = new NodeComparator();
        // Need to hold the conf dir strongly, otherwise it can be garbage-collected.
        private FileObject confDir;
        private FileObject persistenceXmlDir;

        private final FileChangeListener webInfListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    addKey(fe.getFile());
                }
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                // if the old file name was in keys, the new file name
                // is now there (since it's the same FileObject)
                if (keys.contains(fe.getFile())) {
                    // so we need to remove it if it's not well-known
                    if (!isWellKnownFile(fe.getFile().getNameExt())) {
                        removeKey(fe.getFile());
                    } else {
                        // this causes resorting of the keys
                        doSetKeys();
                    }
                } else {
                    // the key is not contained, so add it if it's well-known
                    if (isWellKnownFile(fe.getFile().getNameExt())) {
                        addKey(fe.getFile());
                    }
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    removeKey(fe.getFile());
                }
            }
        };

        private final FileChangeListener anyFileListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                addKey(fe.getFile());
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                removeKey(fe.getFile());
            }
        };

        private final ConfigurationFilesListener serverSpecificFilesListener = 
            new ConfigurationFilesListener() 
        {

            public void fileCreated(FileObject fo) {
                addKey(fo);
            }

            public void fileDeleted(FileObject fo) {
                removeKey(fo);
            }
        };

        private final ClassPathChangeListener cpListener = new ClassPathChangeListener(this);

        private ConfFilesChildren(ProjectWebModule pwm) {
            this.pwm = pwm;
            keys = new HashSet<FileObject>();
        }

        public static Children forProject(Project project) {
            ProjectWebModule pwm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
            return new ConfFilesChildren(pwm);
        }

        @Override
        protected void addNotify() {
            createKeys();
            doSetKeys();
        }

        @Override
        protected void removeNotify() {
            removeListeners();
        }

        public Node[] createNodes(FileObject fo) {
            Node n = null;

            if (keys.contains(fo)) {
                try {
                    DataObject dataObject = DataObject.find(fo);
                    n = dataObject.getNodeDelegate().cloneNode();
                    if (fo.isFolder()) {
                        DataFolder dataFolder = DataFolder.findFolder(fo);
                        n = new FilterNode(n, dataFolder.createNodeChildren(new VisibilityQueryDataFilter(null)));
                    }
                } catch (DataObjectNotFoundException dnfe) {
                }
            }

            return (n == null) ? new Node[0] : new Node[]{n};
        }

        public void refreshNodes() {
            SwingUtilities.invokeLater( new Runnable( ){
                public void run() {
                    addNotify();
                };
            });
        }

        private synchronized void addKey(FileObject key) {
            if (VisibilityQuery.getDefault().isVisible(key)) {
                //System.out.println("Adding " + key.getPath());
                keys.add(key);
                doSetKeys();
            }
        }

        private synchronized void removeKey(FileObject key) {
            //System.out.println("Removing " + key.getPath());
            keys.remove(key);
            doSetKeys();
        }

        private synchronized void createKeys() {
            keys.clear();

            addWellKnownFiles();
            addConfDirectoryFiles();
            addPersistenceXmlDirectoryFiles();
            addServerSpecificFiles();
            addFrameworkFiles();
            addWebFragments();
        }

        private void doSetKeys() {
            final FileObject[] result = keys.toArray(new FileObject[keys.size()]);
            java.util.Arrays.sort(result, comparator);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    setKeys(result);
                }
            });
        }

        private void addWellKnownFiles() {
            FileObject webInf = pwm.getWebInf(true);
            if (webInf == null) {
                return;
            }
            for (int i = 0; i < wellKnownFiles.length; i++) {
                FileObject fo = webInf.getFileObject(wellKnownFiles[i]);
                if (fo != null) {
                    keys.add(fo);
                }
            }

            webInf.addFileChangeListener(webInfListener);
        }

        private void addConfDirectoryFiles() {
            confDir = pwm.getConfDir();
            if (confDir == null) {
                return;
            }
            FileObject[] children = confDir.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (VisibilityQuery.getDefault().isVisible(children[i])) {
                    keys.add(children[i]);
                }
            }

            confDir.addFileChangeListener(anyFileListener);
        }
        
        private void addPersistenceXmlDirectoryFiles() {
            persistenceXmlDir = pwm.getPersistenceXmlDir();
            if (persistenceXmlDir == null ||
                    (confDir != null && FileUtil.toFile(persistenceXmlDir).equals(FileUtil.toFile(confDir)))) {
                return;
            }
            FileObject[] children = persistenceXmlDir.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (VisibilityQuery.getDefault().isVisible(children[i])) {
                    keys.add(children[i]);
                }
            }

            persistenceXmlDir.addFileChangeListener(anyFileListener);
        }

        private void addServerSpecificFiles() {
            FileObject[] files = pwm.getConfigurationFiles();

            for (int i = 0; i < files.length; i++) {
                keys.add(files[i]);
            }

            pwm.addConfigurationFilesListener(serverSpecificFilesListener);
        }

        private void addFrameworkFiles() {
            List providers = WebFrameworks.getFrameworks();
            for (int i = 0; i < providers.size(); i++) {
                WebFrameworkProvider provider = (WebFrameworkProvider) providers.get(i);
                FileObject wmBase = pwm.getDocumentBase();
                File[] files = null;
                if (wmBase != null) {
                    files = provider.getConfigurationFiles(WebModule.getWebModule(wmBase));
                }
                if (files != null) {
                    for (int j = 0; j < files.length; j++) {
                        FileObject fo = FileUtil.toFileObject(files[j]);
                        if (fo != null) {
                            keys.add(fo);
                            // XXX - do we need listeners on these files?
                            //fo.addFileChangeListener(anyFileListener);
                        }
                    }
                }
            }
        }

        private void addWebFragments() {
            try {
                List<FileObject> frags = pwm.getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, List<FileObject>>() {
                    public List<FileObject> run(WebAppMetadata metadata) throws Exception {
                        return metadata.getFragmentFiles();
                    }
                });
                keys.addAll(frags);
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            FileObject[] roots = pwm.getSourceRoots();
            if (roots != null) {
                for (FileObject root : roots) {
                    ClassPath cp = pwm.getClassPathProvider().findClassPath(root, ClassPath.COMPILE);
                    if (cp != null) {
                        cp.removePropertyChangeListener(cpListener);
                        cp.addPropertyChangeListener(cpListener);
                    }
                }
            }
        }

        private void removeListeners() {
            pwm.removeConfigurationFilesListener(serverSpecificFilesListener);

            FileObject webInf = pwm.getWebInf(true);
            if (webInf != null) {
                pwm.getWebInf().removeFileChangeListener(webInfListener);
            }
            if (confDir != null) {
                confDir.removeFileChangeListener(anyFileListener);
            }
        }

        private boolean isWellKnownFile(String name) {
            for (int i = 0; i < wellKnownFiles.length; i++) {
                if (name.equals(wellKnownFiles[i])) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class ClassPathChangeListener implements PropertyChangeListener {
        private ConfFilesChildren confFiles;
        ClassPathChangeListener(ConfFilesChildren confFiles) {
            this.confFiles = confFiles;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            confFiles.refreshNodes();
        }
    }*/
    
    private static final class NodeComparator implements java.util.Comparator<FileObject>, Serializable {

        @Override
        public int compare(FileObject fo1, FileObject fo2) {

            int result = compareType(fo1, fo2);
            if (result == 0) {
                result = compareNames(fo1, fo2);
            }
            if (result == 0) {
                return fo1.getPath().compareTo(fo2.getPath());
            }
            return result;
        }

        private int compareType(FileObject fo1, FileObject fo2) {
            int folder1 = fo1.isFolder() ? 0 : 1;
            int folder2 = fo2.isFolder() ? 0 : 1;

            return folder1 - folder2;
        }

        private int compareNames(FileObject do1, FileObject do2) {
            return do1.getNameExt().compareTo(do2.getNameExt());
        }

    }
    
    private static class ClassPathChangeListener implements PropertyChangeListener {
        ClassPathChangeListener(ConfFilesChildrenFactory factory) {
            myFactory =  factory;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            myFactory.update();
        }
        private ConfFilesChildrenFactory myFactory;
    }

}
