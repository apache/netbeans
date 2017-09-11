/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        for(Collection<KeyStroke> keyStrokes : keybindings.keySet()) {
            MultiKeyBinding mkb = keybindings.get(keyStrokes);
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
