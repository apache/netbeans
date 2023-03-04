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
package org.netbeans.spi.editor;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.editor.lib2.actions.WrapperEditorAction;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Miloslav Metelka
 */
public class AbstractEditorActionTest {
    
    private static final Map SIMPLE_ATTRS = Collections.synchronizedMap(new HashMap());
    static {
        SIMPLE_ATTRS.put(Action.NAME, "my-name");
    }
    private static final Map PRESENTER_ATTRS = Collections.synchronizedMap(new HashMap(SIMPLE_ATTRS));
    static {
        PRESENTER_ATTRS.put(AbstractEditorAction.MENU_TEXT_KEY, "Menu Text");
    }
    
    public AbstractEditorActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetValue() {
        MyAction a = new MyAction(SIMPLE_ATTRS);
        assertEquals("my-name", a.getValue(Action.NAME));
        
        // Test createValue()
        a.createValueKey = "my-key";
        a.createValueValue = "my-value";
        assertEquals(a.createValueValue, a.getValue(a.createValueKey));
    }

    @Test
    public void testPresenters() {
        MyAction a = new MyAction(PRESENTER_ATTRS);
        JMenuItem menuPresenter = a.getMenuPresenter();
        menuPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals("Menu Text", menuPresenter.getText());
        String newMenuText = "Menu Text 2";
        a.putValue(AbstractEditorAction.MENU_TEXT_KEY, newMenuText);
        menuPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals(newMenuText, menuPresenter.getText());

        // Popup presenter
        JMenuItem popupPresenter = a.getPopupPresenter();
        popupPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals(newMenuText, popupPresenter.getText());
        String popupText = "Popup Text";
        a.putValue(AbstractEditorAction.POPUP_TEXT_KEY, popupText);
        popupPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals(popupText, popupPresenter.getText());
        
        // Check menu text translation
        a.putValue(AbstractEditorAction.MENU_TEXT_KEY, "A&BC");
        menuPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals("ABC", menuPresenter.getText());
        assertEquals('B', menuPresenter.getMnemonic());
        
        KeyStroke accel = KeyStroke.getKeyStroke('a');
        a.putValue(Action.ACCELERATOR_KEY, accel);
        menuPresenter.getPreferredSize(); // Ensure pending lazy operation gets completed
        assertEquals(accel, menuPresenter.getAccelerator());
    }

    @Test
    public void testAsynchronous() {
        JTextComponent c = new JEditorPane();
        final MyAction a = new MyAction(SIMPLE_ATTRS);
        ActionEvent evt = new ActionEvent(c, 0, "");
        a.actionPerformed(evt);
        assertSame(Thread.currentThread(), a.actionPerformedThread);
        a.putValue(AbstractEditorAction.ASYNCHRONOUS_KEY, true);
        a.actionPerformedThread = null;
        a.actionPerformed(evt);
        // Add new task to the same RP to verify threading correctness
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                assertNotNull(a.actionPerformedThread);
                assertNotSame(Thread.currentThread(), a.actionPerformedThread);
            }
        });
    }
    
    @Test
    public void testEnabled() {
        Map attrs = new HashMap(SIMPLE_ATTRS);
        attrs.put(AbstractEditorAction.WRAPPER_ACTION_KEY, true);
        final MyAction a = new MyAction(); // No attrs passed
        a.setEnabled(false);
        assertFalse(a.isEnabled());
        attrs.put("delegate", a);
        WrapperEditorAction wrapperAction = WrapperEditorAction.create(attrs);
        assertTrue(wrapperAction.isEnabled());
        JTextComponent c = new JEditorPane();
        ActionEvent evt = new ActionEvent(c, 0, "");
        wrapperAction.actionPerformed(evt);
        assertFalse(wrapperAction.isEnabled());
    }

    @Test
    public void testWrapperAction() {
        Map attrs = new HashMap(SIMPLE_ATTRS);
        attrs.put(AbstractEditorAction.WRAPPER_ACTION_KEY, true);
        final MyAction a = new MyAction(); // No attrs passed
        attrs.put("delegate", a);
        WrapperEditorAction wrapperAction = WrapperEditorAction.create(attrs);
        JTextComponent c = new JEditorPane();
        assertEquals("my-name", wrapperAction.getValue(Action.NAME));
        assertNull(a.getValue(Action.NAME));
        
        ActionEvent evt = new ActionEvent(c, 0, "");
        wrapperAction.actionPerformed(evt);
        assertEquals(Thread.currentThread(), a.actionPerformedThread);

        // Properties transferred
        assertEquals("my-name", a.getValue(Action.NAME));
    }

    public class MyAction extends AbstractEditorAction {

        Thread actionPerformedThread;
        
        String createValueKey;
        
        Object createValueValue;
        
        public MyAction() {
            super();
        }

        public MyAction(Map<String, ?> attrs) {
            super(attrs);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent component) {
            actionPerformedThread = Thread.currentThread();
        }

        @Override
        protected Object createValue(String key) {
            if (key.equals(createValueKey)) {
                return createValueValue;
            }
            return super.createValue(key); //To change body of generated methods, choose Tools | Templates.
        }
        
        
    }
}
