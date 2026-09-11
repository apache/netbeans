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
package org.netbeans.modules.php.blade.editor.typinghooks;

import org.netbeans.modules.php.blade.editor.BladeTestBase;
import static org.netbeans.modules.php.blade.editor.preferences.GeneralPreferencesUtils.ENABLE_AUTO_TAG_COMPLETION;
import org.netbeans.modules.php.blade.editor.preferences.ModulePreferences;


public abstract class BladeTypinghooksTestBase extends BladeTestBase {
    private boolean autoTagCompletion = false;
    public BladeTypinghooksTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        autoTagCompletion = ModulePreferences.getPreferences().getBoolean(ENABLE_AUTO_TAG_COMPLETION, false);
        ModulePreferences.setPrefBoolean(ENABLE_AUTO_TAG_COMPLETION, true);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ModulePreferences.setPrefBoolean(ENABLE_AUTO_TAG_COMPLETION, autoTagCompletion);
    }

}
