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
package org.netbeans.modules.php.blade.editor.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import static org.netbeans.modules.php.blade.editor.preferences.GeneralPreferencesUtils.ENABLE_AUTO_TAG_COMPLETION;

/**
 *
 * @author bogdan
 */
public class ModulePreferences {
    public static Preferences getPreferences(){
        return MimeLookup.getLookup(BladeLanguage.MIME_TYPE).lookup(Preferences.class);
    }
    
    public static void setPrefBoolean(String key, boolean value){
        getPreferences().putBoolean(key, value);
    }
    
    public static boolean isAutoTagCompletionEnabled(){
        return ModulePreferences.getPreferences().getBoolean(ENABLE_AUTO_TAG_COMPLETION, true);
    }
}
