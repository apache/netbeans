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

package org.netbeans.modules.autoupdate.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Checks and caches code name base to module info mapping.
 *
 * @author Jirka Rechtacek
 */
final class ModuleCache
implements PropertyChangeListener, LookupListener {
    private static ModuleCache INSTANCE;

    private final Lookup.Result<ModuleInfo> result;
    private final ChangeSupport support;
    private Map<String,ModuleInfo> infos;

    private ModuleCache() {
        support = new ChangeSupport(this);
        result = Lookup.getDefault().lookupResult(ModuleInfo.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    public static synchronized ModuleCache getInstance () {
        if (INSTANCE == null) {
            INSTANCE = new ModuleCache();
        }
        return INSTANCE;
    }
    
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }
    
    public ModuleInfo find(String cnb) {
        return infos.get(cnb);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        for (ModuleInfo m : result.allInstances()) {
            m.removePropertyChangeListener(this);
            m.addPropertyChangeListener(this);
        }
        Map<String,ModuleInfo> tmp = new HashMap<String,ModuleInfo>();
        for (ModuleInfo mi : result.allInstances()) {
            tmp.put(mi.getCodeNameBase(), mi);
        }
        infos = tmp;
        if (ev != null) {
            fireChange();
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
            ModuleInfo mi = (ModuleInfo)evt.getSource();

            /*
            fireChange();
            if (mi.isEnabled()) {
                enabledCnbs.add(mi.getCodeNameBase());
            } else {
                enabledCnbs.remove(mi.getCodeNameBase());
            }
             */
        }
    }

    private void fireChange() {
        support.fireChange();
    }

}
