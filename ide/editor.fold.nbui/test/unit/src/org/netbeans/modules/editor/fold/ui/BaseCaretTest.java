/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.fold.ui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

import org.netbeans.modules.editor.fold.FoldHierarchyTestEnv;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;

/**
 * Checks that the folding succeeds in plugging into BaseCaret's operation.
 * It checks that a doubleclick on a folded place does not select the surrounding text.
 * Likewise it checks, that a doubleclick on an unfolded place WILL select the surrounding text
 * 
 * @author sdedic
 */
public class BaseCaretTest extends NbTestCase {
    private JEditorPane pane;
    private FoldHierarchyTestEnv env;
    public static Lkp DEFAULT_LOOKUP = null;
    
    public BaseCaretTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.doSetLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    public static class Lkp extends ProxyLookup {
        {
            DEFAULT_LOOKUP = this;
        }
        
        public void doSetLookups(Lookup... lkps) {
            super.setLookups(lkps);
        }
    }
    
    private static final Method PROCESS_EVENT_METHOD;
    
    static {
        System.setProperty("org.openide.util.Lookup", BaseCaretTest.Lkp.class.getName());
        Method m = null;
        
        try {
            m = Component.class.getDeclaredMethod("processEvent", new Class[] { AWTEvent.class });
            m.setAccessible(true);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        PROCESS_EVENT_METHOD = m;
    }

    public static void prepareTest(String[] additionalLayers, Object[] additionalLookupContent) throws IOException, SAXException, PropertyVetoException {
        Collection<URL> allUrls = new ArrayList<URL>();
        for (String u : additionalLayers) {
            if (u.charAt(0) == '/') {
                u = u.substring(1);
            }
            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(u); en.hasMoreElements(); ) {
                allUrls.add(en.nextElement());
            }
        }
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(allUrls.toArray(new URL[0]));
        
        Repository repository = new Repository(system);
        Object[] lookupContent = new Object[additionalLookupContent.length + 1];
        
        System.arraycopy(additionalLookupContent, 0, lookupContent, 1, additionalLookupContent.length);
        
        lookupContent[0] = repository;
        
        setLookup(lookupContent, BaseCaretTest.class.getClassLoader());
    }
    
    private FM fm;
    
    private Fold fold;
    
    private Callable<Boolean> delegate;

    public void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault();
        prepareTest(new String[] {
                "/org/netbeans/modules/editor/resources/layer.xml",
                "/META-INF/generated-layer.xml",
                "/org/netbeans/modules/defaults/mf-layer.xml",
        },
        new Object[0]);
        // hack:
        MimeLookup.getLookup("").lookup(FoldHierarchyMonitor.class);
        env = new FoldHierarchyTestEnv(new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return fm = new FM();
            }
        });
        pane = env.getPane();
        env.getPane().setEditorKit(new Kit());
        env.getDocument().insertString(0, "123456789-123456789-123 aa 89-123456789-1234567890", null);
        
        // ensure initialized
        env.getHierarchy(); 
        // cannot be done in FoldManager, as setEditorKit() will replace document and reinitialize folds in 2nd thread, which may
        // complete even faster than insertString(), so BLE could be thrown.
        FoldHierarchyTransaction tran = fm.op.openTransaction();
        fm.op.addToHierarchy(
                FT, 
                "", 
                true, 
                10, 
                40, 
                1, 1, 
                null, 
                tran);
        tran.commit();
                    
        fold = FoldUtilities.findOffsetFold(env.getHierarchy(), 25);
       // get x/y of the folded part:
        JFrame fr = new JFrame("test");
        fr.setBounds(0, 0, 400, 100);
        fr.add(pane);
        fr.setVisible(true);
        
        delegate = (Callable<Boolean>)pane.getClientProperty("org.netbeans.api.fold.expander");
        pane.putClientProperty("org.netbeans.api.fold.expander", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return retValue = delegate.call();
            }
            
            public boolean equals(Object o) {
                return retValue = delegate.equals(o);
            }
        });
        pane.requestFocus();
        selectWordCalled = false;
    }

    public void tearDown() throws Exception {
        Window w = SwingUtilities.getWindowAncestor(pane);
        w.setVisible(false);
        super.tearDown();
    }
    
    private void processEvent(AWTEvent e) throws Exception{
        PROCESS_EVENT_METHOD.invoke(pane, e);
    }
    
    private boolean retValue;
    
    private static boolean selectWordCalled;
    
    private static class Kit extends ExtKit {
        @Override
        protected Action[] getDeclaredActions() {
            Action swa = new TextAction("") {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    selectWordCalled = true;
                }
            };
            swa.putValue(Action.NAME, BaseKit.selectWordAction);
            
            Action[] actions = new Action[] {
                swa
            };
            return TextAction.augmentList(super.createActions(), actions);
        }
        
    }

    /**
     * Folded region is double-clicked; it should be unfolded, rather than word-selected.
     */
    public void testSelectFoldedRegion() throws Exception {
        env.getHierarchy().collapse(fold);
        Thread.sleep(300);
        Rectangle r = pane.modelToView(10);
        
        // 1st mouseclick on the pane, to position the caret, as if the 1st click in doubleclick was done:
        MouseEvent firstPress = new MouseEvent(pane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 
                InputEvent.BUTTON1_MASK, r.x, r.y, 1, false);
        processEvent(firstPress);
        
        // second press of the doubleclick
        MouseEvent secondPress = new MouseEvent(pane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 
                InputEvent.BUTTON1_MASK, r.x, r.y, 2, false);
        processEvent(secondPress);
        
        // check that no selection is present:
        assertFalse(selectWordCalled);
        assertNull(pane.getSelectedText());
        // check that even the callable did the job:
        assertFalse(fold.isCollapsed());
    }
    
    public void testSelectUnfoldedRegion() throws Exception {
        env.getHierarchy().expand(fold);
        Thread.sleep(300);
        Rectangle r = pane.modelToView(25);
        // 1st mouseclick on the pane, to position the caret, as if the 1st click in doubleclick was done:
        MouseEvent firstPress = new MouseEvent(pane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 
                InputEvent.BUTTON1_MASK, r.x, r.y, 1, false);
        processEvent(firstPress);
        
        // second press of the doubleclick
        MouseEvent secondPress = new MouseEvent(pane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 
                InputEvent.BUTTON1_MASK, r.x, r.y, 2, false);
        env.getPane().requestFocus();
        processEvent(secondPress);
        
        assertFalse(retValue);
        assertTrue(this.toString(), selectWordCalled);
    }
    
    private static final FoldType FT = new FoldType("test");
    
    private static class FM implements FoldManager {
        FoldOperation op;
        
        @Override
        public void init(FoldOperation operation) {
            this.op = operation;
        }

        @Override
        public void initFolds(FoldHierarchyTransaction transaction) {
        }

        @Override
        public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void removeEmptyNotify(Fold epmtyFold) {
        }

        @Override
        public void removeDamagedNotify(Fold damagedFold) {
        }

        @Override
        public void expandNotify(Fold expandedFold) {
        }

        @Override
        public void release() {
        }
    }
    
    private static class P extends AbstractPreferences {
        private Map<String, String>  m = new HashMap<String, String>();

        public P(AbstractPreferences parent, String name) {
            super(parent, name);
        }
        
        @Override
        protected void putSpi(String key, String value) {
            m.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return m.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            m.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            return new String[0];
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return null;
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
        }
    }
}
