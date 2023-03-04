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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static final String PROP_OPEN_CONNECTION_TIMEOUT = "plugin.manager.connection.timeout"; // NOI18N
    public static final int DEFAULT_OPEN_CONNECTION_TIMEOUT = 30000;
    private static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N
    
    private static final Logger err = Logger.getLogger (AutoupdateSettings.class.getName ());
    
    private AutoupdateSettings () {
    }

    public static void setLastCheck (Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (PROP_LAST_CHECK);
        }
    }
    
    public static void setLastCheck (String updateProviderName, Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (updateProviderName+"_"+PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (updateProviderName+"_"+PROP_LAST_CHECK);//NOI18N
        }
    }
    
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    public static int getOpenConnectionTimeout () {
        return getPreferences ().getInt (PROP_OPEN_CONNECTION_TIMEOUT, DEFAULT_OPEN_CONNECTION_TIMEOUT);
    }
}
