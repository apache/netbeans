/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
