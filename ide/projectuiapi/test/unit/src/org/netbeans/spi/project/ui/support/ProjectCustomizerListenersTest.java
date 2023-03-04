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
package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;

import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Test of OK, Store and Close listeners of ProjectCustomzier dialog
 * 
 * @author Milan Kubec, Petr Somol
 */
public class ProjectCustomizerListenersTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ProjectCustomizerListenersTest.class);
    }

    private List<EventRecord> events = new ArrayList<EventRecord>();
    private enum LType { OK, STORE, CLOSE };
    
    public ProjectCustomizerListenersTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestDialogDisplayer.class);
        events.clear();
    }
    
    //skip this tests, something wrong with the setup, results differ on jdk 1.7 and 1.8. 
    //technically 1.8 results are correct (equal to what can be observed in the IDE)
    // see issue 238102
    public void donottestAllListeners() {
        
        Category testCat1 = Category.create("test1", "test1", null);
        final Category testCat2 = Category.create("test2", "test2", null, testCat1);
        final Category testCat3 = Category.create("test3", "test3", null);
        
        testCat1.setOkButtonListener(new Listener(LType.OK, "testCat1", true));
        testCat1.setStoreListener(new Listener(LType.STORE, "testCat1", false));
        testCat1.setCloseListener(new Listener(LType.CLOSE, "testCat1", true));
        testCat2.setOkButtonListener(new Listener(LType.OK, "testCat2", true));
        testCat2.setStoreListener(new Listener(LType.STORE, "testCat2", false));
        testCat2.setCloseListener(new Listener(LType.CLOSE, "testCat2", true));
        testCat3.setOkButtonListener(new Listener(LType.OK, "testCat3", true));
        testCat3.setStoreListener(new Listener(LType.STORE, "testCat3", false));
        testCat3.setCloseListener(new Listener(LType.CLOSE, "testCat3", true));
        
        final Listener mainOKListener = new Listener(LType.OK, "Properties", true);
        final Listener mainStoreListener = new Listener(LType.STORE, "Properties", false);
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Dialog dialog = ProjectCustomizer.createCustomizerDialog(new Category[]{ testCat2, testCat3 }, 
                        new CategoryComponentProviderImpl(), null, mainOKListener, mainStoreListener, 
                        HelpCtx.DEFAULT_HELP);
                    dialog.pack();
                    dialog.setVisible(true);
                    dialog.dispose();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // wait until all events are delivered
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
             ex.printStackTrace();
        }
        
//        for (EventRecord er : events) {
//            System.out.println(er);
//        }
        
        assertEquals(14, events.size());
        assertEquals(new EventRecord(LType.OK, "Properties", 0), events.get(0));
        assertEquals(new EventRecord(LType.OK, "testCat2", 0), events.get(1));
        assertEquals(new EventRecord(LType.OK, "testCat1", 0), events.get(2));
        assertEquals(new EventRecord(LType.OK, "testCat3", 0), events.get(3));
        // CLOSE and STORE events can theoretically be intermixed
        Map<String, Integer> event = new HashMap<String, Integer>();
        for(int i = 4; i < 14; i++) {
            EventRecord er = events.get(i);
            assertNotNull(er);
            assertTrue(er.getType() != LType.OK);
            Integer count = event.get(er.getTypeAndId());
            if(count == null) {
                event.put(er.getTypeAndId(), 1);
            } else {
                event.put(er.getTypeAndId(), count + 1);
            }
        }
        assertEquals(7, event.size());
        assertNotNull(event.get((new EventRecord(LType.STORE, "Properties", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.STORE, "testCat2", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.STORE, "testCat1", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.STORE, "testCat3", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.CLOSE, "testCat2", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.CLOSE, "testCat1", 0)).getTypeAndId()));
        assertNotNull(event.get((new EventRecord(LType.CLOSE, "testCat3", 0)).getTypeAndId()));
        assertEquals(1, event.get((new EventRecord(LType.STORE, "Properties", 0)).getTypeAndId()).intValue());
        assertEquals(1, event.get((new EventRecord(LType.STORE, "testCat2", 0)).getTypeAndId()).intValue());
        assertEquals(1, event.get((new EventRecord(LType.STORE, "testCat1", 0)).getTypeAndId()).intValue());
        assertEquals(1, event.get((new EventRecord(LType.STORE, "testCat3", 0)).getTypeAndId()).intValue());
        assertEquals(2, event.get((new EventRecord(LType.CLOSE, "testCat2", 0)).getTypeAndId()).intValue());
        assertEquals(2, event.get((new EventRecord(LType.CLOSE, "testCat1", 0)).getTypeAndId()).intValue());
        assertEquals(2, event.get((new EventRecord(LType.CLOSE, "testCat3", 0)).getTypeAndId()).intValue());
    }
    
    public void testOKListener() {
        
        Category testCat1 = Category.create("test1", "test1", null);
        final Category testCat2 = Category.create("test2", "test2", null, testCat1);
        final Category testCat3 = Category.create("test3", "test3", null);
        
        testCat1.setOkButtonListener(new Listener(LType.OK, "testCat1", true));
        testCat2.setOkButtonListener(new Listener(LType.OK, "testCat2", true));
        testCat3.setOkButtonListener(new Listener(LType.OK, "testCat3", true));
        
        final Listener mainOKListener = new Listener(LType.OK, "Properties", true);
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    ProjectCustomizer.createCustomizerDialog(new Category[]{ testCat2, testCat3 }, 
                        new CategoryComponentProviderImpl(), null, mainOKListener, 
                        HelpCtx.DEFAULT_HELP);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // wait until all events are delivered
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
             ex.printStackTrace();
        }
        
//        for (EventRecord er : events) {
//            System.out.println(er);
//        }
        
        assertEquals(4, events.size());
        assertEquals(new EventRecord(LType.OK, "Properties", 0), events.get(0));
        assertEquals(new EventRecord(LType.OK, "testCat2", 0), events.get(1));
        assertEquals(new EventRecord(LType.OK, "testCat1", 0), events.get(2));
        assertEquals(new EventRecord(LType.OK, "testCat3", 0), events.get(3));
        
    }
    
    private class Listener implements ActionListener {
        
        private LType type;
        private String id;
        private long when;
        private boolean inEQ;
        
        public Listener(LType type, String id, boolean inEQ) {
            this.type = type;
            this.id = id;
            this.inEQ = inEQ;
        }
        
        public void actionPerformed(ActionEvent e) {
            when = System.nanoTime();
            events.add(new EventRecord(type, id, when));
            if (inEQ) {
                assertTrue(SwingUtilities.isEventDispatchThread());
            } else {
                assertFalse(SwingUtilities.isEventDispatchThread());
            }
        }
        
    }
    
    public static final class TestDialogDisplayer extends DialogDisplayer {
        
        public Object notify(NotifyDescriptor descriptor) {
            return null;
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            Object[] options = descriptor.getOptions();
            if (options[0] instanceof JButton) {
                ((JButton) options[0]).doClick();
            }
            return new JDialog();
        }
        
    }
    
    private static final class CategoryComponentProviderImpl implements CategoryComponentProvider {
        public JComponent create(Category category) {
            return new JPanel();
        }
    }
    
    private static final class EventRecord {
        
        public LType type;
        public String id;
        public long when;
        
        public EventRecord(LType type, String id, long when) {
            this.type = type;
            this.id = id;
            this.when = when;
        }
        
        public LType getType() {
            return type;
        }
        
        public String getId() {
            return id;
        }

        public long getWhen() {
            return when;
        }

        public String getTypeAndId() {
            return type + ", " + id;
        }

        @Override
        public String toString() {
            return type + ", " + id + ", " + when;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EventRecord) {
                return (type.equals(((EventRecord) obj).type)) && 
                       (id.equals(((EventRecord) obj).id));
            }
            return false;
        }
        
    }
    
}
