/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.awt;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.awt.ContextManager.LookupRef;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;


/** Listener on a global context.
 */
class GlobalManager extends Object implements LookupListener {
    private static final Logger LOG = GeneralAction.LOG;
    
    private static final Map<LookupRef, Reference<GlobalManager>> CACHE = new HashMap<LookupRef, Reference<GlobalManager>>();
    private static final Map<LookupRef, Reference<GlobalManager>> SURVIVE = new HashMap<LookupRef, Reference<GlobalManager>>();
    
    private Lookup.Result<ActionMap> result;
    private Reference<ActionMap> actionMap = new WeakReference<ActionMap>(null);
    private Map<Object,Set<GeneralAction.BaseDelAction>> listeners;
    private PropertyChangeListener changeL;
    
    private GlobalManager(Lookup lookup) {
        this.listeners = new HashMap<Object,Set<GeneralAction.BaseDelAction>>();
        this.result = lookup.lookupResult(ActionMap.class);
        result.addLookupListener(this);
        resultChanged(null);
    }
    
    public static GlobalManager findManager(Lookup context, boolean survive) {
        synchronized (CACHE) {
            Map<LookupRef, Reference<GlobalManager>> map = survive ? SURVIVE : CACHE;
            LookupRef lr = new LookupRef(context);
            Reference<GlobalManager> ref = map.get(lr);
            GlobalManager g = ref == null ? null : ref.get();
            if (g == null) {
                g = survive ? new SurviveManager(context) : new GlobalManager(context);
                ref = new GMReference(g, lr, survive);
                map.put(lr, ref);
            }
            return g;
        }
    }
    
    static void clearCache(LookupRef lr, GMReference ref, boolean survive) {
        synchronized (CACHE) {
            Map<LookupRef, Reference<GlobalManager>> map = survive ? SURVIVE : CACHE;
            if (map.get(lr) == ref) {
                map.remove(lr);
            }
        }
    }
    
    public Action findGlobalAction(Object key) {
        if (key == null) {
            return null;
        }
        ActionMap map = actionMap.get();
        Action a = (map == null) ? null : map.get(key);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Action for key: {0} is: {1}", new Object[]{key, a}); // NOI18N
        }
        
        return a;
    }
    
    public final void registerListener(Object key, GeneralAction.BaseDelAction a) {
        if (key == null) {
            return;
        }
        synchronized (CACHE) {
            Set<GeneralAction.BaseDelAction> existing = listeners.get(key);
            if (existing == null) {
                existing = new WeakSet<GeneralAction.BaseDelAction>();
                listeners.put(key, existing);
            }
            existing.add(a);
            a.updateState(new ActionMap(), actionMap.get(), false);
        }
    }

    public final void unregisterListener(Object key, GeneralAction.BaseDelAction a) {
        if (key == null) {
            return;
        }
        synchronized (CACHE) {
            Set<GeneralAction.BaseDelAction> existing = listeners.get(key);
            if (existing != null) {
                existing.remove(a);
                if (existing.isEmpty()) {
                    listeners.remove(key);
                }
            }
        }
    }
    
    /** Change all that do not survive ActionMap change */
    @Override
    public final void resultChanged(org.openide.util.LookupEvent ev) {
        Collection<? extends Lookup.Item<? extends ActionMap>> all = result.allItems();
        ActionMap a = all.isEmpty() ? null : all.iterator().next().getInstance();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "changed map : {0}", a); // NOI18N
            LOG.log(Level.FINE, "previous map: {0}", actionMap.get()); // NOI18N
        }
        
        final ActionMap prev = actionMap.get();
        if (a == prev) {
            return;
        }
        
        final ActionMap newMap = newMap(prev, a);
        
        actionMap = new WeakReference<ActionMap>(newMap);
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("clearActionPerformers"); // NOI18N
        }
        
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                notifyListeners(prev, newMap);
            }
        });
    }

    final void notifyListeners(ActionMap prev, ActionMap now) {
        if (prev == null) prev = new ActionMap();
        if (now == null) now = new ActionMap();
        
        HashSet<Object> keys = new HashSet<Object>();
        Object[] allPrev = prev.allKeys();
        Object[] allNow = now.allKeys();
        if (allPrev != null) {
            keys.addAll(Arrays.asList(allPrev));
        }
        if (allNow != null) {
            keys.addAll(Arrays.asList(allNow));
        }

        for (Object k : keys) {
            Set<GeneralAction.BaseDelAction> actions = listeners.get(k);
            if (actions == null) {
                continue;
            }
            for (GeneralAction.BaseDelAction del : actions) {
                if (del != null) {
                    del.updateState(prev, now, true);
                }
            }
        }
    }

    /** Does not survive focus change */
    public boolean isSurvive() {
        return false;
    }

    /** Method that can be overridden to provide "different" behaviour for
     * keeping previous maps, like in case of "surviveFocusChange"
     */
    protected ActionMap newMap(ActionMap prev, ActionMap newMap) {
        return newMap;
    }

    private static final class GMReference extends WeakReference<GlobalManager> 
    implements Runnable {
        private LookupRef context;
        private boolean survive;
        
        public GMReference(GlobalManager m, LookupRef context, boolean survive) {
            super(m, Utilities.activeReferenceQueue());
            this.context = context;
            this.survive = survive;
        }
        
        @Override
        public void run() {
            clearCache(context, this, survive);
        }
    } // end of GMReference

    /** Manager with special behaviour.
     */
    private static final class SurviveManager extends GlobalManager {
        private SurviveManager(Lookup context) {
            super(context);
        }
        
        @Override
        public boolean isSurvive() {
            return true;
        }
        
        
        @Override
        protected ActionMap newMap(ActionMap prev, ActionMap newMap) {
            ArrayList<Object> old = new ArrayList<Object>();
            if (prev != null) {
                Object[] all = prev.allKeys();
                if (all != null) {
                    old.addAll(Arrays.asList(all));
                    if (newMap != null) {
                        Object[] toRem = newMap.allKeys();
                        if (toRem != null) {
                            old.removeAll(Arrays.asList(toRem));
                        }
                    }
                }
            }

            ActionMap merged = new ActionMap();
            if (newMap != null) {
                Object[] allK = newMap.allKeys();
                if (allK != null) {
                    for (int i = 0; i < allK.length; i++) {
                        Object o = allK[i];
                        merged.put(o, newMap.get(o));
                    }
                }
            }
            
            for (Object o : old) {
                merged.put(o, prev.get(o));
            }
            
            return merged;
        }
    }
}

