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

package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;

/**
 *
 * @author rmatous
 */
public class PreferencesProviderImplTest extends NbPreferencesTest.TestBasicSetup {    
    public PreferencesProviderImplTest(String testName) {
        super(testName);
    }

    /**
     * Test of preferencesForModule method, of class org.netbeans.core.startup.preferences.PreferencesProviderImpl.
     */
    public void testPreferencesForModule() {
        PreferencesProviderImpl instance = new PreferencesProviderImpl();        
        Preferences result = instance.preferencesForModule(getClass());
        assertNotNull(result);
        assertTrue(NbPreferences.class.isAssignableFrom(result.getClass()));
        assertEquals(result.absolutePath(),"/"+getClass().getPackage().getName().replace('.','/'));
    }

    /**
     * Test of preferencesRoot method, of class org.netbeans.core.startup.preferences.PreferencesProviderImpl.
     */
    public void testPreferencesRoot() {        
        PreferencesProviderImpl instance = new PreferencesProviderImpl();        
        Preferences result = instance.preferencesRoot();
        assertNotNull(result);
        assertTrue(NbPreferences.class.isAssignableFrom(result.getClass()));
        assertEquals(result.absolutePath(),"/");
    }
    
}
