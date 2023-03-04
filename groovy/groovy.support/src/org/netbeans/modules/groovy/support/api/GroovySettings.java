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

package org.netbeans.modules.groovy.support.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.netbeans.modules.groovy.support.options.SupportOptionsPanelController;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import static org.netbeans.modules.groovy.support.api.Bundle.*;

/**
 * Groovy settings
 *
 * @author Martin Adamek
 */
// FIXME separate classes ?
public final class GroovySettings extends AdvancedOption {

    public static final String GROOVY_OPTIONS_CATEGORY = "Advanced/org-netbeans-modules-groovy-support-api-GroovySettings"; // NOI18N
    public static final String GROOVY_DOC_PROPERTY  = "groovy.doc"; // NOI18N
    
    private static final String GROOVY_DOC  = "groovyDoc"; // NOI18N
    private static final String ACCESS_MODIFIERS = "honourAccessModifiers";
    
    /**
     * If true, IDE will pretend that access modifiers are not broken in Groovy.
     */
    private static final boolean DEFAULT_ACCESS_MODIFIERS = false;
    
    private static GroovySettings instance;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    

    private GroovySettings() {
    }

    public static synchronized GroovySettings getInstance() {
        if (instance == null) {
            instance = new GroovySettings();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public String getGroovyDoc() {
        synchronized (this) {
            return getPreferences().get(GROOVY_DOC, null); // NOI18N
        }
    }

    public void setGroovyDoc(String groovyDoc) {
        assert groovyDoc != null;

        String oldValue;
        synchronized (this) {
            oldValue = getGroovyDoc();
            getPreferences().put(GROOVY_DOC, groovyDoc);
        }
        propertyChangeSupport.firePropertyChange(GROOVY_DOC_PROPERTY, oldValue, groovyDoc);
    }
    
    public void setHonourAccessModifiers(boolean mods) {
        boolean old;
        synchronized (this) {
            old = isHonourAccessModifiers();
            getPreferences().putBoolean(ACCESS_MODIFIERS, mods);
        }
        propertyChangeSupport.firePropertyChange(ACCESS_MODIFIERS, old, mods);
    }
    
    /**
     * Should code analysis honour access modifiers ?
     * @return true, if access modifiers should be respected.
     */
    public boolean isHonourAccessModifiers() {
        return getPreferences().getBoolean(ACCESS_MODIFIERS, DEFAULT_ACCESS_MODIFIERS);
    }

    @Override
    @NbBundle.Messages("AdvancedOption_DisplayName_Support=Groovy")
    public String getDisplayName() {
        return AdvancedOption_DisplayName_Support();
    }

    @Override
    @NbBundle.Messages("AdvancedOption_Tooltip_Support=Groovy configuration")
    public String getTooltip() {
        return AdvancedOption_Tooltip_Support();
    }

    @Override
    public OptionsPanelController create() {
        return new SupportOptionsPanelController();
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(GroovySettings.class);
    }

}
