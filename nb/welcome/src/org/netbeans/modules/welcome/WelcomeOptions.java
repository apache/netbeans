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

package org.netbeans.modules.welcome;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions {

    private static WelcomeOptions theInstance;
    
    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";
    private static final String PROP_LAST_ACTIVE_TAB = "lastActiveTab";
    private static final String PROP_START_COUNTER = "startCounter";
    
    private PropertyChangeSupport propSupport;
    
    /** Creates a new instance of WelcomeOptions */
    private WelcomeOptions() {
    }

    private Preferences prefs() {
        return NbPreferences.forModule(WelcomeOptions.class);
    }

    public static synchronized WelcomeOptions getDefault() {
        if( null == theInstance ) {
            theInstance = new WelcomeOptions();
        }
        return theInstance;
    }
 
    public void setShowOnStartup( boolean show ) {
        boolean oldVal = isShowOnStartup();
        if( oldVal == show ) {
            return;
        }
        prefs().putBoolean(PROP_SHOW_ON_STARTUP, show);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_SHOW_ON_STARTUP, oldVal, show );

        LogRecord rec = new LogRecord(Level.INFO, "USG_SHOW_START_PAGE"); //NOI18N
        rec.setParameters(new Object[] {show} );
        rec.setLoggerName(Constants.USAGE_LOGGER.getName());
        rec.setResourceBundle(NbBundle.getBundle(BundleSupport.BUNDLE_NAME));
        rec.setResourceBundleName(BundleSupport.BUNDLE_NAME);

        Constants.USAGE_LOGGER.log(rec);

    }

    public boolean isShowOnStartup() {
        return prefs().getBoolean(PROP_SHOW_ON_STARTUP, false);
    }

    public void setLastActiveTab( int tabIndex ) {
        int oldVal = getLastActiveTab();
        prefs().putInt(PROP_LAST_ACTIVE_TAB, tabIndex);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_LAST_ACTIVE_TAB, oldVal, tabIndex );
    }

    public int getLastActiveTab() {
        return prefs().getInt(PROP_LAST_ACTIVE_TAB, -1);
    }
    
    public boolean isSecondStart() {
        return prefs().getInt(PROP_START_COUNTER, -1) == 2;
    }
    
    public void incrementStartCounter() {
        int count = prefs().getInt(PROP_START_COUNTER, 0) + 1;
        if( count > 3 )
            return; //we're just interested in the first and second start so don't bother any more then
        prefs().putInt( PROP_START_COUNTER, count );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            propSupport = new PropertyChangeSupport( this );
        propSupport.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            return;
        propSupport.removePropertyChangeListener( l );
    }
}
