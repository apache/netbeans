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

package org.netbeans.modules.search.project;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach
 */
public class OpenProjectCLITest extends NbTestCase {
    File dir;
    FileObject fo;
    
    public OpenProjectCLITest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    protected void setUp() throws Exception {
        dir = new File(getWorkDir(), "tstdir");
        dir.mkdirs();
        File nb = new File(dir, "nbproject");
        nb.mkdirs();

        MockServices.setServices(DD.class, MockNodeOperation.class, MockProjectFactory.class);
        MockNodeOperation.explored = null;
        
        fo = FileUtil.toFileObject(dir);
        assertTrue("This is a project folder", ProjectManager.getDefault().isProject(fo));
        
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        assertEquals("No open projects", 0, OpenProjects.getDefault().getOpenProjects().length);
        DD.prev = null;
        DD.cnt = 0;
    }

    protected void tearDown() throws Exception {
    }
    
    public void testOpenProjectFolder() throws Exception {
        CommandLine.getDefault().process(new String[] { "--open", dir.getPath() });
        assertNull("No explorer called", MockNodeOperation.explored);

        Project p = OpenProjects.getDefault().getMainProject();
        assertNotNull("There is a main project", p);
        if (!(p instanceof MockProject)) {
            fail("Wrong project: " + p);
        }
        MockProject mp = (MockProject)p;
        
        assertEquals("It is our dir", fo, mp.p);
    }

    public void testOpenBrokenAndProjectFolder() throws Exception {
        File nonExist = new File(dir, "IDoNotExist");
        assertFalse(nonExist.exists());
        File nonExist2 = new File(dir, "IDoNotExistEither");
        assertFalse(nonExist2.exists());
        
        try {
            CommandLine.getDefault().process(new String[] { "--open", nonExist2.getPath(), nonExist.getPath(), dir.getPath() });
            fail("One project does not exists, should fail");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf(nonExist.getName()) == -1) {
                fail("Messages shall contain " + nonExist + "\n" + ex.getMessage());
            }
        }
        assertNull("No explorer called", MockNodeOperation.explored);

        Project p = OpenProjects.getDefault().getMainProject();
        assertNotNull("There is a main project", p);
        if (!(p instanceof MockProject)) {
            fail("Wrong project: " + p);
        }
        MockProject mp = (MockProject)p;
        
        assertEquals("It is our dir", fo, mp.p);
        
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
        
        assertNotNull("Failure notified", DD.prev);
        assertEquals("Just once", 1, DD.cnt);
    }
    
    public static final class DD extends DialogDisplayer {
        static int cnt;
        static NotifyDescriptor prev;

        public Object notify(NotifyDescriptor descriptor) {
            cnt++;
            prev = descriptor;
            return NotifyDescriptor.OK_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static final class MockNodeOperation extends NodeOperation {
        public static Node explored;
        
        public boolean customize(Node n) {
            fail("No customize");
            return false;
        }

        public void explore(Node n) {
            assertNull("No explore before", explored);
            explored = n;
        }

        public void showProperties(Node n) {
            fail("no props");
        }

        public void showProperties(Node[] n) {
            fail("no props");
        }

        public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top) throws UserCancelException {
            fail("no select");
            return null;
        }
    }
    
    public static final class MockProjectFactory implements ProjectFactory {
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.isFolder();
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return new MockProject(projectDirectory);
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }
    }
    
    private static final class MockProject implements Project {
        private final FileObject p;
        
        public MockProject(FileObject p) {
            this.p = p;
        }
        
        public FileObject getProjectDirectory() {
            return p;
        }

        public Lookup getLookup() {
            return Lookups.singleton(this);
        }
        
    }
}
