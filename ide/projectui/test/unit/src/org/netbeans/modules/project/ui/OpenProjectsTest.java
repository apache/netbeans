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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class OpenProjectsTest extends NbTestCase {

    private FileObject scratch;
    private FileObject testProjectFolder;
    private Project testProject;
    
    public OpenProjectsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        scratch = TestUtil.makeScratchDir(this);
        MockLookup.setInstances(TestUtil.testProjectFactory());
        
        assertNotNull(testProjectFolder = scratch.createFolder("test"));
        assertNotNull(testProjectFolder.createFolder("testproject"));
        
        testProject = ProjectManager.getDefault().findProject(testProjectFolder);
        
        assertNotNull(testProject);
    }

    public void testListenersNotified() throws Exception {
        OpenProjects.getDefault().openProjects().get();

        PropertyChangeListenerImpl l = new PropertyChangeListenerImpl();
        
        OpenProjects.getDefault().addPropertyChangeListener(l);
        
        assertEquals(0, l.events.size());
        
        OpenProjects.getDefault().open(new Project[] {testProject}, false);

        assertEquals(1, l.events.size());
        
        PropertyChangeEvent e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
        assertFalse(Arrays.asList((Project[])e.getOldValue()).contains(testProject));
        assertTrue(Arrays.asList((Project[])e.getNewValue()).contains(testProject));
        
        OpenProjects.getDefault().setMainProject(testProject);
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_MAIN_PROJECT, e.getPropertyName());
        
        OpenProjects.getDefault().setMainProject(null);
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_MAIN_PROJECT, e.getPropertyName());
        
        OpenProjects.getDefault().close(new Project[] {testProject});
        
        assertEquals(1, l.events.size());
        
        e = l.events.remove(0);
        
        assertEquals(OpenProjects.PROPERTY_OPEN_PROJECTS, e.getPropertyName());
        assertTrue(Arrays.asList((Project[])e.getOldValue()).contains(testProject));
        assertFalse(Arrays.asList((Project[])e.getNewValue()).contains(testProject));
    }
    
    public void testPreListenerOpenClose () throws Exception {
        assertEquals("No project is open.", 0, OpenProjects.getDefault ().getOpenProjects ().length); 
        class MyListener implements PropertyChangeListener {
            PropertyChangeEvent preEvent;
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("willOpenProjects".equals(evt.getPropertyName())) {
                    assertNull("Only one event expected", preEvent);
                    preEvent = evt;
                    assertEquals("No open projects yet", 0, OpenProjects.getDefault().getOpenProjects().length);
                }
            }
        }
        MyListener list = new MyListener();
        try {
            OpenProjects.getDefault().addPropertyChangeListener(list);
            OpenProjects.getDefault ().open (new Project[] { testProject }, true);
            assertNotNull("Pre event delivered", list.preEvent);
            assertNull("No old value", list.preEvent.getOldValue());
            assertTrue("Array is new value", list.preEvent.getNewValue() instanceof Project[]);
            Project[] arr = (Project[]) list.preEvent.getNewValue();
            assertEquals("Length is one", 1, arr.length);
            assertEquals("Same as our project", testProject, arr[0]);

            list.preEvent = null;
            OpenProjectList.getDefault().close(new Project[] {testProject}, false);
            assertNull("No pre-event delivered on close", list.preEvent);
        } finally {
            OpenProjects.getDefault().removePropertyChangeListener(list);
        }
    }

    
    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {
        
        private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("willOpenProjects".equals(evt.getPropertyName())) {
                return;
            }
            events.add(evt);
        }
        
    }
    
}
