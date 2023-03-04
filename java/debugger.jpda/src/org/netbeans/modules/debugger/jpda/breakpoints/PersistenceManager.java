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

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Properties.Reader;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints. Watches are loaded by debuggercore's PersistentManager.
 * - listens on all changes of breakpoints and saves new values
 *
 * @author Jan Jancura
 */
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public class PersistenceManager implements LazyDebuggerManagerListener {

    private static Reference<PersistenceManager> instanceRef = new WeakReference<PersistenceManager>(null);

    private Breakpoint[] breakpoints;
    private RequestProcessor.Task saveTask;

    public PersistenceManager() {
        instanceRef = new WeakReference<PersistenceManager>(this);
    }
    
    private boolean areBreakpointsPersisted() {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p = p.getProperties("persistence");
        return p.getBoolean("breakpoints", true);
    }
    
    @Override
    public synchronized Breakpoint[] initBreakpoints () {
        if (!areBreakpointsPersisted()) {
            return new Breakpoint[]{};
        }
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray (
            "jpda", 
            new Breakpoint [0]
        );
        for (int i = 0; i < breakpoints.length; i++) {
            if (breakpoints[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) breakpoints[i];
                try {
                    FileObject fo = URLMapper.findFileObject(new URL(lb.getURL()));
                    if (fo == null) {
                        // The file is gone - we should remove the breakpoint as well.
                        Breakpoint[] breakpoints2 = new Breakpoint[breakpoints.length - 1];
                        if (i > 0) {
                            System.arraycopy(breakpoints, 0, breakpoints2, 0, i);
                        }
                        if (i < breakpoints2.length) {
                            System.arraycopy(breakpoints, i + 1, breakpoints2, i, breakpoints2.length - i);
                        }
                        breakpoints = breakpoints2;
                        i--;
                        continue;
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            breakpoints[i].addPropertyChangeListener(this);
        }
        this.breakpoints = breakpoints;
        return breakpoints;
    }
    
    public synchronized Breakpoint[] unloadBreakpoints() {
        Breakpoint[] bpts = DebuggerManager.getDebuggerManager().getBreakpoints();
        ArrayList<Breakpoint> unloaded = new ArrayList<Breakpoint>();
        for (Breakpoint b : bpts) {
            if (b instanceof JPDABreakpoint) {
                unloaded.add(b);
                b.removePropertyChangeListener(this);
            }
        }
        this.breakpoints = null;
        return unloaded.toArray(new Breakpoint[0]);
    }
    
    @Override
    public void initWatches () {
    }
    
    @Override
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    @Override
    public void breakpointAdded (Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof JPDABreakpoint &&
                !((JPDABreakpoint) breakpoint).isHidden ()) {
            synchronized (this) {
                if (breakpoints == null) {
                    return ;
                }
                int n = breakpoints.length;
                Breakpoint[] newBreakpoints = new Breakpoint[n + 1];
                System.arraycopy(breakpoints, 0, newBreakpoints, 0, n);
                newBreakpoints[n] = breakpoint;
                breakpoints = newBreakpoints;
                storeTheBreakpoints();
            }
            breakpoint.addPropertyChangeListener(this);
        }
    }

    @Override
    public void breakpointRemoved (Breakpoint breakpoint) {
        if (!areBreakpointsPersisted()) {
            return ;
        }
        if (breakpoint instanceof JPDABreakpoint &&
                !((JPDABreakpoint) breakpoint).isHidden ()) {
            synchronized (this) {
                if (breakpoints == null) {
                    return ;
                }
                int n = breakpoints.length;
                for (int i = 0; i < n; i++) {
                    if (breakpoints[i] == breakpoint) {
                        Breakpoint[] newBreakpoints = new Breakpoint[n - 1];
                        if (i > 0) {
                            System.arraycopy(breakpoints, 0, newBreakpoints, 0, i);
                        }
                        if (i < (n-1)) {
                            System.arraycopy(breakpoints, i+1, newBreakpoints, i, n - 1 - i);
                        }
                        n--;
                        breakpoints = newBreakpoints;
                    }
                }
                storeTheBreakpoints();
            }
            breakpoint.removePropertyChangeListener(this);
        }
    }
    @Override
    public void watchAdded (Watch watch) {
    }
    
    @Override
    public void watchRemoved (Watch watch) {
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JPDABreakpoint) {
            if (LineBreakpoint.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                BreakpointsReader r = findBreakpointsReader();
                if (r != null) {
                    // Reset the class name, which might change
                    r.storeCachedClassName((JPDABreakpoint) evt.getSource(), null);
                }
            }
            if (Breakpoint.PROP_VALIDITY.equals(evt.getPropertyName())) {
                return ;
            }
            storeTheBreakpoints();
        }
    }
    
    static BreakpointsReader findBreakpointsReader() {
        BreakpointsReader breakpointsReader = null;
        Iterator i = DebuggerManager.getDebuggerManager().lookup (null, Reader.class).iterator ();
        while (i.hasNext ()) {
            Reader r = (Reader) i.next ();
            String[] ns = r.getSupportedClassNames ();
            if (ns.length == 1 && JPDABreakpoint.class.getName().equals(ns[0])) {
                breakpointsReader = (BreakpointsReader) r;
                break;
            }
        }
        return breakpointsReader;
    }

    static void storeBreakpoints() {
        PersistenceManager pm = instanceRef.get();
        if (pm != null) {
            pm.storeTheBreakpoints();
        }
    }

    private synchronized void storeTheBreakpoints() {
        if (saveTask == null) {
            saveTask = new RequestProcessor("Debugger JPDA Breakpoints storage", 1).create(new SaveTask());
        }
        saveTask.schedule(100);
    }
    
    @Override
    public void sessionAdded (Session session) {}
    @Override
    public void sessionRemoved (Session session) {}
    @Override
    public void engineAdded (DebuggerEngine engine) {}
    @Override
    public void engineRemoved (DebuggerEngine engine) {}
    
    
    private final class SaveTask implements Runnable {

        @Override
        public void run() {
            synchronized (PersistenceManager.this) {
                if (breakpoints == null) {
                    return ;
                }
                Properties.getDefault ().getProperties ("debugger").
                    getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                        "jpda",
                        breakpoints
                    );
            }
        }
        
    }

}
