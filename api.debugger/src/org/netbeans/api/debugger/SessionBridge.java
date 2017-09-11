/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
