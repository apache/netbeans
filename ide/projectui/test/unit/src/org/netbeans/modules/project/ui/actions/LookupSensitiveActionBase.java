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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public abstract class LookupSensitiveActionBase extends NbTestCase implements PropertyChangeListener {
    private int change;
    private int ancEvent;
    
    public LookupSensitiveActionBase(String testName) {
        super(testName);
    }            

    
    protected abstract Action create(Lookup context);
    
    protected abstract void enhanceProject(TestSupport.TestProject prj);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * Test of actionPerformed method, of class CloseProject.
     */
    public void testNoNeedToRefreshWhenNotVisiblePopup() throws IOException, InterruptedException {
        doTestRefreshAfterBeingHidden(false, false);
    }
    public void testNoNeedToRefreshWhenNotVisibleMenu() throws IOException, InterruptedException {
        doTestRefreshAfterBeingHidden(false, true);
    }
    public void testCloneNoNeedToRefreshWhenNotVisiblePopup() throws IOException, InterruptedException {
        doTestRefreshAfterBeingHidden(true, false);
    }
    public void testCloneNoNeedToRefreshWhenNotVisibleMenu() throws IOException, InterruptedException {
        doTestRefreshAfterBeingHidden(true, true);
    }
    
    private JMenuItem item(Action a, boolean menu) {
        if (menu) {
            if (a instanceof Presenter.Menu) {
                return ((Presenter.Menu) a).getMenuPresenter();
            } else {
                return new JMenuItem(a);
            }
        } else {
            if (a instanceof Presenter.Popup) {
                return ((Presenter.Popup)a).getPopupPresenter();        
            } else {
                return new JMenuItem(a);
            }
        }
    }
    
    
    private void doTestRefreshAfterBeingHidden(boolean clone, boolean menu) throws IOException {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        
        
        Action instance;
        if (clone) {
            Action a = create(Lookup.EMPTY);
            instance = ((ContextAwareAction)a).createContextAwareInstance(context);
        } else {
            instance = create(context);
        }
        
        if (!(instance instanceof Presenter.Popup)) {
            // cannot test, skipping
            return;
        }
        
        
        CharSequence log1 = Log.enable("org.netbeans.modules.project.ui.actions", Level.FINER);
        assertFalse("Disabled", instance.isEnabled());
        if (!log1.toString().contains("Refreshing")) {
            fail("Should be refreshing: " + log1);
        }
        
        JMenuItem item = item(instance, menu);
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertTrue("Peer created", jmenu.isDisplayable());
        jmenu.getPopupMenu().addNotify();
        assertTrue("Peer for popup", jmenu.getPopupMenu().isDisplayable());
        
        item.addPropertyChangeListener(this);
        jmenu.add(item);
        assertEquals("anncessor properly changes, this means the actions framework is activated", 1, ancEvent);
        
        
        assertFalse("Not enabled", item.isEnabled());
        FileObject pfo = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "yaya");
        FileObject pf2 = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "blabla");
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        Project p = ProjectManager.getDefault().findProject(pfo);
        Project p2 = ProjectManager.getDefault().findProject(pf2);
        if (p instanceof TestSupport.TestProject) {
            enhanceProject((TestSupport.TestProject)p);
        }
        if (p2 instanceof TestSupport.TestProject) {
            enhanceProject((TestSupport.TestProject)p2);
        }
        
        assertNotNull("Project found", p);
        assertNotNull("Project2 found", p2);
        OpenProjects.getDefault().open(new Project[] { p }, false);
        ic.add(p);
        assertTrue("enabled", item.isEnabled());
        assertEquals("One change", 1, change);

        if (menu) {
            item.removeNotify();
            CharSequence log2 = Log.enable("org.netbeans.modules.project.ui.actions", Level.FINER);
            ic.remove(p);
            ic.add(p2);
            if (log2.length() > 0) {
                fail("Nothing shall happen:\n" + log2);
            }
        } // irrelevant for popups
    }

    public void testStackOverFlow() throws IOException {
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        
        boolean clone = false;
        Action instance;
        if (clone) {
            Action a = create(Lookup.EMPTY);
            instance = ((ContextAwareAction)a).createContextAwareInstance(context);
        } else {
            instance = create(context);
        }
        
        FileObject pfo = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "yaya");
        FileObject pf2 = TestSupport.createTestProject(FileUtil.createMemoryFileSystem().getRoot(), "blabla");
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        Project p = ProjectManager.getDefault().findProject(pfo);
        Project p2 = ProjectManager.getDefault().findProject(pf2);
        if (p instanceof TestSupport.TestProject) {
            enhanceProject((TestSupport.TestProject)p);
        }
        if (p2 instanceof TestSupport.TestProject) {
            enhanceProject((TestSupport.TestProject)p2);
        }
        
        assertNotNull("Project found", p);
        assertNotNull("Project2 found", p2);
        OpenProjects.getDefault().open(new Project[] { p }, false);

        assertFalse("Disabled1", instance.isEnabled());
        instance.addPropertyChangeListener(this);
        ic.add(p);
        assertTrue("Enabled", instance.isEnabled());
        assertEquals("One change", 1, change);
        
        
        class Q implements PropertyChangeListener {
            Action i;
            int cnt;
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equals(evt.getPropertyName())) {
                    cnt++;
                    /* What is this for? Often fails during unit tests (but tests pass).
                    assertTrue("enabled in listener", i.isEnabled());
                    */
                }
            }
            
        }
        Q q = new Q();
        q.i = instance;
        
        ic.remove(p);
        
        instance.removePropertyChangeListener(this);
        
        ic.add(p);
        
        instance.addPropertyChangeListener(q);
        assertTrue("Enabled", instance.isEnabled());
        assertEquals("One call", 1, q.cnt);
        
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("ancestor".equals(evt.getPropertyName())) {
            ancEvent++;
            return;
        }
        if ("enabled".equals(evt.getPropertyName())) {
            change++;
            return;
        }
    }

}
