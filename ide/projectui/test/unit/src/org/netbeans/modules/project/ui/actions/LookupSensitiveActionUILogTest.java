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

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class LookupSensitiveActionUILogTest extends NbTestCase {

    public LookupSensitiveActionUILogTest(String name) {
        super( name );
    }
            
    private FileObject dir, f1, f2, f3, f4;
    private DataObject d1, d2, d3, d4;
    
    private MyHandler my;
        
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
        
        my = new MyHandler();
        Logger.getLogger("org.netbeans.ui.actions").addHandler(my);
        Logger.getLogger("org.netbeans.ui.actions").setLevel(Level.FINE);
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testMenuPushIsNotified() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
	assertTrue ("TestLSA action is enabled.", tlsa.isEnabled ());
	tlsa.refreshCounter = 0;
        TestPropertyChangeListener tpcl = new TestPropertyChangeListener();
        tlsa.addPropertyChangeListener( tpcl );
        lookup.change(d2);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );
        assertTrue("Action is enabled", tlsa.isEnabled());

        tlsa.setDisplayName("Jarda");
     
        
        JMenuItem item = new JMenuItem(tlsa);
        item.doClick();
        
        assertEquals("One record logged:\n" + my.recs, 1, my.recs.size());
        LogRecord r = my.recs.get(0);
        assertEquals("Menu push", "UI_ACTION_BUTTON_PRESS", r.getMessage());
        assertEquals("four args", 5, r.getParameters().length);
        assertEquals("first is the menu item", item, r.getParameters()[0]);
        assertEquals("second is its class", JMenuItem.class.getName(), r.getParameters()[1]);
        assertEquals("3rd is action", tlsa, r.getParameters()[2]);
        assertEquals("4th its class", tlsa.getClass().getName(), r.getParameters()[3]);
        assertEquals("5th name", "Jarda", r.getParameters()[4]);
        
        tlsa.clear();
        tpcl.clear();
        lookup.change(d3);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );        
    }

    public void testToolbarPushIsNotified() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
	assertTrue ("TestLSA action is enabled.", tlsa.isEnabled ());
	tlsa.refreshCounter = 0;
        TestPropertyChangeListener tpcl = new TestPropertyChangeListener();
        tlsa.addPropertyChangeListener( tpcl );
        lookup.change(d2);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );
        assertTrue("Action is enabled", tlsa.isEnabled());

        tlsa.setDisplayName("Jarda");
     
        JToolBar bar = new JToolBar();
        JButton item = bar.add(tlsa);
        item.doClick();
        
        assertEquals("One record logged:\n" + my.recs, 1, my.recs.size());
        LogRecord r = my.recs.get(0);
        assertEquals("Menu push", "UI_ACTION_BUTTON_PRESS", r.getMessage());
        assertEquals("four args", 5, r.getParameters().length);
        assertEquals("first is the menu item", item, r.getParameters()[0]);
        assertEquals("second is its class", item.getClass().getName(), r.getParameters()[1]);
        assertEquals("3rd is action", tlsa, r.getParameters()[2]);
        assertEquals("4th its class", tlsa.getClass().getName(), r.getParameters()[3]);
        assertEquals("5th name", "Jarda", r.getParameters()[4]);
        
        tlsa.clear();
        tpcl.clear();
        lookup.change(d3);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );        
    }
    
    public void testKeyEventIsNotified() throws Exception {
        TestSupport.ChangeableLookup lookup = new TestSupport.ChangeableLookup();
        TestLSA tlsa = new TestLSA( lookup );
	assertTrue ("TestLSA action is enabled.", tlsa.isEnabled ());
	tlsa.refreshCounter = 0;
        TestPropertyChangeListener tpcl = new TestPropertyChangeListener();
        tlsa.addPropertyChangeListener( tpcl );
        lookup.change(d2);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );
        assertTrue("Action is enabled", tlsa.isEnabled());

        tlsa.setDisplayName("Jarda");
     
        KeyStroke ks = Utilities.stringToKey("C-S");
        
        class MyPanel extends JPanel {
            public void doEvent(KeyEvent ev, KeyStroke ks) {
            
                super.processKeyBinding(ks, ev, JPanel.WHEN_FOCUSED, true);
            }
        }
        
        MyPanel p = new MyPanel();
        p.getInputMap(JComponent.WHEN_FOCUSED).put(ks, "save");
        p.getActionMap().put("save", tlsa);
        KeyEvent ev = new KeyEvent(p, KeyEvent.KEY_TYPED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_UNDEFINED, 'S');
        p.doEvent(ev, ks);
        
        assertEquals("No record logged:\n" + my.recs, 0, my.recs.size());
        /*
        LogRecord r = my.recs.get(0);
        assertEquals("Menu push", "UI_ACTION_BUTTON_PRESS", r.getMessage());
        assertEquals("four args", 5, r.getParameters().length);
//        assertEquals("first is the menu item", item, r.getParameters()[0]);
//        assertEquals("second is its class", item.getClass().getName(), r.getParameters()[1]);
        assertEquals("3rd is action", tlsa, r.getParameters()[2]);
        assertEquals("4th its class", tlsa.getClass().getName(), r.getParameters()[3]);
        assertEquals("5th name", "Jarda", r.getParameters()[4]);
        
        tlsa.clear();
        tpcl.clear();
        lookup.change(d3);
        assertEquals( "Refresh should be called once", 1, tlsa.refreshCounter );
        assertEquals( "One event should be fired", 1, tpcl.getEvents().size() );        
         */
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
	    }
            
        }
        
        public void clear() {
            performCounter = refreshCounter = 0;
        }
        
        
    }
    
    
    private static class TestPropertyChangeListener implements PropertyChangeListener {
        
        List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
        public void propertyChange( PropertyChangeEvent e ) {
            events.add( e );
        }
        
        void clear() {
            events.clear();
        }
        
        List<PropertyChangeEvent> getEvents() {
            return events;
        }
                
    }
        

    private static final class MyHandler extends Handler {
        public final List<LogRecord> recs = new ArrayList<LogRecord>();
        
        public void publish(LogRecord record) {
            recs.add(record);
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
    } // end of MyHandler
}
