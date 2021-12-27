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

package org.netbeans.modules.junit.api;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.testrunner.CommonSettings;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Options for JUnit module, control behavior of test creation and execution.
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
public class JUnitSettings extends CommonSettings {
    private static final JUnitSettings INSTANCE = new JUnitSettings();
    
    /** */
    static final String JUNIT3_GENERATOR = JUnitVersion.JUNIT3.name().toLowerCase();
    /** */
    static final String JUNIT4_GENERATOR = JUnitVersion.JUNIT4.name().toLowerCase();
    /** */
    static final String JUNIT5_GENERATOR = JUnitVersion.JUNIT5.name().toLowerCase();
    /** */
    static final String JUNIT_GENERATOR_ASK_USER = "ask";               //NOI18N
    /** */
    static final String DEFAULT_GENERATOR = JUNIT_GENERATOR_ASK_USER;

    private  static Preferences getPreferences() {
        return NbPreferences.forModule(JUnitSettings.class);
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    public static JUnitSettings getDefault () {
        return INSTANCE;
    }
    
    public String getGenerator() {
        return getPreferences().get(PROP_GENERATOR, DEFAULT_GENERATOR);
    }
    
}
