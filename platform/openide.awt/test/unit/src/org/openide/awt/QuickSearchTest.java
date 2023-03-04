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
package org.openide.awt;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;

/**
 * Test of QuickSearch.
 * 
 * @author Martin Entlicher
 */
public class QuickSearchTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(QuickSearchTest.class);
    }

    public QuickSearchTest(String name) {
        super(name);
    }
    
    /**
     * Test of attach and detach methods, of class QuickSearch.
     */
    public void testAttachDetach() {
        TestComponent component = new TestComponent();
        Object constraints = null;
        QuickSearch qs = QuickSearch.attach(component, constraints, new DummyCallback());
        assertEquals("One added key listener is expected after attach", 1, component.addedKeyListeners.size());
        assertTrue(qs.isEnabled());
        assertFalse(qs.isAlwaysShown());
        qs.detach();
        assertEquals("No key listener is expected after detach", 0, component.addedKeyListeners.size());
    }

    /**
     * Test of isEnabled and setEnabled methods, of class QuickSearch.
     */
    public void testIsEnabled() {
        TestComponent component = new TestComponent();
        Object constraints = null;
        QuickSearch qs = QuickSearch.attach(component, constraints, new DummyCallback());
        assertTrue(qs.isEnabled());
        qs.setEnabled(false);
        assertEquals("No key listener is expected after setEnabled(false)", 0, component.addedKeyListeners.size());
        assertFalse(qs.isEnabled());
        qs.setEnabled(true);
        assertTrue(qs.isEnabled());
        assertEquals("One added key listener is expected after setEnabled(true)", 1, component.addedKeyListeners.size());
        qs.detach();
    }

    /**
     * Test of the addition of quick search component.
     */
    public void testQuickSearchAdd() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        testQuickSearchAdd();
                    }
                });
            } catch (InterruptedException iex) {
                fail("interrupted.");
            } catch (InvocationTargetException itex) {
                Throwable cause = itex.getCause();
                if (cause instanceof AssertionError) {
                    throw (AssertionError) cause;
                }
                itex.getCause().printStackTrace();
                throw new AssertionError(cause);
            }
            return;
        }
        TestComponent component = new TestComponent();
        Object constraints = null;
        QuickSearch qs = QuickSearch.attach(component, constraints, new DummyCallback());
        component.addNotify();
        KeyEvent ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        //KeyboardFocusManager.getCurrentKeyboardFocusManager().setGlobalFocusOwner(component);
        try {
            Method setGlobalFocusOwner = KeyboardFocusManager.class.getDeclaredMethod("setGlobalFocusOwner", Component.class);
            setGlobalFocusOwner.setAccessible(true);
            setGlobalFocusOwner.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(), component);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
        component.dispatchEvent(ke);
        assertNotNull(component.added);
        assertNull(component.constraints);
        qs.detach();
        assertNull(component.added);
        
        constraints = new Object();
        qs = QuickSearch.attach(component, constraints, new DummyCallback());
        ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        component.dispatchEvent(ke);
        assertNotNull(component.added);
        assertEquals(constraints, component.constraints);
        qs.detach();
        assertNull(component.added);
    }
    
    /**
     * Test of the quick search listener.
     */
    public void testQuickSearchListener() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        testQuickSearchListener();
                    }
                });
            } catch (InterruptedException iex) {
                fail("interrupted.");
            } catch (InvocationTargetException itex) {
                Throwable cause = itex.getCause();
                if (cause instanceof AssertionError) {
                    throw (AssertionError) cause;
                }
                itex.getCause().printStackTrace();
                throw new AssertionError(cause);
            }
            return;
        }
        TestComponent component = new TestComponent();
        Object constraints = null;
        final String[] searchTextPtr = new String[] { null };
        final Boolean[] biasPtr = new Boolean[] { null };
        final boolean[] confirmedPtr = new boolean[] { false };
        final boolean[] canceledPtr = new boolean[] { false };
        QuickSearch.Callback qsc = new QuickSearch.Callback() {
            
            @Override
            public void quickSearchUpdate(String searchText) {
                assertTrue(SwingUtilities.isEventDispatchThread());
                searchTextPtr[0] = searchText;
            }

            @Override
            public void showNextSelection(boolean forward) {
                assertTrue(SwingUtilities.isEventDispatchThread());
                biasPtr[0] = forward;
            }

            @Override
            public String findMaxPrefix(String prefix) {
                assertTrue(SwingUtilities.isEventDispatchThread());
                return prefix + "endPrefix";
            }

            @Override
            public void quickSearchConfirmed() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                confirmedPtr[0] = true;
            }

            @Override
            public void quickSearchCanceled() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                canceledPtr[0] = true;
            }

        };
        QuickSearch qs = QuickSearch.attach(component, constraints, qsc);
        component.addNotify();
        // Test that a key event passed to the component triggers the quick search:
        try {
            Method setGlobalFocusOwner = KeyboardFocusManager.class.getDeclaredMethod("setGlobalFocusOwner", Component.class);
            setGlobalFocusOwner.setAccessible(true);
            setGlobalFocusOwner.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(), component);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
        KeyEvent ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        component.dispatchEvent(ke);
        assertEquals("A", qs.getSearchField().getText());
        assertEquals("A", searchTextPtr[0]);
        assertNull(biasPtr[0]);
        
        // Test that further key events passed to the quick search field trigger the quick search listener:
        qs.getSearchField().setCaretPosition(1);
        try {
            Method setGlobalFocusOwner = KeyboardFocusManager.class.getDeclaredMethod("setGlobalFocusOwner", Component.class);
            setGlobalFocusOwner.setAccessible(true);
            setGlobalFocusOwner.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(), qs.getSearchField());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'b');
        qs.getSearchField().dispatchEvent(ke);
        assertEquals("Ab", searchTextPtr[0]);
        
        // Test the up/down keys resulting to selection navigation:
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
        qs.getSearchField().dispatchEvent(ke);
        assertEquals(Boolean.FALSE, biasPtr[0]);
        
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, (char) KeyEvent.VK_DOWN);
        qs.getSearchField().dispatchEvent(ke);
        assertEquals(Boolean.TRUE, biasPtr[0]);
        
        // Test that tab adds max prefix:
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_TAB, '\t');
        qs.getSearchField().dispatchEvent(ke);
        assertEquals("AbendPrefix", qs.getSearchField().getText());
        
        /*
        // Test that we get no events when quick search listener is detached:
        qs.removeQuickSearchListener(qsl);
        qs.getSearchField().setCaretPosition(2);
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'c');
        qs.getSearchField().dispatchEvent(ke);
        assertEquals("AbcendPrefix", qs.getSearchField().getText());
        assertEquals("Ab", searchTextPtr[0]);
        qs.addQuickSearchListener(qsl);
        */
        
        // Test the quick search confirmation on Enter key:
        assertFalse(confirmedPtr[0]);
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');
        qs.getSearchField().dispatchEvent(ke);
        assertTrue(confirmedPtr[0]);
        
        // Test the quick search cancel on ESC key:
        ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        component.dispatchEvent(ke);
        assertEquals("A", searchTextPtr[0]);
        assertFalse(canceledPtr[0]);
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) 27);
        qs.getSearchField().dispatchEvent(ke);
        assertTrue(canceledPtr[0]);
    }
    
    enum sync { W, N } // Wait, Notify

    /**
     * Test of asynchronous calls, of class QuickSearch.
     */
    public void testAsynchronous() {
        final TestComponent[] componentPtr = new TestComponent[] { null };
        final String[] searchTextPtr = new String[] { null };
        final Boolean[] biasPtr = new Boolean[] { null };
        final Object findMaxPrefixLock = new Object();
        final boolean[] confirmedPtr = new boolean[] { false };
        final boolean[] canceledPtr = new boolean[] { false };
        final sync[] syncPtr = new sync[] { null };
        final QuickSearch.Callback qsc = new QuickSearch.Callback() {

            @Override
            public void quickSearchUpdate(String searchText) {
                assertFalse(SwingUtilities.isEventDispatchThread());
                synchronized(searchTextPtr) {
                    if (syncPtr[0] == null) {
                        syncPtr[0] = sync.W;
                        // Wait for the notification first
                        try { searchTextPtr.wait(); } catch (InterruptedException iex) {}
                    }
                    searchTextPtr[0] = searchText;
                    searchTextPtr.notifyAll();
                    syncPtr[0] = null;
                }
            }

            @Override
            public void showNextSelection(boolean forward) {
                assertFalse(SwingUtilities.isEventDispatchThread());
                synchronized(biasPtr) {
                    if (syncPtr[0] == null) {
                        syncPtr[0] = sync.W;
                        // Wait for the notification first
                        try { biasPtr.wait(); } catch (InterruptedException iex) {}
                    }
                    biasPtr[0] = forward;
                    biasPtr.notifyAll();
                    syncPtr[0] = null;
                }
            }

            @Override
            public String findMaxPrefix(String prefix) {
                assertFalse(SwingUtilities.isEventDispatchThread());
                synchronized(findMaxPrefixLock) {
                    if (syncPtr[0] == null) {
                        syncPtr[0] = sync.W;
                        // Wait for the notification first
                        try { findMaxPrefixLock.wait(); } catch (InterruptedException iex) {}
                    }
                    prefix = prefix + "endPrefix";
                    findMaxPrefixLock.notifyAll();
                    syncPtr[0] = null;
                }
                return prefix;
            }

            @Override
            public void quickSearchConfirmed() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                confirmedPtr[0] = true;
            }

            @Override
            public void quickSearchCanceled() {
                assertTrue(SwingUtilities.isEventDispatchThread());
                canceledPtr[0] = true;
            }

        };
        final QuickSearch[] qsPtr = new QuickSearch[] { null };
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    componentPtr[0] = new TestComponent();
                    qsPtr[0] = QuickSearch.attach(componentPtr[0], null, qsc, true);
                    componentPtr[0].addNotify();
                }
            });
        } catch (InterruptedException iex) {
            fail("interrupted.");
        } catch (InvocationTargetException itex) {
            Throwable cause = itex.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            itex.getCause().printStackTrace();
            throw new AssertionError(cause);
        }
        // Test that a key event passed to the component triggers the asynchronous quick search:
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Method setGlobalFocusOwner = KeyboardFocusManager.class.getDeclaredMethod("setGlobalFocusOwner", Component.class);
                        setGlobalFocusOwner.setAccessible(true);
                        setGlobalFocusOwner.invoke(KeyboardFocusManager.getCurrentKeyboardFocusManager(), componentPtr[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new AssertionError(ex);
                    }
                    KeyEvent ke = new KeyEvent(componentPtr[0], KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
                    componentPtr[0].dispatchEvent(ke);
                }
            });
        } catch (InterruptedException iex) {
            fail("interrupted.");
        } catch (InvocationTargetException itex) {
            Throwable cause = itex.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            itex.getCause().printStackTrace();
            throw new AssertionError(cause);
        }
        synchronized(searchTextPtr) {
            assertNull(searchTextPtr[0]);
            syncPtr[0] = sync.N;
            searchTextPtr.notifyAll();
            // Wait to set the value
            try { searchTextPtr.wait(); } catch (InterruptedException iex) {}
            assertEquals("A", searchTextPtr[0]);
        }
        
        // Test the up/down keys resulting to asynchronous selection navigation:
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    KeyEvent ke = new KeyEvent(qsPtr[0].getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
                    qsPtr[0].getSearchField().dispatchEvent(ke);

                    ke = new KeyEvent(qsPtr[0].getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, (char) KeyEvent.VK_DOWN);
                    qsPtr[0].getSearchField().dispatchEvent(ke);
                }
            });
        } catch (InterruptedException iex) {
            fail("interrupted.");
        } catch (InvocationTargetException itex) {
            Throwable cause = itex.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            itex.getCause().printStackTrace();
            throw new AssertionError(cause);
        }
        synchronized(biasPtr) {
            assertNull(biasPtr[0]);
            syncPtr[0] = sync.N;
            biasPtr.notifyAll();
            // Wait to set the value
            try { biasPtr.wait(); } catch (InterruptedException iex) {}
            assertEquals(Boolean.FALSE, biasPtr[0]);
        }
        synchronized(biasPtr) {
            assertEquals(Boolean.FALSE, biasPtr[0]);
            syncPtr[0] = sync.N;
            biasPtr.notifyAll();
            // Wait to set the value
            try { biasPtr.wait(); } catch (InterruptedException iex) {}
            assertEquals(Boolean.TRUE, biasPtr[0]);
        }
        
        // Test that tab adds max prefix asynchronously:
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    KeyEvent ke = new KeyEvent(qsPtr[0].getSearchField(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_TAB, '\t');
                    qsPtr[0].getSearchField().dispatchEvent(ke);
                }
            });
        } catch (InterruptedException iex) {
            fail("interrupted.");
        } catch (InvocationTargetException itex) {
            Throwable cause = itex.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            itex.getCause().printStackTrace();
            throw new AssertionError(cause);
        }
        synchronized(findMaxPrefixLock) {
            assertEquals("A", qsPtr[0].getSearchField().getText());
            syncPtr[0] = sync.N;
            findMaxPrefixLock.notifyAll();
            // Wait to set the value
            try { findMaxPrefixLock.wait(); } catch (InterruptedException iex) {}
            // Can not test it immediatelly, the text is updated in AWT
            // assertEquals("AendPrefix", qsPtr[0].getSearchField().getText());
        }
        try { Thread.sleep(200); } catch (InterruptedException iex) {}
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    assertEquals("AendPrefix", qsPtr[0].getSearchField().getText());
                }
            });
        } catch (InterruptedException iex) {
            fail("interrupted.");
        } catch (InvocationTargetException itex) {
            Throwable cause = itex.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            itex.getCause().printStackTrace();
            throw new AssertionError(cause);
        }
    }
    
    /**
     * Test of processKeyEvent method, of class QuickSearch.
     */
    public void testProcessKeyEvent() {
        TestComponent component = new TestComponent();
        Object constraints = null;
        final String[] searchTextPtr = new String[] { null };
        final Boolean[] biasPtr = new Boolean[] { null };
        final boolean[] confirmedPtr = new boolean[] { false };
        final boolean[] canceledPtr = new boolean[] { false };
        final QuickSearch.Callback qsc = new QuickSearch.Callback() {

            @Override
            public void quickSearchUpdate(String searchText) {
                searchTextPtr[0] = searchText;
            }

            @Override
            public void showNextSelection(boolean forward) {
                biasPtr[0] = forward;
            }

            @Override
            public String findMaxPrefix(String prefix) {
                return prefix + "endPrefix";
            }

            @Override
            public void quickSearchConfirmed() {
                confirmedPtr[0] = true;
            }

            @Override
            public void quickSearchCanceled() {
                canceledPtr[0] = true;
            }
        };
        QuickSearch qs = QuickSearch.attach(component, constraints, qsc);
        KeyEvent ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        qs.processKeyEvent(ke);
        assertEquals("A", qs.getSearchField().getText());
        assertEquals("A", searchTextPtr[0]);
        assertNull(biasPtr[0]);
        
        ke = new KeyEvent(qs.getSearchField(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'b');
        qs.processKeyEvent(ke);
        assertEquals("Ab", qs.getSearchField().getText());
        assertEquals("Ab", searchTextPtr[0]);
    }

    /**
     * Test of findMaxCommonSubstring method, of class QuickSearch.
     */
    public void testFindMaxCommonSubstring() {
        System.out.println("findMaxCommonSubstring");
        String str1 = "annotation";
        String str2 = "antenna";
        boolean ignoreCase = false;
        String expResult = "an";
        String result = QuickSearch.findMaxPrefix(str1, str2, ignoreCase);
        assertEquals(expResult, result);
        str1 = "Annotation";
        expResult = "";
        result = QuickSearch.findMaxPrefix(str1, str2, ignoreCase);
        assertEquals(expResult, result);
        str1 = "AbCdEf";
        str2 = "AbCxxx";
        expResult = "AbC";
        result = QuickSearch.findMaxPrefix(str1, str2, ignoreCase);
        assertEquals(expResult, result);
    }
    
    public void testClearOnESC() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        testClearOnESC();
                    }
                });
            } catch (InterruptedException iex) {
                fail("interrupted.");
            } catch (InvocationTargetException itex) {
                Throwable cause = itex.getCause();
                if (cause instanceof AssertionError) {
                    throw (AssertionError) cause;
                }
                itex.getCause().printStackTrace();
                throw new AssertionError(cause);
            }
            return;
        }
        TestComponent component = new TestComponent();
        QuickSearch qs = QuickSearch.attach(component, null, new DummyCallback());
        component.addNotify();
        KeyEvent ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'A');
        component.dispatchEvent(ke);
        
        JTextField searchField = qs.getSearchField();
        
        assertTrue(searchField.isDisplayable());
        assertEquals("A", searchField.getText());
        //assertFalse(canceledPtr[0]);
        ke = new KeyEvent(searchField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) 27);
        searchField.dispatchEvent(ke);
        assertTrue(searchField.getText().isEmpty());
        assertFalse(searchField.isDisplayable());
        // The search field is dismissed and the search text is cleared.

        assertFalse(qs.isAlwaysShown()); // Was not always shown
        qs.setAlwaysShown(true); // Force to show
        assertTrue(searchField.isDisplayable());
        
        ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'B');
        component.dispatchEvent(ke);
        assertEquals("B", searchField.getText());
        
        ke = new KeyEvent(searchField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) 27);
        searchField.dispatchEvent(ke);
        assertTrue(searchField.getText().isEmpty());
        assertTrue(searchField.isDisplayable());
        // The search field is visible and cleared
        
        ke = new KeyEvent(component, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'B');
        component.dispatchEvent(ke);
        assertEquals("B", searchField.getText());
        qs.setAlwaysShown(false); // Hide
        assertEquals("B", searchField.getText());
        assertFalse(searchField.isDisplayable());
        qs.setAlwaysShown(true); // Show
        assertEquals("B", searchField.getText()); // B is still there
        assertTrue(searchField.isDisplayable());
        
        ke = new KeyEvent(searchField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) 27);
        searchField.dispatchEvent(ke);
        assertTrue(searchField.getText().isEmpty());
    }
    
    private static final class TestComponent extends JComponent {
        
        List<KeyListener> addedKeyListeners = new ArrayList<KeyListener>();
        Component added;
        Object constraints;
        
        public TestComponent() {
            new JFrame().add(this); // To have a parent
        }

        @Override
        public boolean isShowing() {
            return true;
        }
        
        @Override
        public Component add(Component comp) {
            this.added = comp;
            return super.add(comp);
        }

        @Override
        public void add(Component comp, Object constraints) {
            this.added = comp;
            this.constraints = constraints;
            super.add(comp, constraints);
        }

        @Override
        public void remove(Component comp) {
            if (comp == this.added) {
                this.added = null;
            }
            super.remove(comp);
        }
        
        @Override
        public synchronized void addKeyListener(KeyListener l) {
            addedKeyListeners.add(l);
            super.addKeyListener(l);
        }

        @Override
        public synchronized void removeKeyListener(KeyListener l) {
            addedKeyListeners.remove(l);
            super.removeKeyListener(l);
        }
        
    }
    
    private static final class DummyCallback implements QuickSearch.Callback {

        @Override
        public void quickSearchUpdate(String searchText) {}

        @Override
        public void showNextSelection(boolean forward) {}

        @Override
        public String findMaxPrefix(String prefix) {
            return prefix;
        }

        @Override
        public void quickSearchConfirmed() {}

        @Override
        public void quickSearchCanceled() {}
        
    }
}
