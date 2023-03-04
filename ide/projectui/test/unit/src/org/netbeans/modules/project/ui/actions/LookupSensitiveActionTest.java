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
