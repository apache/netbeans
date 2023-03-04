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
package org.netbeans.modules.cloud.oracle.adm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Pisl
 */
class DefaultKnowledgeBaseStorage implements PreferenceChangeListener {
    private static final String KEY_KNOWLEDGEBASEID = "default_knowledge_base";
    private static final String KEY_COMPARTMENTID = "default_compartment";
    
    private static DefaultKnowledgeBaseStorage INSTANCE;
    
    private final PropertyChangeSupport pcs;
    private String knowledgeBaseId;
    private String compartmentId;

    private DefaultKnowledgeBaseStorage() {
        this.pcs = new PropertyChangeSupport(this);
        NbPreferences.root().addPreferenceChangeListener(this);
        knowledgeBaseId = NbPreferences.root().get(KEY_KNOWLEDGEBASEID, null);
        compartmentId = NbPreferences.root().get(KEY_COMPARTMENTID, null);
    }
    
    public static DefaultKnowledgeBaseStorage getInstance() {
        if (INSTANCE == null) {
            synchronized(DefaultKnowledgeBaseStorage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DefaultKnowledgeBaseStorage();
                }
            }
        }
        return INSTANCE;
    }
    
    public void setAsDefault(String compartmentId, String knowledgeBaseId) {
        NbPreferences.root().put(KEY_KNOWLEDGEBASEID, knowledgeBaseId);
        NbPreferences.root().put(KEY_COMPARTMENTID, compartmentId);
    }
    
    public String getDefaultKnowledgeBaseId() {
        return NbPreferences.root().get(KEY_KNOWLEDGEBASEID, null);
    }
    
    public String getDefaultCompartmentId() {
        return NbPreferences.root().get(KEY_COMPARTMENTID, null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(KEY_KNOWLEDGEBASEID)) {
            pcs.firePropertyChange(KEY_KNOWLEDGEBASEID, knowledgeBaseId, evt.getNewValue());
            knowledgeBaseId = evt.getNewValue();
        }
        
        if (evt.getKey().equals(KEY_COMPARTMENTID)) {
            pcs.firePropertyChange(KEY_COMPARTMENTID, compartmentId, evt.getNewValue());
            compartmentId = evt.getNewValue();
        }
    }

    public void addChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removeChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
}
