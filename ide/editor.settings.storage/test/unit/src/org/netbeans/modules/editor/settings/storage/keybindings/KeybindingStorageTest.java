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
package org.netbeans.modules.editor.settings.storage.keybindings;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.StorageImpl;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class KeybindingStorageTest extends NbTestCase {
    
    /** Creates a new instance of KeybindingsStorageTest */
    public KeybindingStorageTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/keybindings/test-layer-KeybindingStorageTest.xml"),
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

    public void testAllLanguages() throws IOException {
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.<Collection<KeyStroke>, MultiKeyBinding>get(KeyMapsStorage.ID);
        Map<Collection<KeyStroke>, MultiKeyBinding> keybindings = ess.load(MimePath.EMPTY, "MyProfileXyz", true); //NOI18N
        assertNotNull("Keybindings map should not be null", keybindings);
        assertEquals("Wrong number of keybindings", 1, keybindings.size());
        
        checkMapConsistency(keybindings);

        MultiKeyBinding mkb = keybindings.values().iterator().next();
        assertNotNull("MultiKeyBinding should not be null", mkb);
        assertEquals("Wrong action name", "test-action-all-languages-A", mkb.getActionName());
        assertEquals("Wrong number of key strokes", 1, mkb.getKeyStrokeCount());
        assertEquals("Wrong key stroke", Utilities.stringToKey("O-A"), mkb.getKeyStroke(0));
    }

    public void testMultipleFiles() throws IOException {
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.<Collection<KeyStroke>, MultiKeyBinding>get(KeyMapsStorage.ID);
        MimePath mimePath = MimePath.parse("text/x-type-A");
        Map<Collection<KeyStroke>, MultiKeyBinding> keybindings = ess.load(mimePath, "MyProfileXyz", true); //NOI18N
        assertNotNull("Keybindings map should not be null", keybindings);
        assertEquals("Wrong number of keybindings", 5, keybindings.size());
        
        checkMapConsistency(keybindings);

        checkKeybinding(keybindings, "test-action-1", "D-1 A");
        checkKeybinding(keybindings, "test-action-2", "D-2 A");
        checkKeybinding(keybindings, "test-action-2", "D-2 B");
        checkKeybinding(keybindings, "test-action-4", "D-4 B");
        checkKeybinding(keybindings, "test-action-5", "D-5 B");
    }

    public void testWriteKeybindings() throws IOException {
        // Create new keybindings
        Map<Collection<KeyStroke>, MultiKeyBinding> newKeybindings = new HashMap<Collection<KeyStroke>, MultiKeyBinding>();
        MultiKeyBinding mkb = new MultiKeyBinding(Utilities.stringToKeys("D-D D"), "the-super-action");
        newKeybindings.put(mkb.getKeyStrokeList(), mkb);
        
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.<Collection<KeyStroke>, MultiKeyBinding>get(KeyMapsStorage.ID);
        ess.save(MimePath.EMPTY, "MyProfileXyz", false, newKeybindings);
        
        FileObject settingFile = FileUtil.getConfigFile("Editors/Keybindings/MyProfileXyz/org-netbeans-modules-editor-settings-CustomKeybindings.xml");
        assertNotNull("Can't find custom settingFile", settingFile);
        assertEquals("Wrong mime type", KeyMapsStorage.MIME_TYPE, settingFile.getMIMEType());
        
        // Force loading from the files
        StorageImpl<Collection<KeyStroke>, MultiKeyBinding> storage = new StorageImpl<Collection<KeyStroke>, MultiKeyBinding>(new KeyMapsStorage(), null);
        Map<Collection<KeyStroke>, MultiKeyBinding> keybindings = storage.load(MimePath.EMPTY, "MyProfileXyz", false); //NOI18N
        assertNotNull("Keybindings map should not be null", keybindings);
        assertEquals("Wrong number of keybindings", 1, keybindings.size());
        checkKeybinding(keybindings, "the-super-action", "D-D D");
    }
    
    public void testKeybindingsForSpecialTestMimeType() throws Exception {
        final String origMimeType = "text/x-orig";
        final String specialTestMimeType = "test123456_" + origMimeType;
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(specialTestMimeType));
        
        // Check the API class
        Collection<? extends KeyBindingSettings> c = lookup.lookupAll(KeyBindingSettings.class);
        assertEquals("Wrong number of kbs", 1, c.size());
        
        KeyBindingSettings kbs = c.iterator().next();
        assertNotNull("KBS should not be null", kbs);
        assertTrue("Wrong kbs impl", kbs instanceof KeyBindingSettingsImpl.Immutable);
    }

    private void checkMapConsistency(Map<Collection<KeyStroke>, MultiKeyBinding> keybindings) {
        for(Map.Entry<Collection<KeyStroke>,MultiKeyBinding> entry : keybindings.entrySet()) {
            Collection<KeyStroke> keyStrokes = entry.getKey();
            MultiKeyBinding mkb = entry.getValue();
            assertEquals("Inconsistent keystrokes", keyStrokes, mkb.getKeyStrokeList());
        }
    }
    
    private void checkKeybinding(Map<Collection<KeyStroke>, MultiKeyBinding> keybindings, String actionName, String keyStroke) {
        Collection<KeyStroke> strokes = Arrays.asList(Utilities.stringToKeys(keyStroke));
        MultiKeyBinding mkb = keybindings.get(strokes);
        assertNotNull("MultiKeyBinding should not be null", mkb);
        assertEquals("Wrong action name", actionName, mkb.getActionName());
        assertEquals("Wrong key strokes", strokes, mkb.getKeyStrokeList());
    }
}
