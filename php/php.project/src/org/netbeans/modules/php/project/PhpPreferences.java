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

package org.netbeans.modules.php.project;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * @author Radek Matous
 */
public final class PhpPreferences {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing private preferences which are not imported
    private static final String PRIVATE_PREFERENCES_PATH = "private"; // NOI18N

    private PhpPreferences() {
    }

    /**
     * @param importEnabled true means that preferences in this preferences node are
     * expected to be imported through import/export dialog in Options Dialog and also
     * by upgrader when first started new NB version
     * @return instance of Preferences node
     */
    public static Preferences getPreferences(boolean importEnabled) {
        Preferences forModule = NbPreferences.forModule(PhpPreferences.class);
        return (importEnabled) ? forModule : forModule.node(PRIVATE_PREFERENCES_PATH);
    }
}
