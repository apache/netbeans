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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.netbeans.modules.hudson.spi.HudsonManagerAgent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Implementation of the HudsonManager
 *
 * @author Michal Mocnak
 */
public class HudsonManagerImpl {

    private static final RequestProcessor RP = new RequestProcessor(HudsonManagerImpl.class);
    
    /** The only instance of the hudson manager implementation in the system */
    private static HudsonManagerImpl defaultInstance;
    
    private Map<String, HudsonInstanceImpl> instances;
    private final List<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    private final Collection<? extends HudsonManagerAgent> agents;
    
    private HudsonManagerImpl() {
        this.agents = Lookup.getDefault().lookupAll(HudsonManagerAgent.class);
    }
    
    /**
     * Singleton accessor
     *
     * @return instance of hudson manager implementation
     */
    public static synchronized HudsonManagerImpl getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new HudsonManagerImpl();
        }
        return defaultInstance;
    }
    
    public HudsonInstanceImpl addInstance(final HudsonInstanceImpl instance) {
        synchronized (this) {
            if (null == instance
                    || null != getInstancesMap().get(instance.getUrl())) {
                return null;
            }
            if (null != getInstancesMap().put(instance.getUrl(), instance)) {
                return null;
            }
        }
        for (HudsonManagerAgent agent: agents) {
            agent.instanceAdded(instance);
        }
        fireChangeListeners();
        return instance;
    }
    
    public HudsonInstanceImpl removeInstance(HudsonInstanceImpl instance) {
        synchronized (this) {
            if (null == instance || null == getInstancesMap().get(
                    instance.getUrl())) {
                return null;
            }
            if (null == getInstancesMap().remove(instance.getUrl())) {
                return null;
            }
        }
        // Stop autosynchronization if it's running
        instance.terminate();
        
        for (HudsonManagerAgent agent : agents) {
            agent.instanceRemoved(instance);
        }
        // Fire changes into all listeners
        fireChangeListeners();
        
        // Remove instance file
        if (instance.isPersisted()) {
            removeInstanceDefinition(instance);
        }
        
        return instance;
    }
    
    public synchronized HudsonInstanceImpl getInstance(String url) {
        return getInstancesMap().get(url);
    }
    
    public synchronized Collection<HudsonInstanceImpl> getInstances() {
        return Arrays.asList(getInstancesMap().values().toArray(new HudsonInstanceImpl[] {}));
    }
    
    public HudsonInstance getInstanceByName(String name) {
        for (HudsonInstance h : getInstances()) {
            if (h.getName().equals(name))
                return h;
        }
        
        return null;
    }
    
    public void addHudsonChangeListener(HudsonChangeListener l) {
        listeners.add(l);
    }
    
    public void removeHudsonChangeListener(HudsonChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChangeListeners() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.stateChanged();
            l.contentChanged();
        }
    }
    
    public void terminate() {
        // Clear default instance
        defaultInstance = null;
        for (HudsonManagerAgent agent: agents) {
            agent.terminate();
        }
        // Terminate instances
        for (HudsonInstance instance : getInstances())
            ((HudsonInstanceImpl) instance).terminate();
    }

    static Preferences instancePrefs() {
        return NbPreferences.forModule(HudsonManagerImpl.class).node("instances"); // NOI18N
    }
    
    public static String simplifyServerLocation(String name, boolean forKey) {
        // http://deadlock.netbeans.org/hudson/ => deadlock.netbeans.org_hudson
        String display = name.replaceFirst("https?://", "").replaceFirst("/$", "");
        return forKey ? display.replaceAll("[/:]", "_") : display; // NOI18N
    }
    
    private void removeInstanceDefinition(HudsonInstanceImpl instance) {
        try {
            instance.prefs().removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Map<String, HudsonInstanceImpl> getInstancesMap() {
        if (null == instances) {
            instances = new HashMap<String, HudsonInstanceImpl>();
            
            // initialization
            init();
        }
        
        return instances;
    }

    private void init() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        for (String kid : instancePrefs().childrenNames()) {
                            Preferences node = instancePrefs().node(kid);
                            Map<String, String> m = new HashMap<String, String>();
                            for (String k : node.keys()) {
                                m.put(k, node.get(k, null));
                            }
                            if (!m.containsKey(INSTANCE_NAME) || !m.containsKey(INSTANCE_URL) || !m.containsKey(INSTANCE_SYNC)) {
                                continue;
                            }
                            if (FALSE.equals(m.get(INSTANCE_PERSISTED))) {
                                continue;
                            }
                            HudsonInstanceImpl.createHudsonInstance(new HudsonInstanceProperties(m), false);
                        }
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    // Fire changes
                    fireChangeListeners();
                }
            }
        });
        for (HudsonManagerAgent agent: agents) {
            agent.start();
        }
    }
}
