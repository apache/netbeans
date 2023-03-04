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

package org.openide.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

public class WeakListenersTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(WeakListenersTest.class.getName());
    
    public WeakListenersTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    /** Tests that exception is not logged when WeakListenerImpl tries to
     * remove listener on already removed node (#151415). */
    public void testPreferenceChangeListener() throws Exception {
        PreferenceChangeListener pcl = new PreferenceChangeListener() {

            public void preferenceChange(PreferenceChangeEvent evt) {
            }
        };
        NodeChangeListener ncl = new NodeChangeListener() {

            public void childAdded(NodeChangeEvent evt) {
            }

            public void childRemoved(NodeChangeEvent evt) {
            }
        };
        Preferences nbp = NbPreferences.forModule(WeakListeners.class);
        nbp.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, pcl, nbp));
        nbp.addNodeChangeListener(WeakListeners.create(NodeChangeListener.class, ncl, nbp));
        nbp.removeNode();

        Logger logger = Logger.getLogger(WeakListenerImpl.class.getName());
        final AtomicBoolean nodeRemovedException = new AtomicBoolean(false);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                Throwable t = record.getThrown();
                if (t != null) {
                    if (t.getCause() instanceof IllegalStateException && t.getCause().getMessage().equals("Node has been removed.")) {  //NOI18N
                        nodeRemovedException.set(true);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        Reference<PreferenceChangeListener> ref = new WeakReference<PreferenceChangeListener>(pcl);
        pcl = null;
        assertGC("PreferenceChangeListener cannot be collected", ref);
        Reference<NodeChangeListener> ref1 = new WeakReference<NodeChangeListener>(ncl);
        ncl = null;
        assertGC("NodeChangeListener cannot be collected", ref1);
        Thread.sleep(2000);
        assertFalse("The \"Node has been removed\" exception should not be thrown from WeakListenerImpl.", nodeRemovedException.get());
    }

    public void testOneCanCallHashCodeOrOnWeakListener () {
        Listener l = new Listener ();
        Object weak = WeakListeners.create (PropertyChangeListener.class, l, null);
        weak.hashCode ();
    }
    
    /** Useful for next test */
    interface X extends java.util.EventListener {
        public void invoke ();
    }
    /** Useful for next test */
    class XImpl implements X {
        public int cnt;
        public void invoke () {
            cnt++;
        }
    }
    public void testCallingMethodsWithNoArgumentWorks() {
        XImpl l = new XImpl ();
        LOG.fine("XImpl created: " + l);
        X weak = WeakListeners.create(X.class, l, null);
        LOG.fine("weak created: " + weak);
        weak.invoke ();
        LOG.fine("invoked");
        assertEquals ("One invocation", 1, l.cnt);
    }

    public void testReleaseOfListenerWithNullSource () throws Exception {
        doTestReleaseOfListener (false);
    }
    
    public void testReleaseOfListenerWithSource () throws Exception {
        doTestReleaseOfListener (true);
    }
    
    public void testReleaseOfPropNameListenerWithNullSource () throws Exception {
        doTestReleaseOfListener (false, javax.swing.AbstractButton.TEXT_CHANGED_PROPERTY);
    }
    
    public void testReleaseOfPropNameListenerWithSource () throws Exception {
        doTestReleaseOfListener (true, javax.swing.AbstractButton.TEXT_CHANGED_PROPERTY);
    }
    
    private void doTestReleaseOfListener (final boolean source) throws Exception {   
        doTestReleaseOfListener(source, null);
    }
    
    private void doTestReleaseOfListener (final boolean source, final String propName) throws Exception {
        Listener l = new Listener ();
        
        class MyButton extends javax.swing.JButton {
            private Thread removedBy;
            private int cnt;
            private int cntNamed;
            
            @Override
            public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
                // notify prior
                LOG.fine("removePropertyChangeListener: " + source + " cnt: " + cnt);
                if (source && cnt == 0) {
                    notifyAll ();
                    try {
                        // wait for 1
                        LOG.fine("wait for 1");
                        wait ();
                        LOG.fine("wait for 1 over");
                    } catch (InterruptedException ex) {
                        fail ("Not happen");
                    }
                }
                LOG.fine("Super removePropertyChangeListener");
                super.removePropertyChangeListener (l);
                LOG.fine("Super over removePropertyChangeListener");
                removedBy = Thread.currentThread();
                cnt++;
                notifyAll ();
            }

            @Override
            public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
                // notify prior
                LOG.fine("removePropertyChangeListener("+propertyName+"): " + source + " cnt: " + cntNamed);
                if (source && cntNamed == 0) {
                    notifyAll ();
                    try {
                        // wait for 1
                        LOG.fine("wait for 1");
                        wait ();
                        LOG.fine("wait for 1 over");
                    } catch (InterruptedException ex) {
                        fail ("Not happen");
                    }
                }
                assertEquals(propName, propertyName);
                LOG.fine("Super removePropertyChangeListener");
                super.removePropertyChangeListener (propertyName, l);
                LOG.fine("Super over removePropertyChangeListener");
                removedBy = Thread.currentThread();
                cntNamed++;
                notifyAll ();
            }
            
            public synchronized void waitListener () throws Exception {
                int count = 0;
                while (removedBy == null) {
                    LOG.fine("waitListener, wait 500");
                    wait (500);
                    LOG.fine("waitListener 500 Over");
                    if (count++ == 5) {
                        fail ("Time out: removePropertyChangeListener was not called at all");
                    } else {
                        LOG.fine("Forced gc");
                        System.gc ();
                        System.runFinalization();
                        LOG.fine("after force runFinalization");
                    }
                }
            }
        }
        
        MyButton button = new MyButton ();
        LOG.fine("Button is here");
        java.beans.PropertyChangeListener weakL;
        if (propName == null) {
            weakL = WeakListeners.propertyChange (l, source ? button : null);
        } else {
            weakL = WeakListeners.propertyChange (l, propName, source ? button : null);
        }
        LOG.fine("WeakListeners created: " + weakL);
        if (propName == null) {
            button.addPropertyChangeListener(weakL);
        } else {
            button.addPropertyChangeListener(propName, weakL);
        }
        LOG.fine("WeakListeners attached");
        if (propName == null) {
            assertTrue ("Weak listener is there", Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL) >= 0);
        } else {
            assertTrue ("Weak listener is there", Arrays.asList (button.getPropertyChangeListeners(propName)).indexOf (weakL) >= 0);
        }
        
        button.setText("Ahoj");
        LOG.fine("setText changed to ahoj");
        assertEquals ("Listener called once", 1, l.cnt);
        
        Reference<?> ref = new WeakReference<Object>(l);
        LOG.fine("Clearing listener");
        l = null;
        

        synchronized (button) {
            LOG.fine("Before assertGC");
            assertGC ("Can disappear", ref);
            LOG.fine("assertGC ok");
            
            if (source) {
                LOG.fine("before wait");
                button.wait ();
                LOG.fine("after wait");
                // this should not remove the listener twice
                button.setText ("Hoj");
                LOG.fine("after setText - > hoj");
                // go on (wait 1)
                button.notify ();
                LOG.fine("before wait listener");
                
                button.waitListener ();
                LOG.fine("after waitListener");
            } else {
                // trigger the even firing so weak listener knows from
                // where to unregister
                LOG.fine("before setText -> Hoj");
                button.setText ("Hoj");
                LOG.fine("after setText -> Hoj");
            }
            
            LOG.fine("before 2 waitListener");
            button.waitListener ();
            LOG.fine("after 2 waitListener");
            Thread.sleep (500);
            LOG.fine("Thread.sleep over");
        }

        if (propName == null) {
            assertEquals ("Weak listener has been removed", -1, Arrays.asList (button.getPropertyChangeListeners()).indexOf (weakL));
        } else {
            assertEquals ("Weak listener has been removed", -1, Arrays.asList (button.getPropertyChangeListeners(propName)).indexOf (weakL));
        }
        assertEquals ("Button released from a thread", "Active Reference Queue Daemon", button.removedBy.getName());
        if (propName == null) {
            assertEquals ("Unregister called just once", 1, button.cnt);
            assertEquals ("Unregister named not called", 0, button.cntNamed);
        } else {
            assertEquals ("Unregister called just once", 1, button.cntNamed);
            assertEquals ("Unregister unnamed not called", 0, button.cnt);
        }
        
        // and because it is not here, it can be GCed
        Reference<?> weakRef = new WeakReference<Object>(weakL);
        weakL = null;
        LOG.fine("Doing assertGC at the end");
        assertGC ("Weak listener can go away as well", weakRef);
    }
    
    public void testNameListenersCombined() {
        Logger WL_LOG = Logger.getLogger(WeakListenerImpl.class.getName());
        class WLLogHandler extends Handler {
            
            private String warnings;

            @Override
            public void publish(LogRecord record) {
                if (Level.WARNING.intValue() <= record.getLevel().intValue()) {
                    if (warnings == null) {
                        warnings = record.toString();
                    } else {
                        warnings += "\n" + record.toString();
                    }
                }
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
            
            String getWarnings() {
                return warnings;
            }
            
        }
        WLLogHandler wlLogHandler = new WLLogHandler();
        WL_LOG.addHandler(wlLogHandler);
        VetoableChangeSupport vcs = new VetoableChangeSupport(this);
        DefaultColorSelectionModel dcsm = new DefaultColorSelectionModel();
        VListener v1 = new VListener ();
        VListener v2 = new VListener ();
        VListener v3 = new VListener ();
        VListener v4 = new VListener ();
        CListener c1 = new CListener();
        CListener c2 = new CListener();
        CListener c3 = new CListener();
        CListener c4 = new CListener();
        
        vcs.addVetoableChangeListener (WeakListeners.vetoableChange(v1, vcs));
        vcs.addVetoableChangeListener ("name", WeakListeners.vetoableChange (v2, "name", vcs));
        vcs.addVetoableChangeListener ("name3", WeakListeners.vetoableChange (v3, "name3", vcs));
        vcs.addVetoableChangeListener (WeakListeners.vetoableChange (v4, vcs));
        dcsm.addChangeListener(WeakListeners.change(c1, dcsm));
        dcsm.addChangeListener(WeakListeners.change(c2, dcsm));
        dcsm.addChangeListener(WeakListeners.change(c3, dcsm));
        dcsm.addChangeListener(WeakListeners.change(c4, dcsm));
    
        //System.err.println("V1:");
        Reference<?> refV1 = new WeakReference<Object>(v1);
        v1 = null;
        assertGC ("Listener can be GC", refV1);
        
        //System.err.println("C1:");
        Reference<?> refC1 = new WeakReference<Object>(c1);
        c1 = null;
        assertGC ("Listener can be GC", refC1);
        
        //System.err.println("V2:");
        Reference<?> refV2 = new WeakReference<Object>(v2);
        v2 = null;
        assertGC ("Listener can be GC", refV2);
        
        //System.err.println("C2:");
        Reference<?> refC2 = new WeakReference<Object>(c2);
        c2 = null;
        assertGC ("Listener can be GC", refC2);
        
        //System.err.println("V3:");
        Reference<?> refV3 = new WeakReference<Object>(v3);
        v3 = null;
        assertGC ("Listener can be GC", refV3);
        
        //System.err.println("V4:");
        Reference<?> refV4 = new WeakReference<Object>(v4);
        v4 = null;
        assertGC ("Listener can be GC", refV4);
        
        //System.err.println("C3:");
        Reference<?> refC3 = new WeakReference<Object>(c3);
        c3 = null;
        assertGC ("Listener can be GC", refC3);
        
        //System.err.println("ALL:");
        Reference<?> refVcs = new WeakReference<Object>(vcs);
        vcs = null;
        Reference<?> refDcsm = new WeakReference<Object>(dcsm);
        dcsm = null;
        
        assertGC ("Source can be GC", refVcs);
        assertGC ("Source can be GC", refDcsm);
        
        String warnings = wlLogHandler.getWarnings();
        if (warnings != null) {
            System.err.println(warnings);
        }
        assertNull(warnings, warnings);
    }
    
    
    public void testSourceCanBeGarbageCollected () {
        javax.swing.JButton b = new javax.swing.JButton ();
        Listener l = new Listener ();
        
        b.addPropertyChangeListener (WeakListeners.propertyChange (l, b));
        
        Reference<?> ref = new WeakReference<Object>(b);
        b = null;
        
        assertGC ("Source can be GC", ref);
    }
    
    public void testNamingListenerBehaviour () throws Exception {
        Listener l = new Listener ();
        ImplEventContext c = new ImplEventContext ();
        javax.naming.event.NamingListener weakL = (javax.naming.event.NamingListener)WeakListeners.create (
            javax.naming.event.ObjectChangeListener.class,
            javax.naming.event.NamingListener.class,
            l,
            c
        );
        
        c.addNamingListener("", javax.naming.event.EventContext.OBJECT_SCOPE, weakL);
        assertEquals ("Weak listener is there", weakL, c.listener);
        
        Reference<?> ref = new WeakReference<Object>(l);
        l = null;

        synchronized (c) {
            assertGC ("Can disappear", ref);
            c.waitListener ();
        }
        assertNull ("Listener removed", c.listener);
    }
    
    public void testExceptionIllegalState () {
        Listener l = new Listener ();
        /* Will not even compile any more:
        try {
            WeakListeners.create(PropertyChangeListener.class, javax.naming.event.NamingListener.class, l, null);
            fail ("This shall not be allowed as NamingListener is not superclass of PropertyChangeListener");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create(Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
        
        try {
            WeakListeners.create(Object.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
         */
        
        try {
            WeakListeners.create (PropertyChangeListener.class, Object.class, l, null);
            fail ("Not interface, it should fail");
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }
    
    public void testHowBigIsWeakListener () throws Exception {
        Listener l = new Listener ();
        javax.swing.JButton button = new javax.swing.JButton ();
        ImplEventContext c = new ImplEventContext ();
        
        Object[] ignore = {
            l, 
            button,
            c,
            BaseUtilities.activeReferenceQueue()
        };
        
        
        PropertyChangeListener pcl = WeakListeners.propertyChange(l, button);
        assertSize ("Not too big (plus 32 from ReferenceQueue)", java.util.Collections.singleton (pcl), 120, ignore);
        
        Object ocl = WeakListeners.create (javax.naming.event.ObjectChangeListener.class, javax.naming.event.NamingListener.class, l, c);
        assertSize ("A bit bigger (plus 32 from ReferenceQueue)", java.util.Collections.singleton (ocl), 136, ignore);
        
        Object nl = WeakListeners.create (javax.naming.event.NamingListener.class, l, c);
        assertSize ("The same (plus 32 from ReferenceQueue)", java.util.Collections.singleton (nl), 136, ignore);
        
    }

    public void testPrivateRemoveMethod() throws Exception {
        PropChBean bean = new PropChBean();
        Listener listener = new Listener();
        PCL weakL = WeakListeners.create(PCL.class, listener, bean);
        Reference<?> ref = new WeakReference<Object>(listener);
        
        bean.addPCL(weakL);
        
        bean.listeners.firePropertyChange (null, null, null);
        assertEquals ("One call to the listener", 1, listener.cnt);
        listener.cnt = 0;
        
        listener = null;
        assertGC("Listener wasn't GCed", ref);
        
        ref = new WeakReference<Object>(weakL);
        weakL = null;
        assertGC("WeakListener wasn't GCed", ref);
        
        // this shall enforce the removal of the listener
        bean.listeners.firePropertyChange (null, null, null);
        
        assertEquals ("No listeners", 0, bean.listeners.getPropertyChangeListeners ().length);
    }

    @RandomlyFails // NB-Core-Build #1651
    public void testStaticRemoveMethod() throws Exception {
        ChangeListener l = new ChangeListener() {public void stateChanged(ChangeEvent e) {}};
        Singleton.addChangeListener(WeakListeners.change(l, Singleton.class));
        assertEquals(1, Singleton.listeners.size());
        Reference<?> r = new WeakReference<Object>(l);
        l = null;
        assertGC("could collect listener", r);
        assertEquals("called remove method", 0, Singleton.listeners.size());
    }

    public void testIssue156703() throws InterruptedException {
        class Obj {

            Set<PropertyChangeListener> listeners = new LinkedHashSet<PropertyChangeListener>();
            private Thread removedBy;
            int cnt;

            synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                listeners.add(listener);
            }

            synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
                listeners.remove(listener);
                removedBy = Thread.currentThread();
                cnt++;
                notifyAll();
            }

            synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }
        }
        Listener l = new Listener();
        Obj obj = new Obj();
        java.beans.PropertyChangeListener weakL = WeakListeners.propertyChange(l, obj);
        obj.addPropertyChangeListener(weakL);
        obj.firePropertyChange("aa", null, null);
        assertEquals("Listener called once", 1, l.cnt);

        Reference<?> ref = new WeakReference<Object>(l);
        l = null;

        synchronized (obj) {
            assertGC("Can disappear", ref);
            obj.wait();
        }

        assertTrue("Weak listener has been removed", obj.listeners.isEmpty());
        assertEquals("Unregister called just once", 1, obj.cnt);
        assertEquals("Button released from a thread", "Active Reference Queue Daemon", obj.removedBy.getName());

        Reference<?> weakRef = new WeakReference<Object>(weakL);
        weakL = null;
        assertGC("Weak listener can go away as well", weakRef);
    }

    public void testWrapOnlyCheckedExceptions() throws Exception {
        try {
            WeakListeners.create(ChangeListener.class, new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    throw new IllegalStateException();
                }
            }, null).stateChanged(null);
            fail();
        } catch (IllegalStateException x) {
            // OK
        }
        try {
            WeakListeners.create(ChangeListener.class, new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    throw new ThreadDeath();
                }
            }, null).stateChanged(null);
            fail();
        } catch (ThreadDeath x) {
            // OK
        }
    }

    public static class Singleton {
        public static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        public static void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        public static void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
    }
    
    private static final class Listener
    implements PCL, java.beans.PropertyChangeListener, javax.naming.event.ObjectChangeListener {
        public int cnt;
        
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            cnt++;
        }
        
        public void namingExceptionThrown(javax.naming.event.NamingExceptionEvent evt) {
            cnt++;
        }
        
        public void objectChanged(javax.naming.event.NamingEvent evt) {
            cnt++;
        }
    } // end of Listener
    
    private static final class VListener implements VetoableChangeListener {

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        }
    }

    private static final class CListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
        }
    }

    private static final class ImplEventContext extends javax.naming.InitialContext 
    implements javax.naming.event.EventContext {
        public javax.naming.event.NamingListener listener;
        
        public ImplEventContext () throws Exception {
        }
        
        public void addNamingListener(javax.naming.Name target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public void addNamingListener(String target, int scope, javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertNull (listener);
            listener = l;
        }
        
        public synchronized void removeNamingListener(javax.naming.event.NamingListener l) throws javax.naming.NamingException {
            assertEquals ("Removing the same listener", listener, l);
            listener = null;
            notifyAll ();
        }
        
        public boolean targetMustExist() throws javax.naming.NamingException {
            return false;
        }
        
        public synchronized void waitListener () throws Exception {
            int cnt = 0;
            while (listener != null) {
                wait (500);
                if (cnt++ == 5) {
                    fail ("Time out: removeNamingListener was not called at all");
                } else {
                    System.gc ();
                    System.runFinalization();
                }
            }
        }
        
    }
    
    private static class PropChBean {
        private java.beans.PropertyChangeSupport listeners = new java.beans.PropertyChangeSupport (this);
        private void addPCL(PCL l) { listeners.addPropertyChangeListener (l); }
        private void removePCL(PCL l) { listeners.removePropertyChangeListener (l); }
    } // End of PropChBean class

    // just a marker, its name will be used to construct the name of add/remove methods, e.g. addPCL, removePCL
    private static interface PCL extends PropertyChangeListener {
    } // End of PrivatePropL class
    
}
