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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.versioning.ui.history;

import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * Manages History Settings
 * 
 * @author Tomas Stupka
 */
public class HistorySettings {
    
    private static final HistorySettings INSTANCE = new HistorySettings();
    
    private static final String PROP_TTL = "timeToLive";                                        // NOI18N  
    public static final String PROP_INCREMENTS = "history.increments";                          // NOI18N  
    private static final String PROP_CLEANUP_LABELED = "noLabelCleanUp";                        // NOI18N  
    private static final String PROP_KEEP_FOREVER = "keepForever";                              // NOI18N  
    public static final String PROP_LOAD_ALL = "history.loadAll";                               // NOI18N  
    public static final String PROP_ALL_MODE = "history.AllMode";                               // NOI18N  
    public static final String PROP_VCS_MODE = "history.VCSMode";                               // NOI18N  
    public static final String PROP_LH_MODE = "history.LHMode";                               // NOI18N  

    /** Creates a new instance of HistorySettings */
    private HistorySettings() {
    }
    
    public static HistorySettings getInstance() {        
        migrate();
        return INSTANCE;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(HistorySettings.class);
    }

    public void setTTL(int ttl) {
        getPreferences().putInt(PROP_TTL, ttl);
    }
    
    public void setIncrements(int ttl) {
        getPreferences().putInt(PROP_INCREMENTS, ttl);
    }
    
    public int getTTL() {
        return getPreferences().getInt(PROP_TTL, 7);
    }    

    public int getIncrements() {
        int i = getPreferences().getInt(PROP_INCREMENTS, 30);
        return i < 1 ? i = 7 : i;
    }    

    public void setCleanUpLabeled(boolean selected) {
        getPreferences().putBoolean(PROP_CLEANUP_LABELED, selected);
    }
    
    public boolean getCleanUpLabeled() {
        return getPreferences().getBoolean(PROP_CLEANUP_LABELED, true);
    }
    
    public long getTTLMillis() {
        return ((long) getTTL()) * 24 * 60 * 60 * 1000;
    }   

    public boolean getKeepForever() {
        return getPreferences().getBoolean(PROP_KEEP_FOREVER, false);
    }

    public void setKeepForever(boolean forever) {
        getPreferences().putBoolean(PROP_KEEP_FOREVER, forever);
    }
    
    public boolean getLoadAll() {
        return getPreferences().getBoolean(PROP_LOAD_ALL, false);
}

    public void setLoadAll(boolean loadAll) {
        getPreferences().putBoolean(PROP_LOAD_ALL, loadAll);
    }

    public void addPreferenceListener(PreferenceChangeListener l) {
        getPreferences().addPreferenceChangeListener(l);
    }
    
    public void removePreferenceListener(PreferenceChangeListener l) {
        getPreferences().removePreferenceChangeListener(l);
    }

    void setAllMode(String name) {
        getPreferences().put(PROP_ALL_MODE, name);
    }
    
    void setVCSMode(String name) {
        getPreferences().put(PROP_VCS_MODE, name);
    }
    
    void setLHMode(String name) {
        getPreferences().put(PROP_LH_MODE, name);
    }
    
    String getAllMode(String def) {
        return getPreferences().get(PROP_ALL_MODE, def);
    }
    
    String getVCSMode(String def) {
        return getPreferences().get(PROP_VCS_MODE, def);
    }
    
    String getLHMode(String def) {
        return getPreferences().get(PROP_LH_MODE, def);
    }

    private static void migrate() { 
        // migrate pre 7.2 settings 
        String prevPath = "org/netbeans/modules/localhistory"; // NOI18N
        try {
            if(!NbPreferences.root().nodeExists(prevPath)) {
                return;
            }
            Preferences prev = NbPreferences.root().node(prevPath);
            Preferences cur =  NbPreferences.forModule(HistorySettings.class);
            String[] keys = prev.keys();
            for (String key : keys) {
                String value = prev.get(key, null);
                if(value != null && cur.get(key, null) == null) {
                    cur.put(key, value);
                }
            }
            prev.removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }    
}
