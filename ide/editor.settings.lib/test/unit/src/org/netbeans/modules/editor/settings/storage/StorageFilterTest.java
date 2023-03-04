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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.netbeans.modules.editor.settings.storage.preferences.PreferencesStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.openide.util.Utilities;

/**
 *
 * @author vita
 */
public class StorageFilterTest extends NbTestCase {
    
    public StorageFilterTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/test-layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {
                new FilterA(),
                new FilterB()
            },
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testPreferencesSimple() throws IOException {
        EditorSettingsStorage<String, String> ess = EditorSettingsStorage.<String, String>find(PreferencesStorage.ID);
        Map<String, String> map = ess.load(MimePath.parse("text/x-StorageFilterTest"), null, true);
        assertNotNull("Preferences map should not be null", map);
        Object filterAkey1value = map.get("filterA-key-1");
        assertEquals("Wrong value of 'filterA-key-1'", "filterA-key-1-value", filterAkey1value);
    }
    
    public void testKeybindingsSimple() throws IOException {
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.<Collection<KeyStroke>, MultiKeyBinding>find(KeyMapsStorage.ID);
        Map<Collection<KeyStroke>, MultiKeyBinding> map = ess.load(MimePath.parse("text/x-StorageFilterTest"), "NetBeans", true);
        assertNotNull("Keybindings map should not be null", map);
        MultiKeyBinding filterBshortcut = map.get(Arrays.asList(Utilities.stringToKey("CAS-Q")));
        assertEquals("Wrong value of 'CAS-Q' shortcut", "filterB-injected-action-1", filterBshortcut.getActionName());
    }
    
    public static final class FilterA extends StorageFilter<String, String> {
        public FilterA() {
            super(PreferencesStorage.ID);
        }
        
        @Override
        public void afterLoad(Map<String, String> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
            map.put("filterA-key-1", "filterA-key-1-value");
        }

        @Override
        public void beforeSave(Map<String, String> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
            map.remove("filterA-key-1");
        }
    } // End of FilterA

    public static final class FilterB extends StorageFilter<Collection<KeyStroke>, MultiKeyBinding> {
        public FilterB() {
            super(KeyMapsStorage.ID);
        }
        
        @Override
        public void afterLoad(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
            KeyStroke key = Utilities.stringToKey("CAS-Q");
            map.put(Arrays.asList(key), new MultiKeyBinding(key, "filterB-injected-action-1"));
        }

        @Override
        public void beforeSave(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
            KeyStroke key = Utilities.stringToKey("CAS-Q");
            map.remove(Arrays.asList(key));
        }
    } // End of FilterB

}
