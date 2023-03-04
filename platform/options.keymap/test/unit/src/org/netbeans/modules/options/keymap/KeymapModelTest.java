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

package org.netbeans.modules.options.keymap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author David Strupl
 */
public class KeymapModelTest extends TestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", KeymapModelTest.GlobalLookup.class.getName ());
    }
    
    /**
     * 
     * @param testName 
     */
    
    /**
     * 
     * @param testName 
     */
    public KeymapModelTest(String testName) {
        super(testName);
    }
    
    /**
     * 
     * @throws java.lang.Exception 
     */
    protected void setUp() throws Exception {
    }

    /**
     * 
     */
    public void testGetActionCategories() {
        KeymapModel instance = new KeymapModel();
        Set<String> result = instance.getActionCategories();
        assertTrue("Category C1 should be there", result.contains("C1"));
    }

    /**
     * 
     */
    public void testGetActions() {
        String category = "C1";
        KeymapModel instance = new KeymapModel();
        Set expResult = null;
        Set<ShortcutAction> result = instance.getActions(category);
        assertEquals(result.size(), 1);
        Iterator<ShortcutAction> it = result.iterator();
        ShortcutAction sa = it.next();
        ShortcutAction sa1 = sa.getKeymapManagerInstance("One");
        assertNotNull(sa1);
        ShortcutAction sa2 = sa.getKeymapManagerInstance("Two");
        assertNotNull(sa2);
    }

    /**
     * 
     */
    public void testRefreshActions() {
        KeymapModel instance = new KeymapModel();
        instance.refreshActions();
        assertTrue(GlobalLookup.km1.refreshActionsCalled);
        assertTrue(GlobalLookup.km2.refreshActionsCalled);
    }

    /**
     * 
     */
    public void testGetCurrentProfile() {
        KeymapModel instance = new KeymapModel();
        String expResult = "test";
        String result = instance.getCurrentProfile();
        assertEquals(expResult, result);
    }

    /**
     * 
     */
    public void testSetCurrentProfile() {
        String profile = "test";
        KeymapModel instance = new KeymapModel();

        instance.setCurrentProfile(profile);
        assertTrue(GlobalLookup.km1.setProfileCalled);
        assertTrue(GlobalLookup.km2.setProfileCalled);
    }

    /**
     * 
     */
    public void testGetProfiles() {
        KeymapModel instance = new KeymapModel();
        List expResult = new ArrayList();
        expResult.add("test");
        List result = instance.getProfiles();
        assertEquals(expResult, result);
    }

    /**
     * 
     */
    public void testGetKeymap() {
        String profile = "test";
        KeymapModel instance = new KeymapModel();
        Map<ShortcutAction, Set<String>> result = instance.getKeymap(profile);
        Set<String> s = result.values().iterator().next();
        assertNotNull(s);
        assertTrue(s.contains("CS-A"));
    }

    /**
     * 
     */
    public void testGetKeymapDefaults() {
        String profile = "test";
        KeymapModel instance = new KeymapModel();
        Map expResult = instance.getKeymap(profile);
        Map result = instance.getKeymapDefaults(profile);
        assertEquals(expResult, result);
    }

    /**
     * 
     */
    public void testDeleteProfile() {
        String profile = "test";
        KeymapModel instance = new KeymapModel();
        instance.deleteProfile(profile);
        assertTrue(GlobalLookup.km1.deleteProfileCalled);
        assertTrue(GlobalLookup.km2.deleteProfileCalled);
    }

    /**
     * 
     */
    public void testChangeKeymap() {
        String profile = "test";
        Map<ShortcutAction, Set<String>> actionToShortcuts = new HashMap<ShortcutAction, Set<String>>();
        HashSet<String> hs = new HashSet<String>();
        hs.add("CS-B");
        actionToShortcuts.put(GlobalLookup.km1.sa1, hs);
        KeymapModel instance = new KeymapModel();
        instance.changeKeymap(profile, actionToShortcuts);
        Map<ShortcutAction, Set<String>> result = instance.getKeymap(profile);
        for (Set<String> s : result.values()) {
            if (s.contains("CS-B")) {
                return ;
            }
        }
        fail("CS-B should have been found");
    }

    /**
     * 
     */
    public void testMergeActions() {
        Collection<ShortcutAction> res = new ArrayList<ShortcutAction>();
        res.add(GlobalLookup.km1.sa1);
        Collection<ShortcutAction> adding = new ArrayList<ShortcutAction>();
        adding.add(GlobalLookup.km2.sa2);
        String name = "Two";
        Set result = KeymapModel.mergeActions(res, adding, name, new HashMap<ShortcutAction, CompoundAction>());
        assertEquals(1, result.size());
        Iterator<ShortcutAction> it = result.iterator();
        ShortcutAction sa = it.next();
        ShortcutAction sa2 = sa.getKeymapManagerInstance("Two");
        assertNotNull(sa2);
    }

    public static class GlobalLookup extends ProxyLookup {
        public static KeymapManager1 km1 = new KeymapManager1();
        public static KeymapManager2 km2 = new KeymapManager2();
        public GlobalLookup() {
            super(Lookups.fixed(km1, km2));
        }
    }
    
    static class KeymapManager1 extends KeymapManager {
        public ShortcutAction sa1 = new ShortcutAction() {
                public String getDisplayName() {
                    return "Action 1";
                }
                public String getId() {
                    return "A1";
                }
                public String getDelegatingActionId() {
                    return null;
                }
                public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
                    if ("One".equals(keymapManagerName)) {
                        return this;
                    }
                    return null;
                }
            };
        public boolean deleteProfileCalled = false;
        public boolean setProfileCalled = false;
        public boolean refreshActionsCalled = false;
        public KeymapManager1() {
            super("One");
        }
        
        public Map<String, Set<ShortcutAction>> getActions() {
            Map<String, Set<ShortcutAction>> m = 
                    new HashMap<String, Set<ShortcutAction>>();
            HashSet<ShortcutAction> h = new HashSet<ShortcutAction>();
            h.add(sa1);
            m.put("C1", h);
            return m;
        }

        public void refreshActions() {
            refreshActionsCalled = true;
        }

        public Map<ShortcutAction, Set<String>> getKeymap(String profileName) {
            Map<ShortcutAction, Set<String>> m = new HashMap<ShortcutAction, Set<String>>();
            Set<String> ss = new HashSet<String>();
            ss.add("CS-A");
            m.put(sa1, ss);
            return m;
        }

        public void saveKeymap(String profileName,
                               Map<ShortcutAction, Set<String>> actionToShortcuts) {
        }

        public List<String> getProfiles() {
            List<String> al = new ArrayList<String>();
            al.add("test");
            return al;
        }

        public String getCurrentProfile() {
            return "test";
        }

        public void setCurrentProfile(String profileName) {
            setProfileCalled = true;
        }

        public void deleteProfile(String profileName) {
            deleteProfileCalled = true;
        }

        public boolean isCustomProfile(String profileName) {
            return false;
        }

        public Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName) {
            return getKeymap(profileName);
        }
    }
    
    private static class KeymapManager2 extends KeymapManager {
        public boolean deleteProfileCalled = false;
        public boolean setProfileCalled = false;
        public boolean refreshActionsCalled = false;
        public ShortcutAction sa2 = new ShortcutAction() {
                public String getDisplayName() {
                    return "Action 2";
                }
                public String getId() {
                    return "A2";
                }
                public String getDelegatingActionId() {
                    return "A1";
                }
                public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
                    if ("Two".equals(keymapManagerName)) {
                        return this;
                    }
                    return null;
                }
            };
        public KeymapManager2() {
            super("Two");
        }
        
        public Map<String, Set<ShortcutAction>> getActions() {
            Map<String, Set<ShortcutAction>> m = 
                    new HashMap<String, Set<ShortcutAction>>();
            HashSet<ShortcutAction> h = new HashSet<ShortcutAction>();
            h.add(sa2);
            m.put("C1", h);
            return m;
        }

        public void refreshActions() {
            refreshActionsCalled = true;
        }

        public Map<ShortcutAction, Set<String>> getKeymap(String profileName) {
            Map<ShortcutAction, Set<String>> m = new HashMap<ShortcutAction, Set<String>>();
            return m;
        }

        public void saveKeymap(String profileName,
                               Map<ShortcutAction, Set<String>> actionToShortcuts) {
        }

        public List<String> getProfiles() {
            return null;
        }

        public String getCurrentProfile() {
            return null;
        }

        public void setCurrentProfile(String profileName) {
            setProfileCalled = true;
        }

        public void deleteProfile(String profileName) {
            deleteProfileCalled = true;
        }

        public boolean isCustomProfile(String profileName) {
            return false;
        }
        
        public Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName) {
            return getKeymap(profileName);
        }        
    }
}
