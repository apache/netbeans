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

package org.netbeans.api.debugger;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Bridge between sessions.
 * Use this for mixed languages debugging. Any debug session can suggest to change
 * the debugging session for a debug action. A registered implementation of
 * {@link SessionChanger} can decide to change the session in order to perform
 * the given action.
 * <p/>
 * In the current implementation, step into action of JPDA debugger is suggested
 * for a session change only. The support can be extended according to the future
 * requirements.
 * 
 * @author Martin Entlicher
 * @since 1.48
 */
public final class SessionBridge {
    
    private static SessionBridge instance;
    
    private final Map<String, Set<SessionChanger>> sessionChangers = new HashMap<String, Set<SessionChanger>>();
    private final List<SessionChanger> lookupSessionChangers;
    
    private SessionBridge() {
        Lookup lookup = new Lookup.MetaInf(null);
        final List<? extends SessionChanger> scList = lookup.lookup(null, SessionChanger.class);
        ((Customizer) scList).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                for (SessionChanger sc : lookupSessionChangers) {
                    removeSessionChangerListener(sc);
                }
                lookupSessionChangers.clear();
                for (SessionChanger sc : scList) {
                    lookupSessionChangers.add(sc);
                    addSessionChangerListener(sc);
                }
            }
        });
        lookupSessionChangers = new ArrayList<SessionChanger>();
        for (SessionChanger sc : scList) {
            lookupSessionChangers.add(sc);
            addSessionChangerListener(sc);
        }
    }
    
    /**
     * Get the default instance of SessionBridge.
     * @return the default instance
     */
    public static synchronized SessionBridge getDefault() {
        if (instance == null) {
            instance = new SessionBridge();
        }
        return instance;
    }
    
    /**
     * Suggest a session change to perform a particular action.
     * @param origin The original session suggesting the session change
     * @param action An action - a constant from ActionsManager.Action_*
     * @param properties Properties describing the current state of the current session before the given action.
     *                   The actual properties are specific for the particular session type.
     * @return <code>true</code> when the session is changed and another session
     *         decided to perform the given action.<br/>
     *         <code>false</code> when no other session would like to perform this action.
     */
    public boolean suggestChange(Session origin, String action, Map<Object, Object> properties) {
        Set<SessionChanger> scs;
        synchronized (sessionChangers) {
            scs = sessionChangers.get(action);
        }
        if (scs != null) {
            for (SessionChanger sc : scs) {
                Session newSession = sc.changeSuggested(origin, action, properties);
                if (newSession != null) {
                    if (DebuggerManager.getDebuggerManager().getCurrentSession() == origin) {
                        DebuggerManager.getDebuggerManager().setCurrentSession(newSession);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Test whether there is some session changer registered for the given action.
     * @param action An action - a constant from ActionsManager.Action_*
     * @return <code>true</code> when there is some session changer registered
     *         for this action, <code>false</code> otherwise.
     */
    public boolean isChangerFor(String action) {
        synchronized (sessionChangers) {
            Set<SessionChanger> scs = sessionChangers.get(action);
            return scs != null;
        }
    }
    
    private void addSessionChangerListener(SessionChanger sc) {
        Set<String> actions = sc.getActions();
        synchronized (sessionChangers) {
            for (String action : actions) {
                Set<SessionChanger> scs = sessionChangers.get(action);
                if (scs == null) {
                    sessionChangers.put(action, Collections.singleton(sc));
                } else {
                    if (scs.size() == 1) {
                        SessionChanger old = scs.iterator().next();
                        scs = new CopyOnWriteArraySet<SessionChanger>();
                        scs.add(old);
                    }
                    scs.add(sc);
                }
            }
        }
    }
    
    private void removeSessionChangerListener(SessionChanger sc) {
        Set<String> actions = sc.getActions();
        synchronized (sessionChangers) {
            for (String action : actions) {
                Set<SessionChanger> scs = sessionChangers.get(action);
                if (scs == null) {
                    continue;
                }
                if (scs.size() == 1) {
                    SessionChanger old = scs.iterator().next();
                    if (sc.equals(old)) {
                        sessionChangers.remove(action);
                    }
                } else {
                    scs.remove(sc);
                }
            }
        }
    }
    
    /**
     * Implement this interface to handle a debug session change.
     * Register the implementation via {@link DebuggerServiceRegistration} annotation.
     */
    public static interface SessionChanger {
        
        /**
         * Provide the set of actions that are handled by this implementation.
         * @return A set of constants from ActionsManager.Action_*
         */
        Set<String> getActions();
        
        /**
         * Called when a session suggests a session change for an action.
         * @param origin The session suggesting the session change
         * @param action The action, a constant from ActionsManager.Action_*
         * @param properties Session-specific properties describing the state
         *        right before the given action. These are used by a new session
         *        to complete the given action.
         * @return A new session, or <code>null<code> when this handler decides
         *         not to change the debug session for this action.
         */
        Session changeSuggested(Session origin, String action, Map<Object, Object> properties);
    }
    
}
