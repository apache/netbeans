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
