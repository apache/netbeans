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

package org.netbeans.modules.favorites;

import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/** List of all actions available for Favorites module.
* @author   Jaroslav Tulach
*/
public final class Actions extends Object {
    
    /** Used to keep current dir from JFileChooser for Add to Favorites action
     * on root node. */
    private static File currentDir = null;

    private static final Logger LOG = Logger.getLogger(Actions.class.getName());

    private Actions () {
        // noinstances
    }
    
    @ActionID(id = "org.netbeans.modules.favorites.Add", category = "Window")
    @ActionRegistration(displayName = "#ACT_Add", lazy=false)
    @ActionReference(position = 300, path = "UI/ToolActions/Files")
    public static ContextAwareAction add() { return Add.getDefault(); }

    public static Action addOnFavoritesNode () { return AddOnFavoritesNode.getDefault(); }

    @ActionID(id = "org.netbeans.modules.favorites.Remove", category = "Window")
    @ActionRegistration(displayName = "#ACT_Remove")
    public static Action remove () { return Remove.getDefault(); }

    @ActionID(id = "org.netbeans.modules.favorites.Select", category = "Window/SelectDocumentNode")
    @ActionRegistration(displayName = "#ACT_Select_Main_Menu", lazy=false)
    @ActionReference(position = 2800, name = "org-netbeans-modules-favorites-SelectInFavorites", path = "Menu/GoTo")
    public static ContextAwareAction select () { return Select.getDefault(); }
    
    /** An action which selects activated nodes in the Explorer's tab.
    * @author   Dusan Balek
    */
    private static class Select extends NodeAction {
        private static final Select SELECT = new Select ();
        
        public static ContextAwareAction getDefault () {
            return SELECT;
        }
        
        private Select () {
            super();
            putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        }
        
        @Override
        protected void performAction(final Node[] activatedNodes) {
            final Tab proj = Tab.findDefault();
            Tab.RP.post(() -> proj.doSelectNode(activatedNodes[0].getCookie(DataObject.class)));
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return false;
            }
            DataObject dobj = activatedNodes[0].getCookie(DataObject.class);
            if (dobj == null) {
                return false;
            }
            return true;
            /*return Tab.findDefault().containsNode(dobj);*/
          }

        @Override
        public String getName() {
            return NbBundle.getMessage(Select.class, "ACT_Select_Main_Menu"); // NOI18N
        }

        @Override
        protected String iconResource() {
            return "org/netbeans/modules/favorites/resources/actionView.png"; // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

    } // end of Select

    /** Removes root link from favorites
    * @author   Jaroslav Tulach
    */
    private static class Remove extends NodeAction {
        static final long serialVersionUID =-6471281373153172312L;
        /** generated Serialized Version UID */
        //  static final long serialVersionUID = -5280204757097896304L;
        
        private static final String HELP_ID = Remove.class.getName();
        private static final Remove REMOVE = new Remove ();
        
        public static Action getDefault () {
            return REMOVE;
        }
        
        /** Enabled only if the current project is ProjectDataObject.
        */
        @Override
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;

            for (Node arr1 : arr) {
                DataObject shad = arr1.getCookie(DataObject.class);
                //Disable when node is not shadow in Favorites folder.
                if (shad == null || shad.getFolder() != FavoritesNode.getFolder()) {
                    return false;
                }
            }
            return true;
        }

        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        @Override
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_Remove"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Remove.HELP_ID);
        }

        /**
        * Removes the links.
        *
        * @param arr gives array of actually activated nodes.
        */
        @Override
        protected void performAction (Node[] arr) {
            for (Node arr1 : arr) {
                DataObject shad = arr1.getCookie(DataObject.class);
                if (shad != null && shad.getFolder() == FavoritesNode.getFolder()) {
                    try {
                        shad.delete();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

    } // end of Remove
    

    /** Adds something to favorites. Made public so it can be referenced
    * directly from manifest.
    *
    * @author   Jaroslav Tulach
    */
    public static class Add extends NodeAction {
        static final long serialVersionUID =-6471281373153172312L;
        /** generated Serialized Version UID */
        //  static final long serialVersionUID = -5280204757097896304L;
        private static final String HELP_ID = Add.class.getName();
        private static final Add ADD = new Add ();
        
        public static ContextAwareAction getDefault() {
            return ADD;
        }
        
        private Add () {
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        }
        
        /** Enabled only if the current project is ProjectDataObject.
        */
        @Override
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;
            if (arr.length == 1 && arr[0] instanceof FavoritesNode) return true;
                
            

            for (Node arr1 : arr) {
                DataObject dataObject = arr1.getCookie(DataObject.class);
                if (! isAllowed(dataObject))
                    return false;
            }
            return true;
        }
        
        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        @Override
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_Add"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Add.HELP_ID);
        }

        /**
        * Standard perform action extended by actually activated nodes.
        *
        * @param activatedNodes gives array of actually activated nodes.
        */
        @Override
        protected void performAction (final Node[] activatedNodes) {
            try {
                final List<DataObject> toShadows;
                if (activatedNodes.length == 1 && activatedNodes[0] instanceof FavoritesNode) {
                    // show JFileChooser
                    FileObject fo = chooseFileObject();
                    if (fo == null || !VisibilityQuery.getDefault().isVisible(fo)) return;
                    toShadows = Collections.singletonList(DataObject.find(fo));
                } else {
                    toShadows = new ArrayList<>();
                    for (Node node : activatedNodes) {
                        DataObject obj = node.getCookie(DataObject.class);
                        if (obj != null)
                            toShadows.add(obj);
                    }
                }
                Tab.RP.post(() -> addToFavorites(toShadows));
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.INFO, null, e);
            }
        }
        
        /**
         * 
         * @return FileObject or null if FileChooser dialog is cancelled
         */ 
        private static FileObject chooseFileObject() {
            FileObject retVal = null;
            File chooserSelection = null;
            JFileChooser chooser = new JFileChooser ();
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setDialogTitle(NbBundle.getBundle(Actions.class).getString ("CTL_DialogTitle"));
            chooser.setApproveButtonText(NbBundle.getBundle(Actions.class).getString ("CTL_ApproveButtonText"));
            if (currentDir != null) {
                chooser.setCurrentDirectory(currentDir);
            }
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Show the chooser
            if ( option == JFileChooser.APPROVE_OPTION ) {                    
                chooserSelection = chooser.getSelectedFile();
                File selectedFile = FileUtil.normalizeFile(chooserSelection);
                //Workaround for JDK bug #5075580 (filed also in IZ as #46882)
                if (!selectedFile.exists()) {
                    if ((selectedFile.getParentFile() != null) && selectedFile.getParentFile().exists()) {
                        if (selectedFile.getName().equals(selectedFile.getParentFile().getName())) {
                            selectedFile = selectedFile.getParentFile();
                        }
                    }
                }
                //#50482: Check if selected file exists eg. user can enter any file name to text box.
                //#144985: Create new File because of inconsistence in File.exists (JDK bug 6751997)
                if (!new File(selectedFile, "").exists()) {
                    String message = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExist",selectedFile.getPath());
                    String title = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExistDlgTitle");
                    DialogDisplayer.getDefault().notify
                    (new NotifyDescriptor(message,title,NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.CLOSED_OPTION },
                    NotifyDescriptor.OK_OPTION));
                } else {
                    retVal = FileUtil.toFileObject(selectedFile);
                    assert retVal != null;
                }
            }
            currentDir = chooser.getCurrentDirectory();
            return retVal;
        }
        
        public static void selectAfterAddition(final DataObject createdDO) {
            final Tab projectsTab = Tab.findDefault();
            projectsTab.open();
            projectsTab.requestActive();
            //Try to locate newly added node and select it
            if (createdDO != null) {
                Tab.RP.post(() -> {
                    Node [] nodes = projectsTab.getExplorerManager().getRootContext().getChildren().getNodes(true);
                    final Node [] toSelect = new Node[1];
                    boolean setSelected = false;
                    for (Node node : nodes) {
                        if (createdDO.getName().equals(node.getName())) {
                            toSelect[0] = node;
                            setSelected = true;
                            break;
                        }
                    }
                    if (setSelected) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                projectsTab.getExplorerManager().setExploredContextAndSelection(toSelect[0],toSelect);
                            } catch (PropertyVetoException ex) {
                                //Nothing to do
                            }
                        });
                    }
                });
            }
        }

        static DataObject createShadows(final DataFolder favourities, final List<DataObject> dos, final List<DataObject> listAdd) {
            DataObject createdDO = null;
            for (DataObject obj : dos) {
                try {
                    DataShadow added = findShadow(favourities, obj);
                    if (added != null) {
                        if (createdDO == null) {
                            createdDO = added;
                        }
                    } else {
                        if (createdDO == null) {
                            // Select only first node in array added to favorites
                            createdDO = obj.createShadow(favourities);
                            listAdd.add(createdDO);
                        } else {
                            listAdd.add(obj.createShadow(favourities));
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            return createdDO;
        }

        private static DataShadow findShadow (DataFolder f, DataObject dobj) {
            FileObject fo = dobj.getPrimaryFile();
            if (fo != null) {
                DataObject [] arr = f.getChildren();
                for (DataObject arr1 : arr) {
                    if (arr1 instanceof DataShadow) {
                        DataShadow obj = (DataShadow) arr1;
                        if (fo.equals(obj.getOriginal().getPrimaryFile())) {
                            return obj;
                        }
                    }
                }
            }
            return null;
        }

        // what order is this? requires comment
        public static void reorderAfterAddition(final DataFolder favourities, final DataObject[] children, final List<? extends DataObject> listAdd) {
            List<DataObject> listDest = new ArrayList<>();
            if (listAdd.size() > 0) {
                //Insert new nodes just before last (root) node
                DataObject root = null;
                //Find root
                for (DataObject child : children) {
                    FileObject fo = child.getPrimaryFile();
                    if ("Favorites/Root.instance".equals(fo.getPath())) { //NOI18N
                        root = child;
                        break;
                    }
                }
                if (root != null) {
                    for (DataObject child : children) {
                        if (!root.equals(child)) {
                            listDest.add(child);
                        }
                    }
                    listDest.addAll(listAdd);
                    listDest.add(root);
                } else {
                    listDest.addAll(Arrays.asList(children));
                    listDest.addAll(listAdd);
                }
                //Set desired order
                DataObject [] newOrder = listDest.toArray(new DataObject[0]);
                try {
                    favourities.setOrder(newOrder);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        static void addToFavorites(List<DataObject> toShadows) {
            assert !EventQueue.isDispatchThread();
            final DataFolder f = FavoritesNode.getFolder();
            final DataObject[] arr = f.getChildren();
            final List<DataObject> listAdd = new ArrayList<>();
            final DataObject toSelect = createShadows(f, toShadows, listAdd);
            //This is done to set desired order of nodes in view
            reorderAfterAddition(f, arr, listAdd);
            EventQueue.invokeLater(() -> selectAfterAddition(toSelect));
        }

        static boolean isAllowed(DataObject dataObject) {
            //Action is disabled for root folder eg:"/" on Linux or "C:" on Win
            if (dataObject == null) {
                return false;
            }
            FileObject fo = dataObject.getPrimaryFile();
            if (fo != null) {
                //#63459: Do not enable action on internal object/URL.
                if (URLMapper.findURL(fo, URLMapper.EXTERNAL) == null) {
                    return false;
                }
                //Check if it is root.
                if (fo.isRoot()) {
                    //It is root: disable.
                    return false;
                }
            }

            // Fix #14740 disable action on SystemFileSystem.
            try {
                if (dataObject.getPrimaryFile().getFileSystem().isDefault()) {
                    return false;
                }
            } catch (FileStateInvalidException fsie) {
                return false;
            }
            return true;
        }

    } // end of Add
    /** Subclass of Add. Only its display name is different otherwise the same as Add.
    *
    * @author   Marek Slama
    */
    public static class AddOnFavoritesNode extends Add {
        static final long serialVersionUID =-6471284573153172312L;
        
        private static final AddOnFavoritesNode ADD_ON_FAVORITES_NODE = new AddOnFavoritesNode ();
        
        public static ContextAwareAction getDefault () {
            return ADD_ON_FAVORITES_NODE;
        }
        
        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        @Override
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_AddOnFavoritesNode"); // NOI18N
        }
    }
    
}
