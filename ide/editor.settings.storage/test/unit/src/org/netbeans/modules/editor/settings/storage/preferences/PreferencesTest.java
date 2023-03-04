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

package org.netbeans.modules.editor.settings.storage.preferences;

import java.net.URL;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.StorageImpl;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;

/**
 *
 * @author Vita Stejskal
 */
public class PreferencesTest extends NbTestCase {
    
    public PreferencesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/preferences/test-layer-PreferencesStorageTest.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }
    
    public static TestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new PreferencesTest("testSimple"));
        suite.addTest(new PreferencesTest("testWriting"));
        suite.addTest(new PreferencesTest("testEvents1"));
        suite.addTest(new PreferencesTest("testEvents2"));
        suite.addTest(new PreferencesTest("testEvents142723"));
        
        return suite;
    }
    
    public void testSimple() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", prefs.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'localized-setting'", "Hey! This is the value from Bundle.properties!!!", prefs.get("localized-setting", null));
        
        prefs = MimeLookup.getLookup(MimePath.parse("text/x-testA")).lookup(Preferences.class);
        // inherited from MimePath.EMPTY
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", prefs.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'localized-setting'", "Hey! This is the value from Bundle.properties!!!", prefs.get("localized-setting", null));
        // from text/x-testA
        assertEquals("Wrong value for 'testA-1-setting-1'", "value-of-testA-1-setting-1", prefs.get("testA-1-setting-1", null));
        assertEquals("Wrong value for 'testA-1-setting-2'", "The-Default-Value", prefs.get("testA-1-setting-2", "The-Default-Value"));
        assertEquals("Wrong value for 'testA-1-setting-3'", "value-of-testA-1-setting-3-from-testA-2", prefs.get("testA-1-setting-3", null));
        assertEquals("Wrong value for 'testA-2-setting-1'", "value-of-testA-2-setting-1", prefs.get("testA-2-setting-1", null));
    }

    public void testWriting() throws Exception {
        {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", prefs.get("simple-value-setting-A", null));
        
        prefs.put("simple-value-setting-A", "New-Written-Value");
        assertEquals("Wrong value for 'simple-value-setting-A'", "New-Written-Value", prefs.get("simple-value-setting-A", null));

        prefs.sync();
        }

        {
        // read the settings right from the file
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.EMPTY, null, false);
        assertEquals("Wrong value for 'simple-value-setting-A'", "New-Written-Value", map.get("simple-value-setting-A").getValue());
        }
    }
    
    public void testEvents1() throws Exception {
        Preferences prefsA = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        L listenerA = new L();
        prefsA.addPreferenceChangeListener(listenerA);

        Preferences prefsB = MimeLookup.getLookup(MimePath.parse("text/x-testA")).lookup(Preferences.class);
        L listenerB = new L();
        prefsB.addPreferenceChangeListener(listenerB);
        
        assertNotNull("'simple-value-setting-A' should not be null", prefsA.get("simple-value-setting-A", null));
        assertNotNull("'simple-value-setting-A' should not be null", prefsB.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'testA-1-setting-1'", "value-of-testA-1-setting-1", prefsB.get("testA-1-setting-1", null));

        assertEquals("There should be no A events", 0, listenerA.count);
        assertEquals("There should be no B events", 0, listenerB.count);
        
        prefsA.put("simple-value-setting-A", "new-value");
        assertEquals("Wrong value for 'simple-value-setting-A'", "new-value", prefsA.get("simple-value-setting-A", null));
        assertEquals("The value for 'simple-value-setting-A' was not propagated", "new-value", prefsB.get("simple-value-setting-A", null));
        
        Thread.sleep(500);
        
        assertEquals("Wrong number of A events", 1, listenerA.count);
        assertEquals("Wrong setting name in the A event", "simple-value-setting-A", listenerA.lastEvent.getKey());
        assertEquals("Wrong setting value in the A event", "new-value", listenerA.lastEvent.getNewValue());
        assertSame("Wrong Preferences instance in the A event", prefsA, listenerA.lastEvent.getNode());
        assertEquals("Wrong number of B events", 1, listenerB.count);
        assertEquals("Wrong setting name in the B event", "simple-value-setting-A", listenerB.lastEvent.getKey());
        assertEquals("Wrong setting value in the B event", "new-value", listenerB.lastEvent.getNewValue());
        assertSame("Wrong Preferences instance in the B event", prefsB, listenerB.lastEvent.getNode());
    }
    
    public void testEvents2() throws Exception {
        Preferences prefsA = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        L listenerA = new L();
        prefsA.addPreferenceChangeListener(listenerA);

        Preferences prefsB = MimeLookup.getLookup(MimePath.parse("text/x-testA")).lookup(Preferences.class);
        L listenerB = new L();
        prefsB.addPreferenceChangeListener(listenerB);
        
        String origValue = prefsA.get("simple-value-setting-A", null);
        assertNotNull("'simple-value-setting-A' should not be null", origValue);
        assertNotNull("'simple-value-setting-A' should not be null", prefsB.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'testA-1-setting-1'", "value-of-testA-1-setting-1", prefsB.get("testA-1-setting-1", null));

        assertEquals("There should be no A events", 0, listenerA.count);
        assertEquals("There should be no B events", 0, listenerB.count);
        
        prefsB.put("simple-value-setting-A", "another-value-for-testA");
        assertEquals("Wrong value for 'simple-value-setting-A'", origValue, prefsA.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'simple-value-setting-A' in 'text/x-testA'", "another-value-for-testA", prefsB.get("simple-value-setting-A", null));
        
        Thread.sleep(500);
        
        assertEquals("Wrong number of A events", 0, listenerA.count);
        assertEquals("Wrong number of B events", 1, listenerB.count);
        assertEquals("Wrong setting name in the B event", "simple-value-setting-A", listenerB.lastEvent.getKey());
        assertEquals("Wrong setting value in the B event", "another-value-for-testA", listenerB.lastEvent.getNewValue());
        assertSame("Wrong Preferences instance in the B event", prefsB, listenerB.lastEvent.getNode());
        
        listenerA.count = 0; listenerA.lastEvent = null;
        listenerB.count = 0; listenerB.lastEvent = null;
        
        // now change the value in MimeType.EMPTY
        
        prefsA.put("simple-value-setting-A", "another-value-for-MimeType.EMPTY");
        assertEquals("Wrong value for 'simple-value-setting-A'", "another-value-for-MimeType.EMPTY", prefsA.get("simple-value-setting-A", null));
        assertEquals("Wrong value for 'simple-value-setting-A' in 'text/x-testA'", "another-value-for-testA", prefsB.get("simple-value-setting-A", null));
        
        Thread.sleep(500);
        
        assertEquals("Wrong number of A events", 1, listenerA.count);
        assertEquals("Wrong setting name in the A event", "simple-value-setting-A", listenerA.lastEvent.getKey());
        assertEquals("Wrong setting value in the A event", "another-value-for-MimeType.EMPTY", listenerA.lastEvent.getNewValue());
        assertSame("Wrong Preferences instance in the A event", prefsA, listenerA.lastEvent.getNode());
        assertEquals("Wrong number of B events", 0, listenerB.count);
    }

    @RandomlyFails
    public void testEvents142723() throws Exception {
        Preferences prefsA = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        Preferences prefsB = MimeLookup.getLookup(MimePath.parse("text/x-testA")).lookup(Preferences.class);

        String key1 = "all-lang-key-" + getName();
        prefsA.put(key1, "xyz");
        assertEquals("'" + key1 + "' has wrong value", "xyz", prefsA.get(key1, null));

        // attach listeners
        L listenerA = new L();
        prefsA.addPreferenceChangeListener(listenerA);
        L listenerB = new L();
        prefsB.addPreferenceChangeListener(listenerB);
        
        // putting the same value again should not fire an event
        prefsA.put(key1, "xyz");
        assertEquals("'" + key1 + "' has wrong value", "xyz", prefsA.get(key1, null));
        assertEquals("There should be no events from prefsA", 0, listenerA.count);

        assertEquals("'" + key1 + "' should inherit the value", "xyz", prefsB.get(key1, null));
        assertEquals("There should be no events from prefsB", 0, listenerB.count);

        // putting the same value again should not fire an event
        prefsB.put(key1, "xyz");
        assertEquals("'" + key1 + "' has wrong value in prefsB", "xyz", prefsB.get(key1, null));
        assertEquals("There should still be no events from prefsB", 0, listenerB.count);
    }

    private static final class L implements PreferenceChangeListener {
        public int count = 0;
        public PreferenceChangeEvent lastEvent = null;
        
        public void preferenceChange(PreferenceChangeEvent evt) {
            count++;
            lastEvent = evt;
        }
    } // End of L class
}

