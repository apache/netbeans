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

package org.netbeans.modules.project.ui.actions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.actions.TestSupport.ActionCreator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class SetMainProjectTest extends NbTestCase {
    
    public SetMainProjectTest(String name) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();

        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();
    }
    
    public void testAcceleratorsPropagated() {
        TestSupport.doTestAcceleratorsPropagated(new ActionCreator() {
            public LookupSensitiveAction create(Lookup l) {
                return new SetMainProject(l);
            }
        }, false);
    }
    
    public void test70368() {
        SetMainProject a = new SetMainProject();
        WeakReference<?> ref = new WeakReference<Object>(a);
        
        a = null;
        
        assertGC("SetMainProject action's instance can be freed:", ref);
    }
    
    public void test70835() throws IOException {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        assertNotNull(workDir);
        
        FileObject f1 = TestSupport.createTestProject (workDir, "project1");
        FileObject f2 = TestSupport.createTestProject (workDir, "project2");
        
        assertNotNull(f1);
        assertNotNull(f2);
        
        Project p1 = ProjectManager.getDefault().findProject(f1);
        Project p2 = ProjectManager.getDefault().findProject(f2);
        
        assertNotNull(p1);
        assertNotNull(p2);
        
        OpenProjectList.getDefault().open(new Project[] {p1, p2}, false);
        
        SetMainProject a = new SetMainProject();
        
        JMenuItem item = a.getMenuPresenter();
        
        assertTrue(item instanceof JMenu);
        
        JMenu menu = (JMenu) item;
        
        item = null;
        
        assertEquals(4, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        WeakReference<?> menuRef = new WeakReference<Object>(menu);
        WeakReference<?> actionRef = new WeakReference<Object>(a);
        
        a = null;
        
        try {
            assertGC("", actionRef);
        } catch (Error e) {
            //ignore....
        }
        
        OpenProjectList.getDefault().close(new Project[] {p1}, false);
        waitForAWT();
        assertEquals(3, menu.getItemCount());
        assertTrue(menu.isEnabled());

        OpenProjectList.getDefault().close(new Project[] {p2}, false);
        waitForAWT();
        assertEquals(2, menu.getItemCount());
        assertFalse(menu.isEnabled());

        OpenProjectList.getDefault().open(new Project[] {p1}, false);
        waitForAWT();
        assertEquals(3, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        menu = null;
        
        assertGC("", menuRef);
        assertGC("", actionRef);
    }

    private void waitForAWT() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
