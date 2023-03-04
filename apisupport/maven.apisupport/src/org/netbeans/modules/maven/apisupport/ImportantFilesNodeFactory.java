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

package org.netbeans.modules.maven.apisupport;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.api.NodeFactoryUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.maven.apisupport.Bundle.*;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=650)
public class ImportantFilesNodeFactory implements NodeFactory {
    /** Package private for unit tests. */
    static final String IMPORTANT_FILES_NAME = "important.files"; // NOI18N
    
    private static final @StaticResource String CONFIG_BADGE = "org/netbeans/modules/maven/apisupport/config-badge.gif";
    
    static final String LAYER = "LAYER-FILE.PLACEHOLDER"; //NOI18N
    /** Package private for unit tests only. */
    static final RequestProcessor RP = new RequestProcessor();
    
    /** Creates a new instance of ImportantFilesNodeFactory */
    public ImportantFilesNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project p) {
        if (p.getLookup().lookup(NbModuleProvider.class) != null) {
            return new ImpFilesNL(p);
        }
        return NodeFactorySupport.fixedNodeList();
    }

    private static class ImpFilesNL implements NodeList<String> {
        private Project project;
        
        public ImpFilesNL(Project p) {
            project = p;
        }
    
        @Override
        public List<String> keys() {
            return Collections.singletonList(IMPORTANT_FILES_NAME);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        @Override
        public Node node(String key) {
            assert key == IMPORTANT_FILES_NAME;
            return new ImportantFilesNode(project);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }
    }
    
    /**
     * Show node "Important Files" with various config and docs files beneath it.
     */
    @Messages("LBL_important_files=Important Files")
    static final class ImportantFilesNode extends AnnotatedNode {
        
        public ImportantFilesNode(Project project) {
            super(new ImportantFilesChildren(project));
        }
        
        ImportantFilesNode(Children ch) {
            super(ch);
        }
        
        @Override
        public String getName() {
            return IMPORTANT_FILES_NAME;
        }
        
        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage(CONFIG_BADGE, true); //NOI18N
            return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(opened), badge, 8, 8);
        }
        
        @Override
        public String getDisplayName() {
            return annotateName(LBL_important_files());
        }
        
        @Override
        public String getHtmlDisplayName() {
            return NodeFactoryUtils.computeAnnotatedHtmlDisplayName(LBL_important_files(), getFiles());
        }
        
        @Override
        public Image getIcon(int type) {
            return annotateIcon(getIcon(false), type);
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            return annotateIcon(getIcon(true), type);
        }
        
    }
    
    /**
     * Actual list of important files.
     */
    @Messages({"LBL_module_manifest=Module Manifest", "LBL_module.xml=Module Descriptor (deprecated)"})
    private static final class ImportantFilesChildren extends Children.Keys<String> {
        
        private List<String> visibleFiles = new ArrayList<String>();
        private FileChangeListener fcl;
        boolean nolayer = false;
        
        private FileChangeListener layerfcl = new FileChangeAdapter() {
            @Override
            public void fileDeleted(FileEvent fe) {
                nolayer = true;
                refreshKeys();
            }
        };
        
        /** Abstract location to display name. */
        private static final java.util.Map<String,String> FILES = new LinkedHashMap<String,String>();
        static {
            FILES.put("src/main/nbm/manifest.mf", LBL_module_manifest()); //NOI18N
            FILES.put("src/main/nbm/module.xml", LBL_module_xml()); //NOI18N
        }
        
        private final Project project;
        
        public ImportantFilesChildren(Project project) {
            this.project = project;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            attachListeners();
            refreshKeys();
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<String>emptySet());
            removeListeners();
            super.removeNotify();
        }
        
        @Override
        protected Node[] createNodes(String key) {
            if (LAYER.equals(key)) {
                Node nd = NodeFactoryUtils.createLayersNode(project);
                if (nd != null) {
                    DataObject dobj = nd.getLookup().lookup(DataObject.class);
                    if (dobj != null) {
                        FileObject fo = dobj.getPrimaryFile();
                        fo.addFileChangeListener(FileUtil.weakFileChangeListener(layerfcl, fo));
                    }
                    return new Node[] {nd };
                }
                return new Node[0];
            }
            else {
                FileObject file = project.getProjectDirectory().getFileObject(key);
                if (file != null) {
                    try {
                        Node orig = DataObject.find(file).getNodeDelegate();
                        return new Node[] {NodeFactoryUtils.createSpecialFileNode(orig, FILES.get(key))};
                    } catch (DataObjectNotFoundException e) {
                        throw new AssertionError(e);
                    }
                }
                return new Node[0];
            } 
        }
        
        private void refreshKeys() {
            //#149566 prevent setting keys under project mutex.
            if (ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        refreshKeys();
                    }
                });
                return;
            }
            Set<FileObject> files = new HashSet<FileObject>();
            List<String> newVisibleFiles = new ArrayList<String>();
            if (!nolayer) {
                newVisibleFiles.add(LAYER);
                nolayer = false;
            }
            //TODO figure out location of layer file somehow and ad to files..
            // influences the scm annotations only..
//            LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
//            FileObject layerFile = handle.getLayerFile();
//            if (layerFile != null) {
//                newVisibleFiles.add(handle);
//                files.add(layerFile);
//            }
            Iterator<String> it = FILES.keySet().iterator();
            while (it.hasNext()) {
                String loc = it.next();
                FileObject file = project.getProjectDirectory().getFileObject(loc);
                if (file != null) {
                    newVisibleFiles.add(loc);
                    files.add(file);
                }
            }
            if (!isInitialized() || !newVisibleFiles.equals(visibleFiles)) {
                visibleFiles = newVisibleFiles;
//                visibleFiles.add(project.getLookup().lookup(ServiceNodeHandler.class));
                RP.post(new Runnable() { // #72471
                    @Override
                    public void run() {
                        setKeys(visibleFiles);
                    }
                });
                ((ImportantFilesNode) getNode()).setFiles(files);
            }
        }
        
        private void attachListeners() {
            try {
                if (fcl == null) {
                    fcl = new FileChangeAdapter() {
                        @Override
                        public void fileDataCreated(FileEvent fe) {
                            refreshKeys();
                        }
                        @Override
                        public void fileDeleted(FileEvent fe) {
                            refreshKeys();
                        }
                    };
                    project.getProjectDirectory().getFileSystem().addFileChangeListener(fcl);
                }
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }
        
        private void removeListeners() {
            if (fcl != null) {
                try {
                    project.getProjectDirectory().getFileSystem().removeFileChangeListener(fcl);
                } catch (FileStateInvalidException e) {
                    assert false : e;
                }
                fcl = null;
            }
        }
        
    }
        
    }
