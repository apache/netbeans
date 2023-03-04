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
package org.netbeans.modules.maven.options;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class MavenSettingsTest {
    @Before
    public void cleanUpSettings() throws BackingStoreException {
        Preferences p = NbPreferences.forModule(MavenSettings.class);
        p.clear();
    }
    
    @Test
    public void testReuseOutputTabsByDefault() {
        NbBundle.setBranding(null);
        final MavenSettings s = MavenSettings.getDefault();
        assertTrue("By default reuse tabs in NetBeans IDE", s.isReuseOutputTabs());
        s.setReuseOutputTabs(false);
        assertFalse("Now set to false", s.isReuseOutputTabs());
        s.setReuseOutputTabs(true);
        assertTrue("Now set to true", s.isReuseOutputTabs());
    }
    
    @Test
    public void testReuseOutputTabsWithBranding() {
        NbBundle.setBranding("test");
        final MavenSettings s = MavenSettings.getDefault();
        assertFalse("Allow branding to disable reuse of tabs", s.isReuseOutputTabs());
        s.setReuseOutputTabs(true);
        assertFalse("Now set to true, but we branded to 'never'", s.isReuseOutputTabs());
    }

    
}
