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

package org.netbeans.junit;

import java.util.prefs.Preferences;
import junit.framework.*;

/**
 *
 * @author Radek Matous
 */
public class NbTestCasePreferencesTest extends NbTestCase {
    public NbTestCasePreferencesTest(String testName) {
        super(testName);
    }
    
    public void testNotPersistentPreferences() throws Exception {
        Preferences pref = Preferences.userNodeForPackage(getClass());
        assertNotNull(pref);
        pref.put(getName(), "value");
        assertEquals("value", pref.get(getName(), null));
        pref.sync();
        assertEquals(null, pref.get(getName(), null));
    }
    
    public void testFirst() {
        Preferences.userNodeForPackage(getClass()).put("testFirst", "value");
    }
    
    public void testSecond() {
        assertEquals("somedefaultvalue", Preferences.userNodeForPackage(getClass()).get("testFirst", "somedefaultvalue"));
    }
}
