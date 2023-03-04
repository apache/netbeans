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

package org.netbeans.modules.welcome.content;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
class HttpProxySettings {

    private static HttpProxySettings theInstance;
    private static Preferences proxySettingsNode;

    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );

    public static final String PROXY_SETTINGS = "ProxySettings"; // NOI18N
    
    /** Creates a new instance of HttpProxySettings */
    private HttpProxySettings() {
        initProxyMethodsMaybe();
    }

    public static HttpProxySettings getDefault() {
        if( null == theInstance ) {
            theInstance = new HttpProxySettings();
        }
        return theInstance;
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }

    public void showConfigurationDialog() {
        OptionsDisplayer.getDefault().open( "General" );//NOI18N
    }

    private static synchronized void initProxyMethodsMaybe() {
        proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
        assert proxySettingsNode != null;
        proxySettingsNode.addPreferenceChangeListener (new PreferenceChangeListener (){
            public void preferenceChange (PreferenceChangeEvent evt) {
                if (evt.getKey ().startsWith ("proxy") || evt.getKey ().startsWith ("useProxy")) {
                    getDefault ().propertySupport.firePropertyChange (PROXY_SETTINGS, null, getDefault());
                }
            }
        });
    }
}
