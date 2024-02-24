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

package org.netbeans.api.debugger;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.openide.util.Cancellable;
import org.openide.util.Task;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.util.Exceptions;


/**
 * The root class of Debugger APIs. DebuggerManager manages list of
 * {@link org.netbeans.api.debugger.Session}s, 
 * {@link org.netbeans.api.debugger.Breakpoint}s and
 * {@link org.netbeans.api.debugger.Watch}es.
 *  
 *
 * <table>
 * <caption>Description of DebuggerManager</caption>
 * <tbody><tr>
 * <td colspan="2" style="background-color:#4D7A97"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td> 
 *
 * <b>Start &amp; finish debugging:</b>
 *    DebuggerManager manages a process of starting a new debugging (
 *    {@link #startDebugging}). It cooperates with all installed
 *    {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s to create a new 
 *    {@link org.netbeans.api.debugger.Session} (or Sessions) and a new 
 *    {@link org.netbeans.api.debugger.DebuggerEngine} (or Engines).
 *    It supports kill all sessions too ({@link #finishAllSessions}).
 *
 * <br><br>
 * <b>Sessions management:</b>
 *    DebuggerManager keeps list of all 
 *    {@link org.netbeans.api.debugger.Session}s ({@link #getSessions}),
 *    and manages current session ({@link #getCurrentSession},
 *    {@link #setCurrentSession}).
 *
 * <br><br>
 * <b>Engine management:</b>
 *    DebuggerManager provides current engine ({@link #getCurrentEngine}).
 *    Current engine is derivated from current session. So,
 *    <i>
 *    debuggerManager.getCurrentEngine () == debuggerManager.
 *    getCurrentSession.getCurrentEngine ()
 *    </i>
 *    should be always true.
 *
 * <br><br>
 * <b>Breakpoints management:</b>
 *    DebuggerManager keeps list of all shared breakpoints 
 *    ({@link #getBreakpoints}).
 *    Breakpoint can be added ({@link #addBreakpoint}) and removed
 *    ({@link #removeBreakpoint}).
 *
 * <br><br>
 * <b>Watches management:</b>
 *    DebuggerManager keeps list of all shared watches ({@link #getWatches}).
 *    Watch can be created &amp; added ({@link #createWatch}).
 *
 * <br><br>
 * <b>Support for listening:</b>
 *    DebuggerManager propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link org.netbeans.api.debugger.DebuggerManagerListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td> 
 *
 * DebuggerCore module should be the only one provider of this abstract class.
 * This class should be called from debugger plug-in modules and from debugger
 * UI modules. 
 * 
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td> 
 *
 * The only one instance of DebuggerManager should exist, and it should be 
 * created in {@link #getDebuggerManager} method.
 * 
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can 
 * be added.
 *
 * </td></tr></tbody></table>
 *
 * @author Jan Jancura
 */
public final class DebuggerManager implements ContextProvider {
    
    // TODO: deprecate all these properties. They are useless, since there are
    //       dedicated methods in DebuggerManagerListener
    
    // OR: Remove DebuggerManagerListener and use just the properties.
    // - probably not possible because of initBreakpoints() method.
    
    /** Name of property for the set of breakpoints in the system. */
    public static final String                PROP_BREAKPOINTS_INIT = "breakpointsInit"; // NOI18N
    
    /** Name of property for the set of breakpoints in the system. */
    public static final String                PROP_BREAKPOINTS = "breakpoints"; // NOI18N

    /** Name of property for current debugger engine. */
    public static final String                PROP_CURRENT_ENGINE = "currentEngine";

    /** Name of property for current debugger session. */
    public static final String                PROP_CURRENT_SESSION = "currentSession";
    
    /** Name of property for set of running debugger sessions. */
    public static final String                PROP_SESSIONS = "sessions";
    
    /** Name of property for set of running debugger engines. */
    public static final String                PROP_DEBUGGER_ENGINES = "debuggerEngines";

    /** Name of property for the set of watches in the system. */
    public static final String                PROP_WATCHES = "watches"; // NOI18N

    /** Name of property for the set of watches in the system. */
    public static final String                PROP_WATCHES_INIT = "watchesInit"; // NOI18N
    
    
    private static DebuggerManager            debuggerManager;
    private Session                           currentSession;
    private DebuggerEngine                    currentEngine;
    private final List<Session>               sessions = new ArrayList<>();
    private final Set                         engines = new HashSet ();
    private final Vector<Breakpoint>          breakpoints = new Vector<>();
    private boolean                           breakpointsInitializing = false;
    private boolean                           breakpointsInitialized = false;
    private final Vector<Watch>               watches = new Vector<>();
    private boolean                           watchesInitialized = false;
    private ThreadLocal<Boolean>              watchesInitializing = new ThreadLocal<Boolean>();
    private SessionListener                   sessionListener = new SessionListener ();
    private Vector<DebuggerManagerListener>   listeners = new Vector<>();
    private final Map<String, Vector<DebuggerManagerListener>>                     listenersMap = new HashMap<>();
    private ActionsManager                    actionsManager = null;
    
    private Lookup                            lookup = new Lookup.MetaInf (null);
    
    //private ModuleUnloadListeners             moduleUnloadListeners = new ModuleUnloadListeners();
    
    
    /**
     * Returns default instance of DebuggerManager.
     *
     * @return default instance of DebuggerManager
     */
    public static synchronized DebuggerManager getDebuggerManager () {
        if (debuggerManager == null) 
            debuggerManager = new DebuggerManager ();
        return debuggerManager;
    }

    /**
     * Creates a new instance of DebuggerManager.
     * It's called from a synchronized block, do not call any foreign code from here.
     */
    private DebuggerManager () {
    }


    public synchronized ActionsManager getActionsManager () {
        if (actionsManager == null)
            actionsManager = new ActionsManager (lookup);
        return actionsManager;
    }
    
    
    // lookup management .............................................
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        return lookup.lookupFirst (folder, service);
    }
    
    /**
     * Join two lookups together.
     * The result will merge the lookups.
     * The result of its {@link #lookup(String,Class)} method will additionally implement {@link Customizer}.
     * @param cp1 first lookup
     * @param cp2 second lookup
     * @return a merger of the two
     * @since org.netbeans.api.debugger/1 1.13
     */
    public static ContextProvider join(ContextProvider cp1, ContextProvider cp2) {
        return new Lookup.Compound(cp1, cp2);
    }
    
    
    // session / engine management .............................................
    
    /** 
     * Start a new debugging for given 
     * {@link org.netbeans.api.debugger.DebuggerInfo}. DebuggerInfo provides
     * information needed to start new debugging. DebuggerManager finds
     * all {@link org.netbeans.spi.debugger.SessionProvider}s and 
     *  {@link org.netbeans.spi.debugger.DelegatingSessionProvider}s
     * installed for given DebuggerInfo, and creates a new 
     * {@link Session}(s). 
     * After that it looks for all 
     * {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s and 
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider}s
     * installed for Session, and crates a new 
     * {@link DebuggerEngine}(s).
     * <br>
     * If the implementation of ACTION_START providers support cancellation (implements {@link Cancellable}),
     * this startup sequence can be canceled via Thread.interrupt() while
     * startDebugging() method is waiting for the action providers.
     *
     * @param info debugger startup info
     * @return DebuggerEngines started for given info
     */
    public DebuggerEngine[] startDebugging (DebuggerInfo info) {
        //S ystem.out.println("@StartDebugging info: " + info);
        
        // init sessions
        List sessionProviders = new ArrayList<>();
        List<DebuggerEngine> engines = new ArrayList<>();
        Lookup l = info.getLookup ();
        Lookup l2 = info.getLookup ();
        synchronized (l) {
            sessionProviders.addAll (
                l.lookup (
                    null,
                    SessionProvider.class
                )
            );
            sessionProviders.addAll (
                l.lookup (
                    null,
                    DelegatingSessionProvider.class
                )
            );
        }
        Session sessionToStart = null;
        int i, k = sessionProviders.size ();
        for (i = 0; i < k; i++) {
            Session s = null;
            if (sessionProviders.get (i) instanceof DelegatingSessionProvider) {
                s = ((DelegatingSessionProvider) sessionProviders.get (i)).
                    getSession (info);
                l = new Lookup.Compound (
                    l,
                    s.privateLookup
                );
                //S ystem.out.println("@  StartDebugging DelegaingSession: " + s);
            } else {
                SessionProvider sp = (SessionProvider) sessionProviders.get (i);
                if (sp.getSessionName() == null) throw new NullPointerException("<null> session name provided by: "+sp);
                if (sp.getTypeID() == null) throw new NullPointerException("<null> type ID provided by: "+sp);
                if (sp.getServices() == null) throw new NullPointerException("<null> services provided by: "+sp);
                s = new Session (
                    sp.getSessionName (),
                    sp.getLocationName (),
                    sp.getTypeID (),
                    sp.getServices (),
                    l
                );
                sessionToStart = s;
                l = s.getLookup ();
                l2 = s.getLookup ();
                addSession (s);
                //S ystem.out.println("@  StartDebugging new Session: " + s);
            }
            
            // init DebuggerEngines
            List<Object> engineProviders = new ArrayList<>();
            synchronized (l2) {
                engineProviders.addAll (
                    l2.lookup (null, DebuggerEngineProvider.class)
                );
                engineProviders.addAll (
                    l2.lookup (null, DelegatingDebuggerEngineProvider.class)
                );
            }
            int j, jj = engineProviders.size ();
            for (j = 0; j < jj; j++) {
                DebuggerEngine engine = null;
                String[] languages = null; 
                if (engineProviders.get (j) instanceof DebuggerEngineProvider) {
                    DebuggerEngineProvider ep = (DebuggerEngineProvider) 
                        engineProviders.get (j);
                    Object[] services = ep.getServices ();
                    engine = new DebuggerEngine (
                        ep.getEngineTypeID (),
                        s,
                        services,
                        l
                    );
                    languages = ep.getLanguages ();
                    ep.setDestructor (engine.new Destructor ());
                    engines.add (engine);
                    //S ystem.out.println("@    StartDebugging new Engine: " + engine);
                } else {
                    DelegatingDebuggerEngineProvider dep = 
                        (DelegatingDebuggerEngineProvider) 
                        engineProviders.get (j);
                    languages = dep.getLanguages ();
                    engine = dep.getEngine ();
                    dep.setDestructor (engine.new Destructor ());
                    //S ystem.out.println("@    StartDebugging DelegatingEngine: " + engine);
                }
                int w, ww = languages.length;
                for (w = 0; w < ww; w++)
                    s.addLanguage (languages [w], engine);
            }
        }
        
        if (sessionToStart != null) {
            GestureSubmitter.logDebugStart(sessionToStart, engines);
            if (currentSession == null) {
                // Set the new session as current only when there is no existing
                // running session. The current session can be important
                // and it's the debugger's responsibility to set itself as current
                // when it thinks it needs an attention.
                setCurrentSession (sessionToStart);
            }
        }

        k = engines.size ();
        for (i = 0; i < k; i++) {
            if (Thread.interrupted()) {
                break;
            }
            Task task = engines.get(i).getActionsManager ().postAction
                (ActionsManager.ACTION_START);
            if (task instanceof Cancellable) {
                try {
                    task.waitFinished(0);
                } catch (InterruptedException iex) {
                    if (((Cancellable) task).cancel()) {
                        break;
                    } else {
                        task.waitFinished();
                    }
                }
            } else {
                task.waitFinished();
            }
        }
        if (i < k) { // It was canceled
            int n = i + 1;
            for (i = 0; i < k; i++) {
                ActionsManager am = engines.get(i).getActionsManager();
                if (i < (n - 1)) am.postAction(ActionsManager.ACTION_KILL); // kill the started engines
                am.destroy();
            }
            if (sessionToStart != null) {
                sessionToStart.kill();
            }
            return new DebuggerEngine[] {};
        }
        
        DebuggerEngine[] des = new DebuggerEngine [engines.size ()];
        return engines.toArray (des);
    }

    /**
     * Kills all {@link org.netbeans.api.debugger.Session}s and
     * {@link org.netbeans.api.debugger.DebuggerEngine}s.
     */
    public void finishAllSessions () {
        Session[] ds = getSessions ();
        
        if (ds.length == 0) return;

        // finish all non persistent sessions
        int i, k = ds.length;
        for (i = 0; i < k; i++)
            ds [i].getCurrentEngine ().getActionsManager ().
                doAction (ActionsManager.ACTION_KILL);
    }
    
    /**
     * Returns current debugger session or <code>null</code>.
     *
     * @return current debugger session or <code>null</code>
     */
    public Session getCurrentSession () {
        return currentSession;
    }

    /**
     * Sets current debugger session.
     *
     * @param session a session to be current
     */
    public void setCurrentSession (Session session) {
        Session oldSession;
        Session newSession;
        DebuggerEngine oldEngine;
        DebuggerEngine newEngine;
        synchronized (sessions) {
            // 1) check if the session is registerred
            if (session != null) {
                int i, k = sessions.size();
                for (i = 0; i < k; i++)
                    if (session == sessions.get(i)) break;
                if (i == k) 
                    return;
            }
            
            // fire all changes
            oldSession = getCurrentSession ();
            if (session == oldSession) return;
            currentSession = newSession = session;
            
            oldEngine = currentEngine;
            newEngine = null;
            if (getCurrentSession () != null)
                newEngine = getCurrentSession ().getCurrentEngine ();
            currentEngine = newEngine;
        }
        if (oldEngine != newEngine) {
            firePropertyChange (PROP_CURRENT_ENGINE, oldEngine, newEngine);
        }
        firePropertyChange (PROP_CURRENT_SESSION, oldSession, newSession);
    }

    /**
     * Returns set of running debugger sessions.
     *
     * @return set of running debugger sessions
     */
    public Session[] getSessions () {
        synchronized (sessions) {
            return (Session[]) sessions.toArray(new Session[0]);
        }
    }

    /**
     * Returns set of running debugger engines.
     *
     * @return set of running debugger engines
     */
    public DebuggerEngine[] getDebuggerEngines () {
        synchronized (engines) {
            return (DebuggerEngine[]) engines.toArray (new DebuggerEngine [0]);
        }
    }
    
    /**
     * Returns current debugger engine or <code>null</code>.
     *
     * @return current debugger engine or <code>null</code>
     */
    public DebuggerEngine getCurrentEngine () {
        return currentEngine;
    }

    
    // breakpoints management ..................................................
    
    /** 
     * Adds a new breakpoint.
     *
     * @param breakpoint a new breakpoint
     */
    public void addBreakpoint (Breakpoint breakpoint) {
        if (initBreakpoints (breakpoint)) {
            // do not add one breakpoint more than once.
            if (registerBreakpoint(breakpoint)) {
                breakpoints.addElement(breakpoint);
                fireBreakpointCreated (breakpoint, null);
            }
        }
    }
    
    private boolean registerBreakpoint(Breakpoint breakpoint) {
        Class c = breakpoint.getClass();
        ClassLoader cl = c.getClassLoader();
        synchronized (breakpointsByClassLoaders) {
            Set<Breakpoint> lb = breakpointsByClassLoaders.get(cl);
            if (lb == null) {
                lb = new IdentityHashSet<Breakpoint>();
                breakpointsByClassLoaders.put(cl, lb);
                //moduleUnloadListeners.listenOn(cl);
            }
            return lb.add(breakpoint);
        }
    }
    
    /** 
     * Removes breakpoint.
     *
     * @param breakpoint a breakpoint to be removed
     */
    public void removeBreakpoint (
        Breakpoint breakpoint
    ) {
        removeBreakpoint(breakpoint, false);
    }
    
    private void removeBreakpoint (
        Breakpoint breakpoint, boolean ignoreInitBreakpointsListeners
    ) {
        if (!ignoreInitBreakpointsListeners) {
            initBreakpoints ();
            Class c = breakpoint.getClass();
            ClassLoader cl = c.getClassLoader();
            synchronized (breakpointsByClassLoaders) {
                Set<Breakpoint> lb = breakpointsByClassLoaders.get(cl);
                if (lb != null) {
                    lb.remove(breakpoint);
                    if (lb.isEmpty()) {
                        breakpointsByClassLoaders.remove(cl);
                    }
                }
            }
            breakpoints.removeElement (breakpoint);
            breakpoint.disposeOut();
        } else {
            breakpoints.removeElement (breakpoint);
            breakpoint.dispose();
        }
        fireBreakpointRemoved (breakpoint, ignoreInitBreakpointsListeners, null);
    }

    /** 
     * Gets all registered breakpoints.
     *
     * @return all breakpoints
     */
    public Breakpoint[] getBreakpoints () {
        initBreakpoints ();
        return (Breakpoint[]) breakpoints.toArray(new Breakpoint[0]);
    }
    
    private void moduleUnloaded(ClassLoader cl) {
        Set<Breakpoint> lb;
        synchronized (breakpointsByClassLoaders) {
            lb = breakpointsByClassLoaders.remove(cl);
        }
        if (lb == null) return ;
        for (Breakpoint b : lb) {
            removeBreakpoint(b, true);
        }
    }

    
    // watches management ......................................................

    /** 
     * Creates a watch with its expression set to an initial value.
     * Also allows creation of a hidden watch (not presented to the user), 
     * for example for internal use in the editor to obtain values of variables
     * under the mouse pointer.
     *
     * @param expr expression to watch for (the format is the responsibility 
     *    of the debugger plug-in implementation, but it is typically 
     *    a variable name).
     * @return the new watch
     */
    public Watch createWatch (String expr) {
        Watch w = new Watch (expr);
        if (Boolean.TRUE.equals(watchesInitializing.get())) {
            watches.addElement (w);
        } else {
            initWatches ();
            watches.addElement (w);
            fireWatchCreated (w);
        }
        return w;
    }

    /**
     * Creates a watch with its expression set to an initial value
     * and add it at the specific position
     *
     * @param index the position at which the specified watch is to be inserted
     * @param expr expression to watch for (the format is the responsibility
     *    of the debugger plug-in implementation, but it is typically
     *    a variable name).
     * @return the new watch
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *         <code>(index &lt; 0 || index &gt; getWatches().length)</code>
     * @since 1.22
     */
    public Watch createWatch (int index, String expr) {
        Watch w = new Watch (expr);
        if (Boolean.TRUE.equals(watchesInitializing.get())) {
            watches.add (index, w);
        } else {
            initWatches ();
            watches.add (index, w);
            fireWatchCreated (w);
        }
        return w;
    }
    
    /**
     * Create a watch pinned at the specified pin location.
     * @param expr expression to watch for (the format is the responsibility
     *    of the debugger plug-in implementation, but it is typically
     *    a variable name).
     * @param pin A pin where the watch should be pinned at.
     * @return the new watch
     * @since 1.54
     */
    public Watch createPinnedWatch(String expr, Watch.Pin pin) {
        Watch w = new Watch (expr, pin);
        if (Boolean.TRUE.equals(watchesInitializing.get())) {
            watches.addElement (w);
        } else {
            initWatches ();
            watches.addElement (w);
            fireWatchCreated (w);
        }
        return w;
    }

    /**
    * Gets all shared watches in the system.
    *
    * @return all watches
    */
    public Watch[] getWatches () {
        initWatches ();
        return (Watch[]) watches.toArray(new Watch[0]);
    }

    /**
     * Removes all watches from the system.
     */
    public void removeAllWatches () {
        initWatches ();
        Vector v = (Vector) watches.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            ((Watch) v.elementAt (i)).remove ();
    }

    /**
    * Removes watch.
    *
    * @param w watch to be removed
    */
    void removeWatch (Watch w) {
        initWatches ();
        watches.removeElement (w);
        fireWatchRemoved (w);
    }

    /**
     * Reorders watches with given permutation.
     * @param permutation The permutation with the length of current watches list
     * @throws IllegalArgumentException if the permutation is not valid permutation
     * @since 1.22
     */
    public void reorderWatches(final int[] permutation) throws IllegalArgumentException {
        synchronized (watches) {
            if (permutation.length != watches.size()) {
                throw new IllegalArgumentException("Permutation of length "+permutation.length+", but have "+watches.size()+" watches.");
            }
            checkPermutation(permutation);
            Vector<Watch> v = new Vector<>(watches);
            for (int i = 0; i < v.size(); i++) {
                watches.set(permutation[i], v.get(i));
            }
        }
    }

    private static void checkPermutation(int[] permutation) throws IllegalArgumentException {
        int max = permutation.length;
        int[] check = new int[max];
        for (int i = 0; i < max; i++) {
            int p = permutation[i];
            if (p >= max) {
                throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+", which is bigger than the length of the permutation.");
            }
            if (p < 0) {
                throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+", which is negative.");
            }
            if (check[p] != 0) {
                throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+" twice or more times.");
            }
            check[p] = 1;
        }
    }
    
    // listenersMap ...............................................................

    
    /**
    * Fires property change.
    */
    private void firePropertyChange (String name, Object o, Object n) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listeners.clone ();
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (name);
            if (l1 != null)
                l1 = (Vector) l1.clone ();
        }
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, name, o, n
        );
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener)l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener)l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
    * This listener notificates about changes of breakpoints, watches and threads.
    *
    * @param l listener object.
    */
    public void addDebuggerListener (DebuggerManagerListener l) {
        listeners.addElement (l);
    }

    /**
    * Removes debugger listener.
    *
    * @param l listener object.
    */
    public void removeDebuggerListener (DebuggerManagerListener l) {
        listeners.removeElement (l);
    }

    /**
     * Removes debugger listener.
     * Does the same as {@link #removeDebuggerListener(org.netbeans.api.debugger.DebuggerManagerListener)},
     * but uses the standard naming pattern, so that it can be called by
     * {@link org.openide.util.WeakListeners}.
     *
     * @param l listener object.
     */
    // DO NOT REMOVE, used by WeakListeners, by reflection!
    private void removeDebuggerManagerListener (DebuggerManagerListener l) {
        listeners.removeElement (l);
    }

    /** 
     * Add a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to add
     */
    public void addDebuggerListener(String propertyName, DebuggerManagerListener l) {
        synchronized (listenersMap) {
            Vector<DebuggerManagerListener> listeners = (Vector<DebuggerManagerListener>)listenersMap.get(propertyName);
            if (listeners == null) {
                listeners = new Vector<>();
                listenersMap.put(propertyName, listeners);
            }
            listeners.addElement (l);
        }
    }
    
    /** 
     * Remove a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to remove
     */
    public void removeDebuggerListener (
        String propertyName, 
        DebuggerManagerListener l
    ) {
        synchronized (listenersMap) {
            Vector listeners = (Vector) listenersMap.get (propertyName);
            if (listeners == null) return;
            listeners.removeElement (l);
            if (listeners.size () == 0)
                listenersMap.remove (propertyName);
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was created
     */
    private void fireBreakpointCreated (final Breakpoint breakpoint, final DebuggerManagerListener originatingListener) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            DebuggerManagerListener dl = (DebuggerManagerListener) l.elementAt (i);
            if (dl != originatingListener) {
                try {
                    dl.breakpointAdded (breakpoint);
                    dl.propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                DebuggerManagerListener dl = (DebuggerManagerListener) l1.elementAt (i);
                if (dl != originatingListener) {
                    try {
                        dl.breakpointAdded (breakpoint);
                        dl.propertyChange (ev);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    /**
     * Notifies registered listenersMap about a change.
     * Notifies {@link #listeners registered listenersMap} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireBreakpointRemoved (final Breakpoint breakpoint,
                                        boolean ignoreInitBreakpointsListeners,
                                        DebuggerManagerListener ignoredListener) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            DebuggerManagerListener ml = (DebuggerManagerListener) l.elementAt(i);
            if (ml == ignoredListener) continue;
            try {
                Breakpoint[] bps;
                if (ignoreInitBreakpointsListeners && (bps = ml.initBreakpoints()) != null && bps.length > 0) {
                    continue;
                }
                ml.breakpointRemoved(breakpoint);
                ml.propertyChange (ev);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                DebuggerManagerListener ml = (DebuggerManagerListener) l1.elementAt(i);
                if (ml == ignoredListener) continue;
                try {
                    Breakpoint[] bps;
                    if (ignoreInitBreakpointsListeners && (bps = ml.initBreakpoints()) != null && bps.length > 0) {
                        continue;
                    }
                    ml.breakpointRemoved(breakpoint);
                    ml.propertyChange (ev);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        }
    }

    private void initBreakpoints () {
        initBreakpoints(null);
    }
    
    private List<Breakpoint> createdBreakpoints;
    private final Map<ClassLoader, Set<Breakpoint>> breakpointsByClassLoaders =
            new HashMap<ClassLoader, Set<Breakpoint>>();
    
    /**
     * @param newBreakpoint a breakpoint that is to be added if the breakpoints are not yet initialized.
     * @return true if the breakpoints were successfully initialized.
     */
    private boolean initBreakpoints(Breakpoint newBreakpoint) {
        // All is under the lock, including DebuggerManagerListener.initBreakpoints()
        // and DebuggerManagerListener.propertyChange(..PROP_BREAKPOINTS_INIT..) calls.
        // Clients should return the breakpoints via that listener, not add them
        // directly. Therefore this should not lead to deadlock...
        Map<Breakpoint, DebuggerManagerListener> originatingListeners;
        List<Breakpoint> breakpointsToAdd;
        synchronized (breakpoints) {
            if (breakpointsInitialized) return true;
            if (breakpointsInitializing) {
                if (newBreakpoint != null) {
                    // Someone is trying to add new breakpoints during initialization process.
                    // We must permit that doue to historical reasons - see web/jspdebug/src/org/netbeans/modules/web/debug/breakpoints/JspLineBreakpoint.java
                    createdBreakpoints.add(newBreakpoint);
                    return false;
                }
                throw new IllegalStateException("Breakpoints not yet initialized and tried to initialize again...");
            }
            breakpointsInitializing = true;
            try {
                initDebuggerManagerListeners ();

                createdBreakpoints = new ArrayList<>();
                originatingListeners = new HashMap<>();

                Vector l = (Vector) listeners.clone ();
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    DebuggerManagerListener dl = (DebuggerManagerListener) l.elementAt (i);
                    Breakpoint[] bkpts = getInitialBreakpoints(dl);
                    if (bkpts != null) {
                        createdBreakpoints.addAll (Arrays.asList (bkpts));
                        for (int j = 0; j < bkpts.length; j++) {
                            originatingListeners.put(bkpts[j], dl);
                        }
                    }
                }

                Vector l1;
                synchronized (listenersMap) {
                    l1 = (Vector) listenersMap.get (PROP_BREAKPOINTS_INIT);
                    if (l1 != null) {
                        l1 = (Vector) l1.clone ();
                    }
                }
                if (l1 != null) {
                    k = l1.size ();
                    for (i = 0; i < k; i++) {
                        DebuggerManagerListener dl = (DebuggerManagerListener) l1.elementAt (i);
                        Breakpoint[] bkpts = getInitialBreakpoints(dl);
                        if (bkpts != null) {
                            createdBreakpoints.addAll (Arrays.asList (bkpts));
                            for (int j = 0; j < bkpts.length; j++) {
                                originatingListeners.put(bkpts[j], dl);
                            }
                        }
                    }
                }

                breakpoints.addAll(createdBreakpoints);
            } finally {
                breakpointsInitializing = false;
            }
            breakpointsInitialized = true;
            breakpointsToAdd = createdBreakpoints;
            createdBreakpoints = null;
        }
        for (Breakpoint bp : breakpointsToAdd) {
            registerBreakpoint(bp);
            fireBreakpointCreated (bp, originatingListeners.get(bp));
        }
        return true;
    }

    private void addBreakpoints(DebuggerManagerListener dl) {
        if (!breakpointsInitialized) {
            return ;
        }
        //System.err.println("\n addBreakpoints("+dl+")\n");
        Map<Breakpoint, DebuggerManagerListener> originatingListeners = new HashMap<Breakpoint, DebuggerManagerListener>();
        List<Breakpoint> breakpointsToAdd;
        synchronized (breakpoints) {
            breakpointsInitialized = false;
            breakpointsInitializing = true;
            try {
                createdBreakpoints = new ArrayList<Breakpoint>();
                Breakpoint[] bps = getInitialBreakpoints(dl);
                if (bps != null) {
                    createdBreakpoints.addAll (Arrays.asList(bps));
                    for (int j = 0; j < bps.length; j++) {
                        originatingListeners.put(bps[j], dl);
                    }
                }
                //System.err.println("createdBreakpoints = "+createdBreakpoints);
                breakpoints.addAll(createdBreakpoints);
            } finally {
                breakpointsInitializing = false;
                breakpointsInitialized = true; 
            }
            breakpointsToAdd = createdBreakpoints;
            createdBreakpoints = null;
        }
        //System.err.println("createdBreakpoints = "+breakpointsToAdd);
        for (Breakpoint bp : breakpointsToAdd) {
            registerBreakpoint(bp);
            fireBreakpointCreated (bp, originatingListeners.get(bp));
        }
    }
    
    private static Breakpoint[] getInitialBreakpoints(DebuggerManagerListener dl) {
        Breakpoint[] bps;
        try {
            bps = dl.initBreakpoints();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
            bps = null;
        }
        if (bps == null) {
            return null;
        }
        int nn = 0;
        for (int i = 0; i < bps.length; i++) {
            if (bps[i] == null) { // Should be an exception
                nn++;
            }
        }
        if (nn > 0) {
            if (nn == bps.length) {
                return null;
            }
            Breakpoint[] bps2 = new Breakpoint[bps.length - nn];
            int i = 0, j = 0;
            while (i < bps.length) {
                if (bps[i] != null) {
                    bps2[j] = bps[i];
                    j++;
                }
                i++;
            }
            bps = bps2;
        }
        return bps;
    }

    private void removeBreakpoints(DebuggerManagerListener dl) {
        if (!breakpointsInitialized) {
            return ;
        }
        Breakpoint[] bps;
        try {
            java.lang.reflect.Method unloadMethod = dl.getClass().getMethod("unloadBreakpoints", new Class[] {});
            bps = (Breakpoint[]) unloadMethod.invoke(dl, new Object[] {});
        } catch (IllegalAccessException iaex) {
            return ;
        } catch (IllegalArgumentException iaex) {
            return ;
        } catch (NoSuchMethodException iaex) {
            return ;
        } catch (SecurityException iaex) {
            return ;
        } catch (InvocationTargetException iaex) {
            Exceptions.printStackTrace(iaex.getTargetException());
            return ;
        }
        //Breakpoint[] bps = dl.unloadBreakpoints();
        //System.err.println("\n removeBreakpoints("+dl+")\n");
        breakpoints.removeAll(Arrays.asList(bps));
        for (Breakpoint breakpoint : bps) {
            Class c = breakpoint.getClass();
            ClassLoader cl = c.getClassLoader();
            synchronized (breakpointsByClassLoaders) {
                Set<Breakpoint> lb = breakpointsByClassLoaders.get(cl);
                if (lb != null) {
                    lb.remove(breakpoint);
                    if (lb.isEmpty()) {
                        breakpointsByClassLoaders.remove(cl);
                    }
                }
            }
            breakpoint.disposeOut();
        }
        //System.err.println("removedBreakpoints = "+Arrays.asList(bps));
        for (Breakpoint bp : bps) {
            fireBreakpointRemoved(bp, false, dl);
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a watch
     * {@link DebuggerManagerListener#watchAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param watch  a watch that was created
     */
    private void fireWatchCreated (final Watch watch) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).watchAdded (watch);
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_WATCHES);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).watchAdded (watch);
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a watch
     * {@link DebuggerManagerListener#watchRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param watch  a watch that was removed
     */
    private void fireWatchRemoved (final Watch watch) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).watchRemoved (watch);
                // TODO: fix nonsense double firing
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_WATCHES);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).watchRemoved (watch);
                    // TODO: fix nonsense double firing
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private void initWatches () {
        initDebuggerManagerListeners();
        synchronized (watches) {
            if (watchesInitialized) return ;
            watchesInitialized = true;
            try {
                watchesInitializing.set(Boolean.TRUE);
                // All is synchronized, initWatches() implementations should call just createWatch()
                PropertyChangeEvent ev = new PropertyChangeEvent (
                    this, PROP_WATCHES_INIT, null, null
                );

                Vector l = (Vector) listeners.clone ();
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    try {
                        ((DebuggerManagerListener) l.elementAt (i)).initWatches ();
                        ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                Vector l1;
                synchronized (listenersMap) {
                    l1 = (Vector) listenersMap.get (PROP_WATCHES_INIT);
                    if (l1 != null) {
                        l1 = (Vector) l1.clone ();
                    }
                }
                if (l1 != null) {
                    k = l1.size ();
                    for (i = 0; i < k; i++) {
                        try {
                            ((DebuggerManagerListener) l1.elementAt (i)).initWatches ();
                            ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            } finally {
                watchesInitializing.set(Boolean.FALSE);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a session
     * {@link DebuggerManagerListener#sessionAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param session a session that was created
     */
    private void fireSessionAdded (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).sessionAdded(session);
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_SESSIONS);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).sessionAdded(session);
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a session
     * {@link DebuggerManagerListener#sessionRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param session a session that was removed
     */
    private void fireSessionRemoved (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).sessionRemoved(session);
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_SESSIONS);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).sessionRemoved(session);
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a engine
     * {@link DebuggerManagerListener#engineAdded was added}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param engine a engine that was created
     */
    private void fireEngineAdded (
        final DebuggerEngine engine,
        final DebuggerEngine[] old,
        final DebuggerEngine[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_DEBUGGER_ENGINES, old, ne
        );
        
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).engineAdded(engine);
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_DEBUGGER_ENGINES);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).engineAdded(engine);
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listeners registered listeners} that a engine
     * {@link DebuggerManagerListener#engineRemoved was removed}
     * and its properties
     * {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * were changed.
     *
     * @param engine a engine that was removed
     */
    private void fireEngineRemoved (
        final DebuggerEngine engine,
        final DebuggerEngine[] old,
        final DebuggerEngine[] ne
    ) {
        initDebuggerManagerListeners ();
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_DEBUGGER_ENGINES, old, ne
        );

        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            try {
                ((DebuggerManagerListener) l.elementAt (i)).engineRemoved(engine);
                ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Vector l1;
        synchronized (listenersMap) {
            l1 = (Vector) listenersMap.get (PROP_DEBUGGER_ENGINES);
            if (l1 != null) {
                l1 = (Vector) l1.clone ();
            }
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                try {
                    ((DebuggerManagerListener) l1.elementAt (i)).engineRemoved(engine);
                    ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    
    // helper methods ....................................................

    private Set<LazyDebuggerManagerListener> loadedListeners;
    private List<? extends LazyDebuggerManagerListener> listenersLookupList;
    private final Object loadedListenersLock = new Object();
    
    private void initDebuggerManagerListeners () {
        synchronized (loadedListenersLock) {
            if (loadedListeners == null) {
                loadedListeners = new HashSet<LazyDebuggerManagerListener>();
                listenersLookupList = lookup.lookup (null, LazyDebuggerManagerListener.class);
                refreshDebuggerManagerListeners(listenersLookupList);
                ((Customizer) listenersLookupList).addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        refreshDebuggerManagerListeners((List<? extends LazyDebuggerManagerListener>) evt.getSource());
                    }
                });
            }

        }
    }
    
    private void refreshDebuggerManagerListeners(List<? extends LazyDebuggerManagerListener> listenersLookupList) {
        //System.err.println("\n refreshDebuggerManagerListeners()");
        //It's neccessary to pay attention on the order in which the listeners and breakpoints are registered!
        //Annotation listeners must be unregistered AFTER breakpoints are removed
        //and registered back BEFORE breakpoints are loaded
        Set<LazyDebuggerManagerListener> addedInitBreakpointsListeners = new HashSet<LazyDebuggerManagerListener>();
        //Set<ClassLoader> uninstalledModules = new HashSet<ClassLoader>();
        synchronized (listenersLookupList) {
            int i, k = listenersLookupList.size ();
            //System.err.println("size() = "+k+",  content = "+listenersLookupList+"\n");
            for (i = 0; i < k; i++) {
                LazyDebuggerManagerListener l = listenersLookupList.get (i);
                if (loadedListeners.add(l)) {
                    String[] props = l.getProperties ();
                    if ((props == null) || (props.length == 0)) {
                        addDebuggerListener (l);
                    } else {
                        int j, jj = props.length;
                        for (j = 0; j < jj; j++) {
                            addDebuggerListener (props [j], l);
                        }
                    }
                    //System.err.println("ADDED listener: "+l);
                    addedInitBreakpointsListeners.add(l);
                }
            }
            Set<LazyDebuggerManagerListener> listenersToRemove = new HashSet<LazyDebuggerManagerListener>(loadedListeners);
            listenersToRemove.removeAll(listenersLookupList);
            for (LazyDebuggerManagerListener l : listenersToRemove) {
                if (loadedListeners.contains(l)) {
                    removeBreakpoints(l);
                }
            }
            for (LazyDebuggerManagerListener l : listenersToRemove) {
                if (loadedListeners.remove(l)) {
                    moduleUnloaded(l.getClass().getClassLoader());
                    String[] props = l.getProperties ();
                    if ((props == null) || (props.length == 0)) {
                        removeDebuggerListener (l);
                    } else {
                        int j, jj = props.length;
                        for (j = 0; j < jj; j++) {
                            removeDebuggerListener (props [j], l);
                        }
                    }
                    //System.err.println("REMOVED listener: "+l);
                }
            }
        }
        for (LazyDebuggerManagerListener l : addedInitBreakpointsListeners) {
            addBreakpoints(l);
        }
    }
    
    private void addSession (Session session) {
        Session[] oldSessions;
        Session[] newSessions;
        synchronized (sessions) {
            oldSessions = getSessions();
            int i, k = oldSessions.length;
            for (i = 0; i < k; i++)
                if (session == oldSessions[i]) return;

            newSessions = new Session [oldSessions.length + 1];
            System.arraycopy (oldSessions, 0, newSessions, 0, oldSessions.length);
            newSessions[oldSessions.length] = session;
            this.sessions.add(session);

            session.addPropertyChangeListener (sessionListener);
        }
        fireSessionAdded (session, oldSessions, newSessions);
    }
    
    private void removeSession (Session session) {
        Session[] oldSessions;
        Session[] newSessions;
        Session nCurrentSesson = null;
        synchronized (sessions) {
            oldSessions = getSessions();
            // find index of given debugger and new instance of currentDebugger
            int i, k = oldSessions.length;
            for (i = 0; i < k; i++) {
                if (oldSessions[i] == session) {
                    break;
                } else if (nCurrentSesson == null) {
                    nCurrentSesson = oldSessions[i];
                }
            }
            if (i == k) return; // this debugger is not registered
            
            // set new current debugger session
            if (session == getCurrentSession ()) {
                if ((nCurrentSesson == null) && (k > 1))
                    nCurrentSesson = oldSessions[1];
            } else {
                nCurrentSesson = getCurrentSession();
            }
            
            newSessions = new Session [oldSessions.length - 1];
            System.arraycopy (oldSessions, 0, newSessions, 0, i);
            if ((oldSessions.length - i) > 1)
                System.arraycopy (
                    oldSessions, i + 1, newSessions, i, oldSessions.length - i - 1
                );
            sessions.remove(i);
            
            session.removePropertyChangeListener (sessionListener);
            // The current engine is set in setCurrentSession().
        }
        setCurrentSession (nCurrentSesson);
        fireSessionRemoved (session, oldSessions, newSessions);
    }
    
    void addEngine (DebuggerEngine engine) {
        DebuggerEngine[] old;
        DebuggerEngine[] ne;
        synchronized (engines) {
            if (engines.contains (engine)) return;
            old = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [0]);
            engines.add (engine);
            ne = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [0]);
        }
        fireEngineAdded (engine, old, ne);
    }
    
    void removeEngine (DebuggerEngine engine) {
        DebuggerEngine[] old;
        DebuggerEngine[] ne;
        synchronized (engines) {
            if (!engines.contains (engine)) return;
            old = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [0]);
            engines.remove (engine);
            ne = (DebuggerEngine[]) engines.toArray (new DebuggerEngine [0]);
        }
        fireEngineRemoved (engine, old, ne);
    }
    

    
    // innerclasses ............................................................

    /**
     * Listens on all engines and sessions for: 
     * current thread changes 
     * start / finish of engines
     * last action
     * current engine
     */  
    private class SessionListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getSource () instanceof Session) {
                if ( (!e.getPropertyName ().equals
                      (Session.PROP_CURRENT_LANGUAGE)) &&
                     (!e.getPropertyName ().equals
                      (Session.PROP_SUPPORTED_LANGUAGES))
                ) return;
                // update the current engine
                DebuggerEngine oldEngine;
                DebuggerEngine newEngine;
                synchronized (sessions) {
                    oldEngine = currentEngine;
                    newEngine = null;
                    if (getCurrentSession () != null)
                        newEngine = getCurrentSession ().getCurrentEngine ();
                    currentEngine = newEngine;
                }
                if (newEngine != oldEngine) {
                    firePropertyChange (PROP_CURRENT_ENGINE, oldEngine, newEngine);
                }
                Session s = (Session) e.getSource ();
                if (s.getSupportedLanguages ().length == 0)
                    removeSession (s);
            }
        }
    }
}

