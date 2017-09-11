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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public class LookupSensitiveActionTest extends NbTestCase {

    public LookupSensitiveActionTest(String name) {
        super( name );
    }

    protected boolean isEnabled(Action action) {
        assertTrue("Is AWT thread", EventQueue.isDispatchThread());
        return action.isEnabled();
    }
    
    protected Object getValue(Action action, String key) {
        assertTrue("Is AWT thread", EventQueue.isDispatchThread());
        return action.getValue(key);
    }
            
    private FileObject dir, f1, f2, f3, f4;
    private DataObject d1, d2, d3, d4;
        
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        dir = FileUtil.toFileObject(getWorkDir());
        f1 = dir.createData("f1.java");
        f2 = dir.createData("f2.java");
        f3 = dir.createData("f3.properties");
        f4 = dir.createData("f4.xml");          
        d1 = DataObject.find(f1);
        d2 = DataObject.find(f2);
        d3 = DataObject.find(f3);
        d4 = DataObject.find(f4);
    }
    
    @Override
    public boolean runInEQ () {
        return true;
    }
    
    public void testListening() throws Exception {
    
        // Lookup sensitive action has to refresh if and only if
        // it has at least one property change listener
        
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
	assertFalse("TestLSA has no DataObject and is disabled", isEnabled(tlsa));
	tlsa.refreshCounter = 0;
	
        lookup.change(d1);
        assertEquals( "No refresh should be called ", 0, tlsa.refreshCounter );
        lookup.change(d2);
        lookup.change(d1);
        assertEquals( "No refresh should be called ", 0, tlsa.refreshCounter );
	assertTrue("TestLSA action is enabled.", isEnabled(tlsa));
        assertEquals( "One check is needed", 1, tlsa.refreshCounter );
        
                
        TestPropertyChangeListener tpcl = new TestPropertyChangeListener();
        tlsa.addPropertyChangeListener( tpcl );
        assertEquals( "Listener does not trigger any checks", 1, tlsa.refreshCounter );
        tlsa.refreshCounter = 0;
        
        lookup.change(d2);
        assertEquals( "Refresh should be called once more", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );
        
        assertTrue("Enabled2", isEnabled(tlsa));
        
        tlsa.clear();
        tpcl.clear();
        lookup.change(d3);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );        
        
        lookup.change();
        assertFalse("Enabled3.1", isEnabled(tlsa));
        tlsa.removePropertyChangeListener( tpcl );
        assertFalse("Enabled3.2", isEnabled(tlsa));

        tlsa.clear();
        tpcl.clear();
        lookup.change(d2);
        assertEquals( "Refresh should not be called", 0, tlsa.refreshCounter );
        assertEquals( "No One event should be fired", 0, tpcl.getEvents().size() );        

        assertTrue("Enabled4", isEnabled(tlsa));
        
        assertEquals( "Refresh should be called now", 1, tlsa.refreshCounter );
        assertEquals( "No listener no event", 0, tpcl.getEvents().size() );        
        
    }
    
    public void testCorrectValuesWithoutListener() throws Exception {
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
        
        lookup.change(d1);
        assertEquals( "Action should return correct name ", d1.getName(), getValue(tlsa, Action.NAME ) );
        
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        
        assertEquals( "Action should return correct name ", d1.getName(), getValue(tlsa, Action.NAME ) );        
        assertEquals( "Refresh should still be called only once", 1, tlsa.refreshCounter );
        
    }
    
    public void testActionGC() throws Exception {
        
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
        
        WeakReference<?> reference = new WeakReference<Object>(tlsa);
        tlsa = null;
        
        assertGC( "Action should be GCed", reference );
        
    }
    
    
    private static class TestLSA extends LookupSensitiveAction {
        
        private int performCounter;
        private int refreshCounter;
               
        public TestLSA( Lookup lookup ) {
            super( null, lookup, new Class[] { DataObject.class } );
        }
        
        protected void actionPerformed( Lookup context ) {
            performCounter++;
        }
           
        protected @Override void refresh(Lookup context, boolean immediate) {
            refreshCounter++;
            
            DataObject dobj = context.lookup(DataObject.class);
            
            if (dobj != null) {
		putValue( Action.NAME, dobj.getName() );
                setEnabled(true);
	    } else {
                setEnabled(false);
            }
            
        }
        
        public void clear() {
            performCounter = refreshCounter = 0;
        }
        
        
    }
    
    
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
        public void propertyChange( PropertyChangeEvent e ) {
            assertTrue("Changes in action state need to be notified in AWT thread", EventQueue.isDispatchThread());
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
