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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.netbeans.modules.projectapi.nb.NbProjectInformationProvider;
import org.netbeans.modules.projectapi.nb.NbProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class OpenProjectsTrampolineImplTest extends NbTestCase {
    
    public OpenProjectsTrampolineImplTest(String name) {
        super( name );
    }
    
    
    private FileObject scratch;
    private FileObject goodproject;
    private FileObject goodproject2;
    // private FileObject badproject;
    // private FileObject mysteryproject;
    private ProjectManager pm;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        goodproject = scratch.createFolder("good");
        goodproject.createFolder("testproject");
        goodproject2 = scratch.createFolder("good2");
        goodproject2.createFolder("testproject");
//        badproject = scratch.createFolder("bad");
//        badproject.createFolder("testproject").createData("broken");
//        mysteryproject = scratch.createFolder("mystery");
        MockLookup.setLookup(Lookups.fixed(
            new NbProjectManager(),
            new NbProjectInformationProvider(),
            new NbMutexEventProvider(),
            TestUtil.testProjectFactory()));
        pm = ProjectManager.getDefault();
        OpenProjectList.waitProjectsFullyOpen();
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        goodproject = null;
//        badproject = null;
//        mysteryproject = null;
        pm = null;
        super.tearDown();
    }

    @RandomlyFails
    public void testOpenProjects() throws Exception {
    
        OpenProjectsTrampolineImpl trampoline = new OpenProjectsTrampolineImpl();
        TestPropertyChangeListener tpchl = new TestPropertyChangeListener();
        trampoline.addPropertyChangeListenerAPI( tpchl, this );
        
        Project[] projects = trampoline.getOpenProjectsAPI();
        
        assertEquals( "No project should be open.", 0, projects.length );
        assertEquals( "No events.", 0, tpchl.getEvents().size() );
        
        Project p1 = null;
        
        try {
            p1 = pm.findProject( goodproject );
        } catch ( IOException e ) {
            fail("Should not fail to load goodproject: " + e);
        }
        
        assertNotNull( "Project should not be null", p1 );
        
        OpenProjectList.getDefault().open( p1 );
        projects = trampoline.getOpenProjectsAPI();
        
        assertEquals( "One project should be open.", 1, projects.length );
        assertEquals( "Obe event.", 1, tpchl.getEvents().size() );
        
        Project p2 = null;
        
        try {
            p2 = pm.findProject( goodproject2 );
        } catch ( IOException e ) {
            fail("Should not fail to load goodproject: " + e);
        }
        
        assertNotNull( "Project should not be null", p2 );
        
        OpenProjectList.getDefault().open( p2 );
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 2, projects.length );
        assertEquals( "Two events.", 2, tpchl.getEvents().size() );
        
        OpenProjectList.getDefault().close(new Project[] {p1}, false);
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 1, projects.length );
        assertEquals( "Two events.", 3, tpchl.getEvents().size() );
        
        
        OpenProjectList.getDefault().close(new Project[] {p2}, false);
        projects = trampoline.getOpenProjectsAPI();
        assertEquals( "Two projects should be open.", 0, projects.length );
        assertEquals( "Two events.", 4, tpchl.getEvents().size() );
                
    }

    
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
        @Override
        public void propertyChange( PropertyChangeEvent e ) {
            if ("willOpenProjects".equals(e.getPropertyName())) {
                return;
            }
            events.add( e );
        }
        
        void clear() {
            events.clear();
        }
        
        List<PropertyChangeEvent> getEvents() {
            return events;
        }
                
    }
    
}
