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
package org.netbeans.modules.project.ui.problems;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
public class BrokenReferencesSettings {

    private static final String PROP_SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    public static boolean isShowAgainBrokenRefAlert() {
        return prefs().getBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }

    public static void setShowAgainBrokenRefAlert(boolean again) {
        prefs().putBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }


    private static Preferences prefs() {
        return NbPreferences.forModule(BrokenReferencesSettings.class);
    }

}
