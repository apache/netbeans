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

package org.netbeans.modules.project.ui;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.RestrictThreadCreation;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Tests storing and reloading project's documents in case of open/close project.
 *
 * @author Jiri Rechtacek
 */
public class ProjectUtilitiesTest extends NbTestCase {

    public static Test suite() {
        final NbTestSuite suite = new NbTestSuite();
        if (!GraphicsEnvironment.isHeadless()) {
            suite.addTest(new ProjectUtilitiesTest("testCloseAllDocuments"));                               //NOI18N
            suite.addTest(new ProjectUtilitiesTest("testSavingModifiedNotOpenedFiles67526"));               //NOI18N
            suite.addTest(new ProjectUtilitiesTest("testCloseAndOpenProjectAndClosedWithoutOpenFiles"));    //NOI18N
            suite.addTest(new ProjectUtilitiesTest("testCanUseFileName"));                                  //NOI18N
            suite.addTest(new ProjectUtilitiesTest("testNavigatorIsNotClosed"));                            //NOI18N
        }
        return suite;
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    private static final String NAVIGATOR_MODE = "navigator";
    
    DataObject do1_1_open, do1_2_open, do1_3_close, do1_4_close;
    DataObject do2_1_open;
    Project project1, project2;
    Set<DataObject> openFilesSet = new HashSet<DataObject>();
    TopComponent tc1_1, tc1_2, tc2_1, tc1_1_navigator;
    
    public ProjectUtilitiesTest (String testName) {
        super (testName);
    }
    
    protected boolean runInEQ () {
        return true;
    }

    protected void setUp () throws Exception {
        super.setUp ();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
                                
        clearWorkDir ();
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        //Mode mode = WindowManager.getDefault ().createWorkspace ("TestHelper").createMode (CloneableEditorSupport.EDITOR_MODE, CloneableEditorSupport.EDITOR_MODE, null);
        
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        FileObject f1_1 = p1.createData("f1_1.java");
        FileObject f1_2 = p1.createData("f1_2.java");
        FileObject f1_3 = p1.createData("f1_3.java");
        FileObject f1_4 = p1.createData("f1_4.java");
        do1_1_open = DataObject.find (f1_1);
        do1_2_open = DataObject.find (f1_2);
        do1_3_close = DataObject.find (f1_3);
        do1_4_close = DataObject.find (f1_4);
        openFilesSet.add (do1_1_open);
        openFilesSet.add (do1_2_open);

        project1 = ProjectManager.getDefault ().findProject (p1);
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        FileObject f2_1 = p2.createData("f2_1.java");
        do2_1_open = DataObject.find (f2_1);

        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        //it will be necessary to dock the top components into the "editor" and "navigator" modes, so they need to be created:
        createMode(CloneableEditorSupport.EDITOR_MODE);
        createMode(NAVIGATOR_MODE);
        (tc1_1 = new SimpleTopComponent (do1_1_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc1_2 = new SimpleTopComponent (do1_2_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc2_1 = new SimpleTopComponent (do2_1_open, CloneableEditorSupport.EDITOR_MODE)).open ();
        (tc1_1_navigator = new SimpleTopComponent2 (do1_1_open, NAVIGATOR_MODE)).open ();
        
        ExitDialog.SAVE_ALL_UNCONDITIONALLY = true;

        RestrictThreadCreation.permitStandard();
        RestrictThreadCreation.permit(OpenProjectList.class.getName() + "$LoadOpenProjects.waitFinished",
                OpenProjectList.class.getName() + "$LoadOpenProjects.resultChanged",
                "org.openide.text.CloneableEditorSupport.prepareDocument",
                "org.openide.text.CloneableEditor.initialize",
                "org.openide.util.lookup.MetaInfServicesLookup.beforeLookup",
                "org.netbeans.modules.project.ui.OpenProjectList.close",
                "org.netbeans.modules.project.ui.OpenProjectList.doOpenProject");
        RestrictThreadCreation.forbidNewThreads(false);
    }
    
    @SuppressWarnings("deprecation")
    private static void createMode(String name) {
        WindowManager.getDefault().getWorkspaces()[0].createMode(name, name, null);
    }

    public void testCloseAllDocuments () {
        closeProjectWithOpenedFiles ();
    }
    
    private void closeProjectWithOpenedFiles () {
        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull ("AuxiliaryConfiguration must be present if project's lookup", aux);

        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS2, false);
        if (openFilesEl != null) {
            assertEquals ("OpenFiles element is empty or null.", 0, openFilesEl.getChildNodes ().getLength ());
        }
        
        Project[] projects = new Project[] {project1};
        
        if (ProjectUtilities.closeAllDocuments(projects, false, null)) {
           // OpenProjectList.getDefault().close(projects, false);
        }
        
        openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS2, false);
        assertNotNull ("OPEN_FILES_ELEMENT found in the private configuration.", openFilesEl);
        
        NodeList list = openFilesEl.getElementsByTagName (ProjectUtilities.FILE_ELEMENT);
        
        assertNotNull ("FILE_ELEMENT must be present", list);
        assertTrue ("Same count of FILE_ELEMENTs and open files, elements count " + list.getLength (), openFilesSet.size () == list.getLength ());
        
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item (i).getChildNodes ().item (0).getNodeValue ();
            FileObject fo = null;
            try {
                fo = URLMapper.findFileObject (new URL (url));
                assertNotNull ("Found file for URL " + url, fo);
                DataObject dobj = DataObject.find (fo);
                assertTrue (dobj + " is present in the set of open files.", openFilesSet.contains (dobj));
                assertNotSame ("The closed file are not present.", do1_3_close, dobj);
                assertNotSame ("The open file of other project is not present.", do2_1_open, dobj);
            } catch (MalformedURLException mue) {
                fail ("MalformedURLException in " + url);
            } catch (DataObjectNotFoundException donfo) {
                fail ("DataObject must exist for " + fo);
            }
        }
        
    }
    
    private void modifyDO(DataObject toModify) throws BadLocationException, IOException {
        System.err.println("toModify = " + toModify );
        EditorCookie ec = toModify.getCookie(EditorCookie.class);
        
        ec.openDocument().insertString(0, "test", null);
    }
    
    public void testSavingModifiedNotOpenedFiles67526() throws BadLocationException, IOException {
        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull ("AuxiliaryConfiguration must be present if project's lookup", aux);
        
        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS2, false);
        if (openFilesEl != null) {
            assertEquals ("OpenFiles element is empty or null.", 0, openFilesEl.getChildNodes ().getLength ());
        }
        
        modifyDO(do1_4_close);
        
        Project[] projects = new Project[] {project1};
        
        if (ProjectUtilities.closeAllDocuments(projects, true, null)) {
     //       OpenProjectList.getDefault().close(projects, true);
        }
        
        assertFalse("the do1_4_close not modified", do1_4_close.isModified());
        
        openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS2, false);
        assertNotNull ("OPEN_FILES_ELEMENT found in the private configuration.", openFilesEl);
        
        NodeList list = openFilesEl.getElementsByTagName (ProjectUtilities.FILE_ELEMENT);
        
        assertNotNull ("FILE_ELEMENT must be present", list);
        assertTrue ("Same count of FILE_ELEMENTs and open files, elements count " + list.getLength (), openFilesSet.size () == list.getLength ());
        
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item (i).getChildNodes ().item (0).getNodeValue ();
            FileObject fo = null;
            try {
                fo = URLMapper.findFileObject (new URL (url));
                assertNotNull ("Found file for URL " + url, fo);
                DataObject dobj = DataObject.find (fo);
                System.err.println("openFilesSet = " + openFilesSet );
                assertTrue (dobj + " is present in the set of open files.", openFilesSet.contains (dobj));
                assertNotSame ("The closed file are not present.", do1_3_close, dobj);
                assertNotSame ("The open file of other project is not present.", do2_1_open, dobj);
            } catch (MalformedURLException mue) {
                fail ("MalformedURLException in " + url);
            } catch (DataObjectNotFoundException donfo) {
                fail ("DataObject must exist for " + fo);
            }
        }
    }

    public void testCloseAndOpenProjectAndClosedWithoutOpenFiles () {
        closeProjectWithOpenedFiles ();
        assertFalse("The project1 is not opened", OpenProjectList.getDefault ().isOpen(project1));

        OpenProjectList.getDefault ().open (project1, false);
        assertTrue("The project1 is opened", OpenProjectList.getDefault ().isOpen(project1));

        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            assertTrue("TopComponent has been closed successfully.", tc.close());
        }
        
        //The OpenProjectList.close calls ProjectUtilities.closeAllDocuments.
        //Calling the ProjectUtilities.closeAllDocuments and OpenProjectList.close
        //causes delete of opened documents -> call just OpenProjectList.close.
        OpenProjectList.getDefault().close(new Project[] {project1}, false);

        AuxiliaryConfiguration aux = project1.getLookup().lookup(AuxiliaryConfiguration.class);
        Element openFilesEl = aux.getConfigurationFragment (ProjectUtilities.OPEN_FILES_ELEMENT, ProjectUtilities.OPEN_FILES_NS2, false);
        assertTrue ("Non empty OPEN_FILES_ELEMENT found in the private configuration.",
            openFilesEl == null ||
            openFilesEl.getElementsByTagName (ProjectUtilities.FILE_ELEMENT).getLength() == 0);
        
        assertFalse ("Project1 must be closed.", OpenProjectList.getDefault ().isOpen (project1));
    }
    
    public void testCanUseFileName() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        FileObject p1 = d.getFileObject("project1");
        assertNotNull(p1);
        assertNull("normal file addition", ProjectUtilities.canUseFileName(p1, null, "foo", "java", false, false));
        assertNull("normal file addition with no extension is OK", ProjectUtilities.canUseFileName(p1, null, "foo", null, false, false));
        assertNull("normal file addition in an existing subdir", ProjectUtilities.canUseFileName(d, "project1", "foo", "java", false, false));
        assertNull("normal file addition in a new subdir", ProjectUtilities.canUseFileName(d, "dir", "foo", "java", false, false));
        //assertNotNull("no target name", ProjectUtilities.canUseFileName(d, "dir", null, "java"));
        assertNotNull("no target folder", ProjectUtilities.canUseFileName(null, "dir", "foo", "java", false, false));
        assertNotNull("file already exists", ProjectUtilities.canUseFileName(p1, null, "f1_1", "java", false, false));
        assertNotNull("file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", "java", false, false));
        assertNull("similar file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", "properties", false, false));
        assertNull("similar file already exists in subdir", ProjectUtilities.canUseFileName(d, "project1", "f1_1", null, false, false));
        d = new XMLFileSystem().getRoot();
        assertNotNull("FS is r/o", ProjectUtilities.canUseFileName(d, null, "foo", "java", false, false));
        // #59876: deal with non-disk-based filesystems sensibly
        d = FileUtil.createMemoryFileSystem().getRoot();
        d.createData("bar.java");
        FileUtil.createData(d, "sub/dir/foo.java");
        assertNull("can create file in non-disk FS", ProjectUtilities.canUseFileName(d, null, "foo", "java", false, false));
        assertNotNull("file already exists", ProjectUtilities.canUseFileName(d, null, "bar", "java", false, false));
        assertNotNull("file already exists in subsubdir", ProjectUtilities.canUseFileName(d, "sub/dir", "foo", "java", false, false));
        assertNull("can otherwise create file in subsubdir", ProjectUtilities.canUseFileName(d, "sub/dir", "bar", "java", false, false));
        //#66792: allow to create whole directory tree at once using Folder Template:
        assertNull("can create directory subtree", ProjectUtilities.canUseFileName(d, null, "a/b/c", null, true, false));
        //#59654: do not allow slash and backslash for common templates:
        assertNotNull("cannot create file with slashes", ProjectUtilities.canUseFileName(d, null, "a/b/c", "txt", false, false));
        assertNotNull("cannot create file with backslashes", ProjectUtilities.canUseFileName(d, null, "a\\b\\c", "txt", false, false));
        // Check freeFileExtension mode:
        assertNull(ProjectUtilities.canUseFileName(d, null, "foo", "java", false, true));
        assertNotNull(ProjectUtilities.canUseFileName(d, null, "bar", "java", false, true));
        assertNotNull(ProjectUtilities.canUseFileName(d, null, "bar.java", "java", false, true));
        assertNull(ProjectUtilities.canUseFileName(d, null, "bar.java", "java", false, false));
        String err = ProjectUtilities.canUseFileName(d, null, "<a href='whatever'>HTML\njunk!</a>", "html", false, false);
        assertNotNull(err);
        assertTrue(err, err.contains("&lt;a href='whatever'>HTML junk!&lt;…"));
    }
    
    public void testNavigatorIsNotClosed() throws Exception {
        closeProjectWithOpenedFiles ();
        
        final boolean[] tc1_1isOpened = new boolean[] {true};
        final boolean[] tc1_2isOpened = new boolean[] {true};
        final boolean[] tc1_1_navigatorisOpened = new boolean[] {false};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                tc1_1isOpened[0] = tc1_1.isOpened();
                tc1_2isOpened[0] = tc1_2.isOpened();
                tc1_1_navigatorisOpened[0] = tc1_1_navigator.isOpened();
            }
        };
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
        long l = System.currentTimeMillis();
        while(!tc1_1_navigatorisOpened[0]) {
            Thread.sleep(200);
            if(System.currentTimeMillis() - l > 10000) {
                fail("testNavigatorIsNotClosed timeout");
            }
        }
        assertFalse(tc1_1isOpened[0]);
        assertFalse(tc1_2isOpened[0]);
        assertTrue(tc1_1_navigatorisOpened[0]);
    }

    private static class SimpleTopComponent extends CloneableTopComponent {
        private Object content;
        private String modeToDockInto;
        public SimpleTopComponent (Object obj, String modeToDockInto) {
            this.content = obj;
            this.modeToDockInto = modeToDockInto;
            setName (obj.toString ());
        }
        
        public Lookup getLookup () {
            return Lookups.singleton (content);
        }
        
        public void open() {
            super.open();
            WindowManager.getDefault().findMode(modeToDockInto).dockInto(this);
        }
    }
    
    private static class SimpleTopComponent2 extends TopComponent {
        private Object content;
        private String modeToDockInto;
        public SimpleTopComponent2 (Object obj, String modeToDockInto) {
            this.content = obj;
            this.modeToDockInto = modeToDockInto;
            setName (obj.toString ());
        }
        
        public Lookup getLookup () {
            return Lookups.singleton (content);
        }
        
        public void open() {
            super.open();
            WindowManager.getDefault().findMode(modeToDockInto).dockInto(this);
        }
    }
    
}
