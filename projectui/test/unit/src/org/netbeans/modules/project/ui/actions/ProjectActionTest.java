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

package org.netbeans.modules.project.ui.actions;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class ProjectActionTest extends NbTestCase {
    
    public ProjectActionTest(String name) {
        super( name );
    }

    private FileObject p1;
    private FileObject p2;
    private FileObject f1_1; 
    private FileObject f1_2; 
    private FileObject f2_1;
    private FileObject f2_2;
    private DataObject d1_1; 
    private DataObject d1_2;
    private DataObject d2_1;
    private DataObject d2_2;
    private TestSupport.TestProject project1;
    private TestActionProvider tap1;
    private TestSupport.TestProject project2;

    @Override protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("sync.project.execution", "true");
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
    
        
        p1 = TestSupport.createTestProject( workDir, "project1" );
        f1_1 = p1.createData("f1_1.java");
        f1_2 = p1.createData("f1_2.krava");
        d1_1 = DataObject.find(f1_1);
        d1_2 = DataObject.find(f1_2);
               
        project1 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p1 );
        tap1 = new TestActionProvider();
        project1.setLookup(Lookups.singleton(tap1));
        
        p2 = TestSupport.createTestProject( workDir, "project2" );
        f2_1 = p2.createData("f2_1.java");
        f2_2 = p2.createData("f2_2.krava");
        d2_1 = DataObject.find(f2_1);
        d2_2 = DataObject.find(f2_2);
               
        project2 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p2 );                
    }

    private static void assertEnablement(final Action action, final boolean enabled) throws Exception {
        LookupSensitiveAction.RP.post(new Runnable() {public @Override void run() {}}).waitFinished();
        assertTrue(action.isEnabled() ^ !enabled);
    }
    
    public void testCommandEnablement() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        Action action = new ProjectAction("COMMAND", "TestProjectAction", null, lookup);
        action.isEnabled(); // priming check
        assertEnablement(action, false);
        lookup.change(d1_1);
        assertEnablement(action, true);
        lookup.change(d1_1, d1_2);
        assertEnablement(action, false);
        lookup.change(d1_1, d2_1);
        assertEnablement(action, false);
        TestActionProvider tap2 = new TestActionProvider();
        project2.setLookup(Lookups.singleton(tap2));
        lookup.change(d2_1);
        assertEnablement(action, true);
        lookup.change(d1_1, d2_1);
        assertEnablement(action, true);
        action.actionPerformed(null);
        assertEquals("[COMMAND]", tap1.invocations.toString());
        assertEquals("[COMMAND]", tap2.invocations.toString());
        tap1.listenerSuccess = true;
        tap2.listenerSuccess = true;
        action.actionPerformed(null);
        assertEquals("[COMMAND, COMMAND]", tap1.invocations.toString());
        assertEquals("[COMMAND, COMMAND]", tap2.invocations.toString());
        tap1.listenerSuccess = false;
        action.actionPerformed(null);
        assertEquals("[COMMAND, COMMAND, COMMAND]", tap1.invocations.toString());
        assertEquals("[COMMAND, COMMAND]", tap2.invocations.toString());
    }
    
    public void testProviderEnablement() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestActionPerformer tap = new TestActionPerformer();
        ProjectAction action = new ProjectAction( tap, "TestProjectAction", null,lookup );
     
        assertFalse( "Action should NOT be enabled", action.isEnabled() );                
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
        tap.clear();
        tap.on( true );
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
        tap.clear();
        tap.on( false );
        lookup.change(d1_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should be called once: ", 1, tap.enableCount );
        assertEquals( "enable() should be called on right project: ", project1.toString(), tap.project.toString() );
        
        tap.clear();
        tap.on( true );
        lookup.change(d1_2);
        assertTrue( "Action should be enabled", action.isEnabled() );        
        assertEquals( "enable() should be called once: ", 1, tap.enableCount );
        assertEquals( "enable() should be called on right project: ", project1.toString(), tap.project.toString() );
                
        tap.clear();
        tap.on( false );
        lookup.change(d1_1, d2_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
    }
    
    public void testAcceleratorsPropagated() {
        TestSupport.doTestAcceleratorsPropagated(new TestSupport.ActionCreator() {
            public LookupSensitiveAction create(Lookup l) {
                return new ProjectAction("command", "TestProjectAction", null, l);
            }
        }, true);
    }
    
    private static class TestActionProvider implements ActionProvider {
        
        public String COMMAND = "COMMAND";
        
        private String[] ACTIONS = new String[] { COMMAND };
        
        private List<String> invocations = new ArrayList<String>();
        Boolean listenerSuccess;
        
        public String[] getSupportedActions() {
            return ACTIONS;
        }
                
        public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                invocations.add( command );
                if (listenerSuccess != null) {
                    ActionProgress.start(context).finished(listenerSuccess);
                }
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        public boolean isActionEnabled( String command, Lookup context) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                for (DataObject dobj : context.lookupAll(DataObject.class)) {
                    if ( !dobj.getPrimaryFile().getNameExt().endsWith( ".java" ) ) {
                        return false;
                    }                    
                }
                return true;
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        
    }
    
    private static class TestActionPerformer implements ProjectActionPerformer {
        
        private int enableCount;
        private Project project;
        private boolean on;
        
        public boolean enable( Project project ) {
            enableCount ++;
            this.project = project;
            return on;
        }

        public void perform( Project project ) {
            this.project = project;
        }
        
        public void on( boolean on ) {
            this.on = on;
        }
        
        public void clear() {
            this.project = null;
            enableCount = 0;
        }
        
        
    }
        
}
