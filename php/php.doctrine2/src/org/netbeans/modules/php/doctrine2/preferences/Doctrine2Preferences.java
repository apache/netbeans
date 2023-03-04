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
package org.netbeans.modules.php.doctrine2.preferences;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.doctrine2.Doctrine2PhpFrameworkProvider;

/**
 * Doctrine2 preferences (for each PHP module).
 */
public final class Doctrine2Preferences {

    private static final String ENABLED = "enabled"; // NOI18N


    private Doctrine2Preferences() {
    }

    public static boolean isEnabled(PhpModule module) {
        return getPreferences(module).getBoolean(ENABLED, false);
    }

    public static void setEnabled(PhpModule module, boolean enabled) {
        getPreferences(module).putBoolean(ENABLED, enabled);
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(Doctrine2PhpFrameworkProvider.class, true);
    }

}
