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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class FileActionTest extends NbTestCase {
    
    public FileActionTest(String name) {
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
    private TestSupport.TestProject project2;

    protected @Override void setUp() throws Exception {
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
        project1.setLookup( Lookups.fixed( new Object[] { new TestActionProvider() } ) );  
        
        p2 = TestSupport.createTestProject( workDir, "project2" );
        f2_1 = p2.createData("f2_1.java");
        f2_2 = p2.createData("f2_2.krava");
        d2_1 = DataObject.find(f2_1);
        d2_2 = DataObject.find(f2_2);
               
        project2 = (TestSupport.TestProject)ProjectManager.getDefault().findProject( p2 );
        
    }
    
    public @Override boolean runInEQ() {
        return true;
    }
    
    public void testCommandEnablement() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        FileAction action = new FileAction( "COMMAND", "TestFileCommandAction", (Icon)null, lookup );
        
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change(d1_1);
        assertTrue( "Action should be enabled", action.isEnabled() );        
        
        lookup.change(d1_1, d1_2);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
        lookup.change(d1_1, d2_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );        
        
    }
    
    public void testProviderEnablement() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestActionPerformer tap = new TestActionPerformer();
        FileAction action = new FileAction( tap, "TestFileAction", null,lookup );
     
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
        assertEquals( "enable() should be called on right file: ", f1_1.toString(), tap.fObj.toString() );
        
        tap.clear();
        tap.on( true );
        lookup.change(d1_2);
        assertTrue( "Action should be enabled", action.isEnabled() );        
        assertEquals( "enable() should be called once: ", 1, tap.enableCount );
        assertEquals( "enable() should be called on right file: ", f1_2.toString(), tap.fObj.toString() );
                
        tap.clear();
        tap.on( false );
        lookup.change(d1_1, d2_1);
        assertFalse( "Action should NOT be enabled", action.isEnabled() );
        assertEquals( "enable() should NOT be called: ", 0, tap.enableCount );
        
    }
    
    public void testAgregateRefreshes() throws InterruptedException {
        FileAction action = new FileAction("COMMAND", "TestFileCommandAction", (Icon) null, Lookup.EMPTY);
        final Semaphore s = new Semaphore(0);

        // Lookup that will be queried inside refreshImpl.
        Lookup l = new Lookup() {

            @Override
            public <T> T lookup(Class<T> clazz) {
                if (DataObject.class.equals(clazz) || FileObject.class.equals(clazz)) {
                    s.release();
                }
                return null;
            }

            @Override
            public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
                Class<?> clazz = template.getType();
                if (DataObject.class.equals(clazz) || FileObject.class.equals(clazz)) {
                    s.release();
                }
                return Lookup.EMPTY.lookup(template);
            }
        };
        for (int i = 0; i < 10; i++) {
            action.refresh(l, false); // invoke async refresh
        }

        assertTrue("refreshImpl should be called at least once",
                s.tryAcquire(1, TimeUnit.MINUTES));
        assertFalse("refreshImpl should not be called for each consecutive "
                + "refresh", s.tryAcquire(9, 100, TimeUnit.MILLISECONDS));
    }

    public void testAcceleratorsPropagated() {
        TestSupport.doTestAcceleratorsPropagated(new TestSupport.ActionCreator() {
            public LookupSensitiveAction create(Lookup l) {
                return new FileAction("command", "TestProjectAction", (Icon) null, l);
            }
        }, true);
    }

    public void testGlobalActions() throws Exception {
        final AtomicInteger runCount = new AtomicInteger();
        MockLookup.setInstances(new ActionProvider() {
            public String[] getSupportedActions() {
                return new String[] {"run"};
            }
            public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                DataObject d = context.lookup(DataObject.class);
                return d != null && d.getPrimaryFile().getName().equals("foo");
            }
            public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                runCount.incrementAndGet();
            }
        });
        ContextAwareAction global = (ContextAwareAction) FileSensitiveActions.fileCommandAction("run", "Run {0,choice,0#File|1#\"{1}\"|1<Files}", null);
        DataObject foo = DataObject.find(FileUtil.createMemoryFileSystem().getRoot().createData("foo"));
        DataObject bar = DataObject.find(FileUtil.createMemoryFileSystem().getRoot().createData("bar"));
        Action local = global.createContextAwareInstance(Lookups.fixed(foo));
        assertTrue(local.isEnabled());
        assertEquals("Run \"foo\"", local.getValue("menuText"));
        local.actionPerformed(null);
        assertEquals(1, runCount.get());
        local = global.createContextAwareInstance(Lookups.fixed(bar));
        assertFalse(local.isEnabled());
        assertEquals("Run File", local.getValue("menuText"));
        // XXX could test more complex interactions, e.g. >1 project responds, or file owned by project but project does not respond
    }
    
    private static class TestActionProvider implements ActionProvider {
        
        public String COMMAND = "COMMAND";
        
        private String[] ACTIONS = new String[] { COMMAND };
        
        private List<String> invocations = new ArrayList<String>();
        
        public String[] getSupportedActions() {
            return ACTIONS;
        }
                
        public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                invocations.add( command );
            }            
            else {
                throw new IllegalArgumentException();
            }
            
        }

        public boolean isActionEnabled( String command, Lookup context) throws IllegalArgumentException {
            
            if ( COMMAND.equals( command ) ) {
                Collection dobjs = context.lookupAll(DataObject.class);
                for ( Iterator it = dobjs.iterator(); it.hasNext();  ) {
                    DataObject dobj = (DataObject)it.next();
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
    
    private static class TestActionPerformer implements FileActionPerformer {
        
        private int enableCount;
        private FileObject fObj;
        private boolean on;
        
        public boolean enable( FileObject fo ) {
            enableCount ++;
            this.fObj = fo;
            return on;
        }

        public void perform( FileObject fo ) {
            this.fObj = fo;
        }
        
        public void on( boolean on ) {
            this.on = on;
        }
        
        public void clear() {
            this.fObj = null;
            enableCount = 0;
        }
        
        
    }
    
}
