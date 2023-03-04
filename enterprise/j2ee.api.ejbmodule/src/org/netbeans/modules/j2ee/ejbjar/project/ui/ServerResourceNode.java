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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.actions.FileSystemAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.nodes.FilterNode.Children;

/**
 * Node to represent the setup folder described in the blueprints.
 *
 * @author Chris Webster, Andrei Badea
 */
public class ServerResourceNode extends FilterNode {
    
    private static final Logger LOGGER = Logger.getLogger(ServerResourceNode.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final String SETUP_DIR = "setup"; // NOI18N
    private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
    
    private Project project;
    private FileChangeListener projectDirectoryListener;
    
    /** Creates a new instance of ServerResourceNode */
    public ServerResourceNode(Project project) throws DataObjectNotFoundException {
        this(getSetupDataFolder(project), project);
    }
    
    private ServerResourceNode(DataFolder folderDo, Project project) throws DataObjectNotFoundException {
        // if lookup would be needed uncomment and use getLookup() method
        super(getDataFolderNode(folderDo, project), getDataFolderNodeChildren(folderDo));
        projectDirectoryListener = new ProjectDirectoryListener();
        if (LOG) {
            LOGGER.log(Level.FINE, "Adding file listener to " + project.getProjectDirectory()); // NOI18N
        }
        project.getProjectDirectory().addFileChangeListener(FileUtil.weakFileChangeListener(projectDirectoryListener, project.getProjectDirectory()));
        this.project = project;
    }

    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    public Image getOpenedIcon( int type ) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private static Image badgeIcon(Image icon) {
        return ImageUtilities.mergeImages(icon, ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ejbjar/project/ui/resourcesBadge.gif", true ), 7, 7);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ServerResourceNode.class, "LBL_Node_ResourceNode");
    }

    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    public boolean canRename() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public Action[] getActions( boolean context ) {
        return new Action[] {
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
        };
    }
        
    private void refresh() {
        if (LOG) {
            LOGGER.log(Level.FINE, "Refreshing"); // NOI18N
        }
        final DataFolder folderDo = getSetupDataFolder(project);
        if (LOG) {
            LOGGER.log(Level.FINE, "The DataFolder is: " + folderDo); // NOI18N
        }
        final Node original = getDataFolderNode(folderDo, project);
        final org.openide.nodes.Children children = getDataFolderNodeChildren(folderDo);
        // #106687: should not call Children.getNodes(true) under Children.MUTEX
        if (LOG) {
            LOGGER.log(Level.FINE, "New children count: " + children.getNodes(true).length);
        }
        // #64665: should not call FilterNode.changeOriginal() or Node.setChildren() 
        // under Children.MUTEX read access
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                changeOriginal(original, false);
                setChildren(children);
            }
        });

    }
    
    private static DataFolder getSetupDataFolder(Project project) {
        FileObject folderFo = project.getProjectDirectory().getFileObject(SETUP_DIR);
        DataFolder folderDo = null;
        if (folderFo != null && folderFo.isFolder()) {
            try {
                folderDo = DataFolder.findFolder(folderFo);
            }
            catch (IllegalArgumentException e) {}
        }
        return folderDo;
    }
    
    private static Node getDataFolderNode(DataFolder folderDo, Project project) {
        // The project in the placeholder node lookup is needed for the New File action.
        return (folderDo != null) ? folderDo.getNodeDelegate() : new PlaceHolderNode(Lookups.singleton(project));
    }
    
    private static org.openide.nodes.Children getDataFolderNodeChildren(DataFolder folderDo) {
        return (folderDo != null) ? folderDo.createNodeChildren(VISIBILITY_QUERY_FILTER) : Children.LEAF;
    }

//    private static Lookup getLookup(Project project) throws DataObjectNotFoundException, FileStateInvalidException {
//        FileObject projectFolder = project.getProjectDirectory();
//        if (!projectFolder.isValid()) {
//            // #205581 - reread the folder if was replaced
//            projectFolder.refresh();
//            if (!projectFolder.isValid()) {
//                // fo is still not valid (probably deleted), we can't provide suitable data to lookup, so we could
//                // use empty one or don't create the node at all - the second solution prevent #126642 issue too
//                throw new FileStateInvalidException();
//            }
//        }
//        return Lookups.singleton(DataFolder.find(projectFolder));
//    }

    private final class ProjectDirectoryListener extends FileChangeAdapter {
        
        public void fileDeleted(FileEvent fe) {
            if (isWatchedFile(getFileName(fe)))
                refresh();
        }

        public void fileFolderCreated(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                refresh();
            }
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            if (isWatchedFile(getFileName(fe)) || isWatchedFile(getOldFileName(fe)))
                refresh();
        }

        private boolean isWatchedFile(String fileName) {
            return fileName.equals(SETUP_DIR);
        }

        private String getFileName(FileEvent fe) {
            return fe.getFile().getNameExt();
        }

        private String getOldFileName(FileRenameEvent fe) {
            String result = fe.getName();
            if (fe.getExt() != "") // NOI18N
                result = result + "." + fe.getExt(); // NOI18N

            return result;
        }
    }
    
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        private EventListenerList ell = new EventListenerList();
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }
                
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }
        
        public void stateChanged( ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0;  i-= 2) {
                if (listeners[i] == ChangeListener.class) {             
                    if (event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i + 1]).stateChanged(event);
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add(ChangeListener.class, listener);
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove(ChangeListener.class, listener);
        }
    }    
    
    /**
     * A placeholder node for a folder node.
     */
    private static final class PlaceHolderNode extends AbstractNode {
        
        public PlaceHolderNode(Lookup lookup) {
            super(Children.LEAF, lookup);
        }

        public Image getIcon(int type) {
            Image image = null;
            Node imageDelegate = getImageDelegate();
            if (imageDelegate != null) {
                image = imageDelegate.getIcon(type);
            }
            if (image == null) {
                image = super.getIcon(type);
            }
            return image;
        }
        
        public Image getOpenedIcon(int type) {
            Image image = null;
            Node imageDelegate = getImageDelegate();
            if (imageDelegate != null) {
                image = imageDelegate.getOpenedIcon(type);
            }
            if (image == null) {
                image = super.getOpenedIcon(type);
            }
            return image;
        }
        
        private static Node getImageDelegate() {
            FileObject imageFo = FileUtil.getConfigRoot();
            if (imageFo != null) {
                try {
                    DataObject imageDo = DataObject.find(imageFo);
                    return imageDo.getNodeDelegate();
                } catch (DataObjectNotFoundException donfe) {
                    Logger.getLogger("global").log(Level.INFO, null, donfe);
                }
            }
            return null;
        }
    }
}

