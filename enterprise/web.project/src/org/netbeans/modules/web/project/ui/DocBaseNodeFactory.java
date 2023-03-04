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

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=100)
public final class DocBaseNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public DocBaseNodeFactory() {
    }

    private static RequestProcessor RP = new RequestProcessor();
    
    public NodeList createNodes(Project p) {
        WebProject project = p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new DocBaseNodeList(project);
    }

    private static class DocBaseNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String DOC_BASE = "docBase"; //NOI18N
        private static final String WEB_INF = "webInf"; //NOI18N

        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        
        private SourceGroup webDocRoot;
        
        DocBaseNodeList(WebProject proj) {
            project = proj;
            evaluator = project.evaluator();
            helper = project.getUpdateHelper();
            Sources s = project.getLookup().lookup(Sources.class);
            assert s != null;
//            assert s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT).length > 0;

            if(s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT).length > 0)
            {
                webDocRoot = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT)[0];
            }
            else
            {
                String name = ProjectUtils.getInformation( proj ).getDisplayName();

                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DocBaseNodeList.class, "LBL_No_Source_Groups_Found", name), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
        
        public List<String> keys() {
            FolderHolder nodeFolders = getNodeFolders();
            List<String> result = new ArrayList<String>();
            result.add(DOC_BASE + getFolderPath(nodeFolders.getWebDocBaseDir()));
            if (!nodeFolders.hasCorrectStructure()) {
                result.add(WEB_INF + getFolderPath(nodeFolders.getWebInfDir()));
            }
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            FolderHolder nodeFolders = getNodeFolders();
            if (key.startsWith(DOC_BASE)) {
                FileObject webDocBaseDir = nodeFolders.getWebDocBaseDir();
                DataFolder webFolder = getFolder(webDocBaseDir);
                if (webFolder != null) {
                    return new DocBaseNode(webFolder, project, new VisibilityQueryDataFilter(webDocRoot));
                }
                return null;
            } else if (key.startsWith(WEB_INF)) {
                if (nodeFolders.hasCorrectStructure()) {
                    return null;
                }
                FileObject webInfDir = nodeFolders.getWebInfDir();
                DataFolder webInfFolder = getFolder(webInfDir);
                if (webInfFolder != null) {
                    return new WebInfNode(webInfFolder, project, new VisibilityQueryDataFilter(null));
                }
                return null;
            }
            assert false: "No node for key: " + key; // NOI18N
            return null;
        }

        public void addNotify() {
            evaluator.addPropertyChangeListener(this);
        }

        public void removeNotify() {
            evaluator.removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            
            // #197171 - changeSupport used to fire change in AWT thread which
            // does not work in following scenario: when project properties has
            // been changed and are being saved the write lock on ProjectManager
            // can be hold for long time. And that prevents getNodeFolders()
            // method in this class from being executed (in response to change). Therefore I posted
            // below fireChange to background thread rather then AWT thread:
            RP.post(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private DataFolder getFolder(FileObject folder) {
            if (folder != null) {
                return DataFolder.findFolder(folder);
            }
            return null;
        }
        
        // # 114402
        private String getFolderPath(FileObject folder) {
            if (folder == null) {
                return "";
            }
            return folder.getPath();
        }
        
        private FileObject getFileObject(String propName) {
            String foName = evaluator.getProperty(propName);
            if (foName == null) {
                return null;
            }
            FileObject fo = helper.getAntProjectHelper().resolveFileObject(foName);
            // when the project is deleted externally, the sources change could
            // trigger a call to thid method before the project directory is 
            // notified about the deletion - invalid FileObject-s could be returned
            return fo != null && fo.isValid() ? fo : null;
        }
        
        private FolderHolder getNodeFolders() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<FolderHolder>() {
                public FolderHolder run() {
                    FileObject webDocBaseDir = getFileObject(WebProjectProperties.WEB_DOCBASE_DIR);
                    FileObject webInf = getFileObject(WebProjectProperties.WEBINF_DIR);
                    
                    return new FolderHolder(webDocBaseDir, webInf);
                }
            });
        }
        
        private static final class FolderHolder {
            private final FileObject webDocBaseDir;
            private final FileObject webInfDir;

            public FolderHolder(FileObject webDocBaseDir, FileObject webInfDir) {
                this.webDocBaseDir = webDocBaseDir;
                this.webInfDir = webInfDir;
            }

            public FileObject getWebDocBaseDir() {
                return webDocBaseDir;
            }

            public FileObject getWebInfDir() {
                return webInfDir;
            }
            
            /**
             * Return <code>true</code> if <tt>WEB-INF<tt> folder
             * is located inside <tt>web</tt> folder.
             * Return <code>false</code> if any of these folders
             * is <code>null</code>.
             */
            public boolean hasCorrectStructure() {
                if (webDocBaseDir == null
                        || webInfDir == null) {
                    return false;
                }
                return FileUtil.isParentOf(webDocBaseDir, webInfDir);
            }
        }
    }
    
    private static final class DocBaseNode extends BaseNode {
        
        DocBaseNode (DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super(folder, project, filter);
        }
        
        @Override
        public String getDisplayName () {
            return NbBundle.getMessage(DocBaseNodeFactory.class, "LBL_Node_DocBase"); //NOI18N
        }
    }

    private static final class WebInfNode extends BaseNode {
        
        WebInfNode (DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super (folder, project, filter);
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DocBaseNodeFactory.class, "LBL_Node_WebInf"); //NOI18N
        }
    }

    private abstract static class BaseNode extends FilterNode {
        private static Image WEB_PAGES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/web/project/ui/resources/webPagesBadge.gif" ); //NOI18N
        /**
         * The MIME type of Java files.
         */
        private static final String JAVA_MIME_TYPE = "text/x-java"; //NO18N
        private Action actions[];
        protected final WebProject project;
        
        BaseNode(final DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(filter));
            this.project = project;
        }

        @Override
        public Image getIcon(int type) {        
            return computeIcon(false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Node getDataFolderNodeDelegate() {
            return getLookup().lookup(DataFolder.class).getNodeDelegate();
        }

        private Image computeIcon(boolean opened, int type) {
            Image image;

            image = opened ? getDataFolderNodeDelegate().getOpenedIcon(type) : getDataFolderNodeDelegate().getIcon(type);
            image = ImageUtilities.mergeImages(image, WEB_PAGES_BADGE, 7, 7);

            return image;        
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public Action[] getActions(boolean context) {
            if (actions == null) {
                actions = new Action[9];
                actions[0] = CommonProjectActions.newFileAction();
                actions[1] = null;
                actions[2] = SystemAction.get(FindAction.class);
                actions[3] = null;
                actions[4] = SystemAction.get(PasteAction.class);
                actions[5] = null;
                actions[6] = SystemAction.get(FileSystemAction.class);
                actions[7] = null;
                actions[8] = ProjectUISupport.createPreselectPropertiesAction(project, "Sources", null); //NOI18N
            }
            return actions;
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                    Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (data != null) {
                        List list = (List) data;
                        for (Object each : list) {
                            File f = FileUtil.normalizeFile((File) each);
                            FileObject file = FileUtil.toFileObject(f);
                            if (file != null && JAVA_MIME_TYPE.equals(file.getMIMEType())) { //NO18N
                                // don't allow java files, see #119968
                                return null;
                            }
                        }
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return super.getDropType(t, action, index);
        }
    }

    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter, PropertyChangeListener {
        private static final long serialVersionUID = 1L;
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final SourceGroup sourceGroup;
        
        public VisibilityQueryDataFilter(SourceGroup sourceGroup) {
            this.sourceGroup = sourceGroup;
            if (this.sourceGroup != null) {
                this.sourceGroup.addPropertyChangeListener(this);
            }
            VisibilityQuery defaultQuery = VisibilityQuery.getDefault();
            defaultQuery.addChangeListener(WeakListeners.change(this, defaultQuery));
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return (sourceGroup == null || sourceGroup.contains(fo)) && VisibilityQuery.getDefault().isVisible(fo);
        }
        
        public void stateChanged( ChangeEvent e) {            
            changeSupport.fireChange();
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            changeSupport.addChangeListener(listener);
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            changeSupport.removeChangeListener(listener);
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            changeSupport.fireChange();
        }
        
    }

}
