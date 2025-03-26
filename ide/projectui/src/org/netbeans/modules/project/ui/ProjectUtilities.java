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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import static org.netbeans.modules.project.ui.Bundle.*;
import org.netbeans.modules.project.ui.groups.Group;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class ProjectUtilities {
    
    private static final Logger LOG = Logger.getLogger(ProjectUtilities.class.getName());
    
    static final String OPEN_FILES_NS = "http://www.netbeans.org/ns/projectui-open-files/1"; // NOI18N
    static final String OPEN_FILES_NS2 = "http://www.netbeans.org/ns/projectui-open-files/2"; // NOI18N
    static final String OPEN_FILES_ELEMENT = "open-files"; // NOI18N
    static final String FILE_ELEMENT = "file"; // NOI18N
    static final String GROUP_ELEMENT = "group"; // NOI18N
    static final String NAME_ATTR = "name";
    
    
    // support class for xtesting in OpenProjectListTest
    static OpenCloseProjectDocument OPEN_CLOSE_PROJECT_DOCUMENT_IMPL = new OpenCloseProjectDocument () {
        @Override
        public boolean open (FileObject fo) {
            DataObject dobj;
            try {
                dobj = DataObject.find (fo);
            } catch (DataObjectNotFoundException donfo) {
                assert false : "DataObject must exist for " + fo;
                return false;
            }
            EditCookie ec = dobj.getLookup().lookup(EditCookie.class);
            OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
            Openable o = dobj.getLookup().lookup(Openable.class);
            if (ec != null) {
                ec.edit();
            } else if (oc != null) {
                oc.open();
            } else if (o != null) {
                o.open();
            } else {
                LOG.log(Level.INFO, "No EditCookie nor OpenCookie nor Openable for {0}", dobj);
                return false;
            }
            return true;
        }
         
        @Override
        public Map<Project, Set<String>> close(Project[] projects, boolean notifyUI) {
            Map<Project, Set<String>> project2FilesMap = new LinkedHashMap<>();
            List<Project> listOfProjects = Arrays.asList(projects);
            for (Project p : listOfProjects) { //#232668 all projects need an entry in the map - to handle projects without files correctly
                project2FilesMap.put(p, new LinkedHashSet<>());
            }
            Set<DataObject> openFiles = new LinkedHashSet<>();
            List<TopComponent> tc2close = new ArrayList<>();

            LOG.finer("Closing TCs");
            List<TopComponent> openedTC = getOpenedTCs();
            
            for (TopComponent tc : openedTC) {
                DataObject dobj = tc.getLookup().lookup(DataObject.class);

                if (dobj != null) {
                    FileObject fobj = dobj.getPrimaryFile();
                    Project owner = ProjectConvertors.getNonConvertorOwner(fobj);
                    LOG.log(Level.FINER, "Found {0} owned by {1} in {2} of {3}", new Object[] {fobj, owner, tc.getName(), tc.getClass()});

                    Set<String> files = project2FilesMap.get(owner);
                    if (files != null) {
                        if (notifyUI) {
                            openFiles.add(dobj);
                            tc2close.add(tc);
                        } else if (!dobj.isModified()) {
                            // when not called from UI, only include TCs that arenot modified
                            tc2close.add(tc);
                        }
                        files.add(fobj.toURL().toExternalForm());
                    } else if (owner != null) {
                        LOG.log(Level.WARNING, "project association lost, project ({0}) might lose an opened file ({1}) on reopen", new Object[] {owner, fobj});
                    }
                } else {
                    LOG.log(Level.FINE, "#194243: no DataObject in lookup of {0} of {1}", new Object[] {tc.getName(), tc.getClass()});
                }
            }
            if (notifyUI) {
                for (DataObject dobj : DataObject.getRegistry().getModifiedSet()) {
                    FileObject fobj = dobj.getPrimaryFile();
                    Project owner = ProjectConvertors.getNonConvertorOwner(fobj);

                    if (listOfProjects.contains(owner) &&
                        !openFiles.contains(dobj)) {
                        openFiles.add(dobj);
                    }
                }
            }
            if (!notifyUI ||
                (!openFiles.isEmpty() && ExitDialog.showDialog(openFiles))) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        // close documents
                        for (TopComponent tc : tc2close) {
                            tc.close();
                        }
                    }
                };
                if(SwingUtilities.isEventDispatchThread()) {
                    r.run();
                } else {
                    SwingUtilities.invokeLater(r);
                }
            } else {
                // signal that close was vetoed
                if (!openFiles.isEmpty()) {
                    return null;
                }
            }
            return project2FilesMap;
        }

        private List<TopComponent> getOpenedTCs() {
            List<TopComponent> openedTC = new ArrayList<>();
            Runnable onEDT = () -> {
                WindowManager wm = WindowManager.getDefault();
                for (Mode mode : wm.getModes()) {
                    //#84546 - this condituon should allow us to close just editor related TCs that are in any imaginable mode.
                    if (!wm.isEditorMode(mode)) {
                        continue;
                    }
                    LOG.log(Level.FINER, "Closing TCs in mode {0}", mode.getName());
                    openedTC.addAll(Arrays.asList(wm.getOpenedTopComponents(mode)));
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                onEDT.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(onEDT);
                } catch (InterruptedException | InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return openedTC;
        }
    };

    private ProjectUtilities() {}
    
    public static void selectAndExpandProject( final Project p ) {
        
        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater (new Runnable () {
            
            @Override
            public void run () {
                final ProjectTab ptLogial = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);

                Node root = ptLogial.getExplorerManager ().getRootContext ();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node projNode = null;
                for (Node n : root.getChildren().getNodes()) {
                    Project prj = n.getLookup().lookup(Project.class);
                    if (prj != null && prj.getProjectDirectory().equals(p.getProjectDirectory())) {
                        projNode = n;
                        break;
                    }
                }
                if (projNode == null) {
                    // fallback..
                    projNode = root.getChildren ().findChild( ProjectUtils.getInformation( p ).getName() );
                }
                
                if ( projNode != null ) {
                    try {                            
                        ptLogial.getExplorerManager ().setSelectedNodes( new Node[] { projNode } );
                        ptLogial.expandNode( projNode );
                        // ptLogial.open ();
                        // ptLogial.requestActive ();
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
            }
        });
        
    }
    
    /** Invokes the preferred action on given object and tries to select it in
     * corresponding view, e.g. in logical view if possible otherwise
     * in physical project's view.
     * Note: execution this methods can invokes new threads to assure the action
     * is called in EQ.
     *
     * @param newDo new data object
     */   
    public static void openAndSelectNewObject (final DataObject newDo) {
        // call the preferred action on main class
        Mutex.EVENT.writeAccess (new Runnable () {
            @Override
            public void run () {
                final Node node = newDo.getNodeDelegate ();
                Action a = node.getPreferredAction();
                if (a instanceof ContextAwareAction) {
                    a = ((ContextAwareAction) a).createContextAwareInstance(node.getLookup ());
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }

                // next action -> expand && select main class in package view
                final ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                final ProjectTab ptPhysical = ProjectTab.findDefault(ProjectTab.ID_PHYSICAL);
                ProjectTab.RP.post(new Runnable() {
                    public @Override void run() {
                        ProjectTab tab = ptLogical;
                        Node n = tab.findNode(newDo.getPrimaryFile());
                        if (n == null) {
                            tab = ptPhysical;
                            n = tab.findNode(newDo.getPrimaryFile());
                        }
                        if (n != null) {
                            tab.selectNode(n);
                        }
                    }
                });
            }
        });
    }
    
    /** Makes the project tab visible
     * @param requestFocus if set to true the project tab will not only become visible but also
     *        will gain focus
     */
    public static void makeProjectTabVisible() {
        if (Boolean.getBoolean("project.tab.no.selection")) {
            return;
        }
        ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
        ptLogical.open();
        ptLogical.requestActive();
    }
    
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder (null or /-separated)
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @param allowFileSeparator if '/' (and possibly other file separator, see {@link FileUtil#createFolder FileUtil#createFolder})
     *                           is allowed in the newObjectName
     * @return localized error message (HTML-safe) or null if all right
     */    
    @Messages({
        "# {0} - name of the file", "# {1} - an integer representing the invalid characters:", "#       0: both '/' and '\\' are invalid", "#       1: '\\' is invalid", "MSG_not_valid_filename=The filename {0} is not permitted as it contains {1,choice,0#a slash (/) or a backslash (\\)|1#a backslash (\\)}.",
        "# {0} - name of the file", "# {1} - an integer representing the invalid characters:", "#       0: both '/' and '\\' are invalid", "#       1: '\\' is invalid", "MSG_not_valid_folder=The folder name {0} is not permitted as it contains {1,choice,0#a slash (/) or a backslash (\\)|1#a backslash (\\)}.",
        "MSG_fs_or_folder_does_not_exist=The target folder does not exist.",
        "MSG_fs_is_readonly=The target folder is read-only.",
        "# {0} - name of the existing file", "MSG_file_already_exist=The file {0} already exists."
    })
    public static String canUseFileName (FileObject targetFolder, String folderName, String newObjectName,
            String extension, boolean allowFileSeparator, boolean freeFileExtension) {
        assert newObjectName != null; // SimpleTargetChooserPanel.isValid returns false if it is... XXX should it use an error label instead?

        boolean allowSlash = false;
        boolean allowBackslash = false;
        int errorVariant = 0;
        
        if (allowFileSeparator) {
            if (File.separatorChar == '\\') {
                errorVariant = 3;
                allowSlash = allowBackslash = true;
            } else {
                errorVariant = 1;
                allowSlash = true;
            }
        }
        
        if ((!allowSlash && newObjectName.indexOf('/') != -1) || (!allowBackslash && newObjectName.indexOf('\\') != -1)) {
            //if errorVariant == 3, the test above should never be true:
            assert errorVariant == 0 || errorVariant == 1 : "Invalid error variant: " + errorVariant;
            
            return MSG_not_valid_filename(safeEncode(newObjectName), errorVariant);
        }
        
        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return MSG_fs_or_folder_does_not_exist();
        }
        
        // target directory should be writable
        // We should not check this via java.io.File - this breaks not only non-file-based file systems,
        // but can break versioning as well. See issue #251857 (In Remote Favorites tab user can't create new file)
        FileObject targetDir = (folderName != null) ? targetFolder.getFileObject(folderName) : targetFolder;        
        if (targetDir != null) {
            if (targetDir.isValid()&& ! targetDir.canWrite ()) {
                return MSG_fs_is_readonly();
            }
        } else if (! targetFolder.canWrite ()) {
            return MSG_fs_is_readonly();
        }

        // file should not already exist
        StringBuilder relFileName = new StringBuilder();
        if (folderName != null) {
            if (!allowBackslash && folderName.indexOf('\\') != -1) {
                return MSG_not_valid_folder(safeEncode(folderName), 1);
            }
            relFileName.append(folderName);
            relFileName.append('/');
        }
        relFileName.append(newObjectName);
        String ext = "";
        if (extension != null && extension.length() != 0 && (!freeFileExtension || newObjectName.indexOf('.') == -1)) {
            ext = "." + extension;
            relFileName.append(ext);
        }
        if (targetFolder.getFileObject(relFileName.toString()) != null) {
            return MSG_file_already_exist(safeEncode(newObjectName + ext));
        }
        
        // all ok
        return null;
    }
    private static String safeEncode(String text) { // #208432
        if (text.length() > 30) {
            text = text.substring(0, 30) + '…';
        }
        try {
            return XMLUtil.toElementContent(text.replaceAll("\\s+", " "));
        } catch (CharConversionException ex) {
            return text;
        }
    }
    
    
    public static class WaitCursor implements Runnable {
        
        private boolean show;
        
        private WaitCursor( boolean show ) {
            this.show = show;
        }
       
        public static void show() {            
            invoke( new WaitCursor( true ) );
        }
        
        public static void hide() {
            invoke( new WaitCursor( false ) );            
        }
        
        private static void invoke( WaitCursor wc ) {
            if (GraphicsEnvironment.isHeadless()) {
                return;
            }
            if ( SwingUtilities.isEventDispatchThread() ) {
                wc.run();
            }
            else {
                SwingUtilities.invokeLater( wc );
            }
        }
        
        @Override
        public void run() {
            try {            
                JFrame f = (JFrame)WindowManager.getDefault ().getMainWindow ();
                Component c = f.getGlassPane ();
                c.setVisible ( show );
                c.setCursor (show ? Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR) : null);
            } 
            catch (NullPointerException npe) {
                Exceptions.printStackTrace(npe);
            }
        }
    }
    
    /** Closes all documents in editor area which are owned by one of given projects.
     * If some documents are modified then an user is notified by Save/Discard/Cancel dialog.
     * Dialog is showed only once for all project's documents together.
     * URLs of closed documents are stored to <code>private.xml</code>.
     *
     * @param p project to close
     * @return false if the user cancelled the Save/Discard/Cancel dialog, true otherwise
     */    
    public static boolean closeAllDocuments(Project[] projects, boolean notifyUI, String groupName) {
        if (projects == null) {
            throw new IllegalArgumentException ("No projects are specified."); // NOI18N
        }
        
        if (projects.length == 0) {
            // no projects to close, no documents will be closed
            return true;
        }
        
        Map<Project,Set<String>> urls4project = OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.close(projects, notifyUI);

        if (urls4project != null) {
            // store project's documents
            // loop all project being closed
            for (Map.Entry<Project,Set<String>> entry : urls4project.entrySet()) {
                storeProjectOpenFiles(entry.getKey(), new ArrayList<>(entry.getValue()), groupName);
            }
        }
        
        return urls4project != null;
    }
    
    public static void storeProjectOpenFiles(Project p, List<String> urls, String groupName) {

        List<String> openFileUrls = getOpenFilesUrls(p, groupName);
        // check if file list changed, order matters
        if (urls.equals(openFileUrls)) {
            return;
        }

        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(p);
        aux.removeConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);

        Element openFiles = aux.getConfigurationFragment(OPEN_FILES_ELEMENT, OPEN_FILES_NS2, false);
        if (openFiles == null) {
            Document xml = XMLUtil.createDocument (OPEN_FILES_ELEMENT, OPEN_FILES_NS2, null, null);
            openFiles = xml.createElementNS (OPEN_FILES_NS2, OPEN_FILES_ELEMENT);
        }
        NodeList groups = openFiles.getElementsByTagNameNS(OPEN_FILES_NS2, GROUP_ELEMENT);
        for (int i = 0; i < groups.getLength(); i++) {
            Element g = (Element) groups.item(i);
            String attr = g.getAttribute(NAME_ATTR);
            if (attr.equals(groupName) || (attr.equals("") && groupName == null)) {
                openFiles.removeChild(g);
                break;
            }
        }
        Element groupEl = openFiles.getOwnerDocument ().createElementNS(OPEN_FILES_NS2, GROUP_ELEMENT);
        if (groupName != null) {
            groupEl.setAttribute(NAME_ATTR, groupName);
        }
        openFiles.appendChild(groupEl);

        Element fileEl;
        // loop all open files of given project
        for (String url : urls) {
            fileEl = groupEl.getOwnerDocument ().createElementNS(OPEN_FILES_NS2, FILE_ELEMENT);
            fileEl.appendChild(fileEl.getOwnerDocument().createTextNode(url));
            groupEl.appendChild (fileEl);
        }

        aux.putConfigurationFragment (openFiles, false);
    }
    
    /** Opens the project's files read from the private <code>project.xml</code> file
     * 
     * @param p project
     */
    public static Set<FileObject> openProjectFiles (Project p) {
        Group grp = Group.getActiveGroup();
        return openProjectFiles(p, grp);
    }
    
    public static Set<FileObject> openProjectFiles (Project p, Group grp) {
        String groupName = grp == null ? null : grp.getName();
        LOG.log(Level.FINE, "Trying to open files from {0}...", p);
        
        List<String> urls = getOpenFilesUrls(p, groupName);
        Set<FileObject> toRet = new LinkedHashSet<>();
        for (String url : urls) {
            LOG.log(Level.FINE, "Will try to open {0}", url);
            FileObject fo;
            try {
                fo = URLMapper.findFileObject (new URL (url));
            } catch (MalformedURLException mue) {
                assert false : "MalformedURLException in " + url;
                continue;
            }
            if (fo == null || !fo.isValid()) { //check for validity because of issue #238488
                LOG.log(Level.FINE, "Could not find {0}", url);
                continue;
            }
            
            //#109676
            if (ProjectConvertors.getNonConvertorOwner(fo) != p) {
                LOG.log(Level.FINE, "File {0} doesn''t belong to project at {1}", new Object[] {url, p.getProjectDirectory().getPath()});
                continue;
            }
            
            OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (fo);
            toRet.add(fo);
        }
        
        // clean-up stored files -
        // mkleint: I've commented this out as it makes debugging what went wrong when switching groups or exiting the IDE very difficult
        // and now that we have per-group settings stored, removing a single group's values does not pose any real advantage.
        
        //aux.removeConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);
//        openFiles.removeChild(groupEl);
//        if (openFiles.getElementsByTagNameNS(OPEN_FILES_NS2, GROUP_ELEMENT).getLength() > 0) {
//            aux.putConfigurationFragment (openFiles, false);
//        } else {
//            aux.removeConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS2, false);
//        }
        return toRet;
    }
    
    /// Returns an deduplicated list of opened file URLs in encounter order for this project and group.
    private static List<String> getOpenFilesUrls(Project p, String groupName) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(p);
        
        Element openFiles = aux.getConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS2, false);
        if (openFiles == null) {
            return Collections.emptyList();
        }

        Element groupEl = null;
        
        NodeList groups = openFiles.getElementsByTagNameNS(OPEN_FILES_NS2, GROUP_ELEMENT);
        for (int i = 0; i < groups.getLength(); i++) {
            Element g = (Element) groups.item(i);
            String attr = g.getAttribute(NAME_ATTR);
            if (attr.equals(groupName) || (attr.equals("") && groupName == null)) {
                groupEl = g;
                break;
            }
        }
        
        if (groupEl == null) {
            return Collections.emptyList();
        }
        
        NodeList list = groupEl.getElementsByTagNameNS(OPEN_FILES_NS2, FILE_ELEMENT);
        Set<String> set = new LinkedHashSet<>();
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item(i).getChildNodes().item(0).getNodeValue();
            set.add(url);
        }    
        return new ArrayList<>(set);
    }

    // interface for handling project's documents stored in project private.xml
    // it serves for a unit test of OpenProjectList
    interface OpenCloseProjectDocument {
        
        // opens stored document in the document area
        boolean open(FileObject fo);
        
        // closes documents of given projects and returns mapped document's urls by project
        // it's used as base for storing documents in project private.xml
        Map<Project,Set<String>> close(Project[] projects, boolean notifyUI);
    }
    
}
