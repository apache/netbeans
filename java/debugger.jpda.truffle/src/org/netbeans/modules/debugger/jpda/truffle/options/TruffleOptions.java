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

package org.netbeans.modules.debugger.jpda.truffle.options;

import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Properties;
import org.openide.util.WeakListeners;

public class TruffleOptions {
    
    private static final String PROPERTIES_TRUFFLE = "debugger.options.JPDA.truffle"; // NOI18N
    public static final String PROPERTY_LANG_DEV_MODE = "LanguageDeveloperMode";  // NOI18N

    private static final Properties PROPERTIES = Properties.getDefault().getProperties(PROPERTIES_TRUFFLE);
    
    private TruffleOptions() {}
    
    private static boolean isLanguageDeveloperModeDefault() {
        return false;
    }
    
    public static boolean isLanguageDeveloperMode() {
        return PROPERTIES.getBoolean(PROPERTY_LANG_DEV_MODE, isLanguageDeveloperModeDefault());
    }
    
    public static void setLanguageDeveloperMode(boolean ldm) {
        PROPERTIES.setBoolean(PROPERTY_LANG_DEV_MODE, ldm);
    }

    public static void onLanguageDeveloperModeChange(PropertyChangeListener chl) {
        PROPERTIES.addPropertyChangeListener(WeakListeners.propertyChange(chl, PROPERTIES));
    }
}
