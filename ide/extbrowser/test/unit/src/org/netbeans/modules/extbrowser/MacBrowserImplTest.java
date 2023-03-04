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
package org.netbeans.modules.extbrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author petrjiricka
 */
public class MacBrowserImplTest {
    
    public MacBrowserImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of getPrivateBrowserFamilyIdFromDefaultApps method, of class MacBrowserImpl.
     */
    @Test
    public void testGetPrivateBrowserFamilyIdFromDefaultApps() throws IOException {
        System.out.println("getPrivateBrowserFamilyIdFromDefaultApps");
        assertEquals(PrivateBrowserFamilyId.CHROME, MacBrowserImpl.getPrivateBrowserFamilyIdFromDefaultApps(
                getDefaultAppsFile("org/netbeans/modules/extbrowser/data/mac_defaults_chrome.txt")));
        assertEquals(PrivateBrowserFamilyId.FIREFOX, MacBrowserImpl.getPrivateBrowserFamilyIdFromDefaultApps(
                getDefaultAppsFile("org/netbeans/modules/extbrowser/data/mac_defaults_firefox.txt")));
        assertEquals(PrivateBrowserFamilyId.SAFARI, MacBrowserImpl.getPrivateBrowserFamilyIdFromDefaultApps(
                getDefaultAppsFile("org/netbeans/modules/extbrowser/data/mac_defaults_safari.txt")));
        assertEquals(PrivateBrowserFamilyId.OPERA, MacBrowserImpl.getPrivateBrowserFamilyIdFromDefaultApps(
                getDefaultAppsFile("org/netbeans/modules/extbrowser/data/mac_defaults_opera.txt")));
    }
    
    private String getDefaultAppsFile(String resource) throws IOException {
        BufferedReader reader = null;
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine())!= null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                builder.append(line);
            }
            return builder.toString();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
   }

    
}
