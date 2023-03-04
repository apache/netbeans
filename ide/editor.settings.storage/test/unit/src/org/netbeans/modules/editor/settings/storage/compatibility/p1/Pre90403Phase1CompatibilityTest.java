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

package org.netbeans.modules.editor.settings.storage.compatibility.p1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.MimeTypesTracker;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class Pre90403Phase1CompatibilityTest extends NbTestCase {
    
    /** Creates a new instance of Pre90403Phase1Compatibility */
    public Pre90403Phase1CompatibilityTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
                new File(getDataDir(), "SFS-Editors-Folder-8.2.zip").toURI().toURL(),
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();

        // Sanity check
        FileObject f = FileUtil.getConfigFile("Editors/text/x-java/NetBeans/Defaults/coloring.xml");
        assertNotNull("Corrupted SystemFileSystem!", f);
    }
    
    public void testColoringProfiles() {
        Set<String> profiles = EditorSettings.getDefault().getFontColorProfiles();
        ArrayList<String> sortedProfiles = new ArrayList<String>(profiles);
        Collections.sort(sortedProfiles);
        String currentProfiles = sortedProfiles.toString();
        assertEquals("Wrong coloring profiles",
            "[CityLights, NetBeans]",
            currentProfiles);
    }
    
    public void testMimeTypesWithColorings() {
        Set<String> mimeTypes = EditorSettings.getDefault().getMimeTypes();
        ArrayList<String> sortedMimeTypes = new ArrayList<String>(mimeTypes);
        Collections.sort(sortedMimeTypes);
        String currentMimeTypes = sortedMimeTypes.toString();
        assertEquals("Wrong coloring mime types",
            "[application/xml-dtd, text/css, text/html, text/plain, text/x-el, text/x-java, text/x-jsp, text/x-properties, text/x-sql, text/x-tag, text/xml]",
            currentMimeTypes);
    }
    
    public void testColorings() {
        Set<String> mimeTypes = new HashSet<String>(EditorSettings.getDefault().getMimeTypes());
        mimeTypes.add("");
        Set<String> profiles = EditorSettings.getDefault().getFontColorProfiles();
        
        for(String mimeType : mimeTypes) {
            for(String profile : profiles) {
                Collection<AttributeSet> colorings = EditorSettings.getDefault().getFontColorSettings(mimeType.length() == 0 ? new String[0] : new String [] { mimeType }).getAllFontColors(profile);
                Map<String, Map<String, String>> norm = normalize(colorings);
                
                String current = norm.toString();
                String golden = fromFile("C-" + mimeType.replace("/", "-") + "-" + profile);
                
                assertEquals("Wrong colorings for '" + mimeType + "', profile '" + profile + "'", golden, current);
            }
        }
    }
    
    public void testKeybindingsProfiles() {
        Set<String> profiles = EditorSettings.getDefault().getKeyMapProfiles();
        ArrayList<String> sortedProfiles = new ArrayList<String>(profiles);
        Collections.sort(sortedProfiles);
        String currentProfiles = sortedProfiles.toString();
        assertEquals("Wrong keybindings profiles",
            "[Eclipse, Emacs, NetBeans]",
            currentProfiles);
    }
    
    public void testMimeTypesWithKeybindings() {
        MimeTypesTracker tracker = MimeTypesTracker.get(KeyMapsStorage.ID, "Editors");
        ArrayList<String> sortedMimeTypes = new ArrayList<String>(tracker.getMimeTypes());
        Collections.sort(sortedMimeTypes);
        String currentMimeTypes = sortedMimeTypes.toString();
        assertEquals("Wrong keybindings mime types",
            "[text/x-java, text/x-jsp]",
            currentMimeTypes);
    }
    
    public void testKeybindings() {
        MimeTypesTracker tracker = MimeTypesTracker.get(KeyMapsStorage.ID, "Editors");
        Set<String> mimeTypes = new HashSet<String>(tracker.getMimeTypes());
        Set<String> profiles = EditorSettings.getDefault().getKeyMapProfiles();
        
        for(String profile : profiles) {
            List<MultiKeyBinding> commonKeybindings = EditorSettings.getDefault().getKeyBindingSettings(new String[0]).getKeyBindings(profile);
            Map<String, String> commonNorm = normalize(commonKeybindings);

            String commonCurrent = commonNorm.toString();
            String commonGolden = fromFile("KB--" + profile);

            assertEquals("Wrong keybindings for '', profile '" + profile + "'", commonGolden, commonCurrent);

            for(String mimeType : mimeTypes) {
                List<MultiKeyBinding> keybindings = EditorSettings.getDefault().getKeyBindingSettings(mimeType.length() == 0 ? new String[0] : new String [] { mimeType }).getKeyBindings(profile);

                Map<String, String> mimeTypeNorm = new TreeMap<String, String>();
                Map<String, String> norm = normalize(keybindings);
                
                mimeTypeNorm.putAll(commonNorm);
                mimeTypeNorm.putAll(norm);
                
                String current = mimeTypeNorm.toString();
                String golden = fromFile("KB-" + mimeType.replace("/", "-") + "-" + profile);
                
                assertEquals("Wrong keybindings for '" + mimeType + "', profile '" + profile + "'", golden, current);
            }
        }
    }
    
    private static Map<String, Map<String, String>> normalize(Collection<AttributeSet> colorings) {
        Map<String, Map<String, String>> norm = new TreeMap<String, Map<String, String>>();
        
        for(AttributeSet as : colorings) {
            String name = (String) as.getAttribute(StyleConstants.NameAttribute);
            
            assertNotNull("NameAttribute should not be null", name);
            
            name = "'" + name + "'";
            assertFalse("Duplicate AttributeSet with name " + name, norm.containsKey(name));
            
            Map<String, String> attribs = new TreeMap<String, String>();
            norm.put(name, attribs);
            
            Enumeration<? extends Object> names = as.getAttributeNames();
            while(names.hasMoreElements()) {
                Object attrName = names.nextElement();
                Object attrValue = as.getAttribute(attrName);
                String normalizedName = attrName == null ? "'null'" : "'" + attrName.toString() + "'";
                String normalizedValue = attrValue == null ? "'null'" : "'" + attrValue.toString() + "'";
                
                assertFalse("Duplicate attribute '" + normalizedName + "'", attribs.containsKey(normalizedName));
                attribs.put(normalizedName, normalizedValue);
            }
        }
        
        return norm;
    }

    private static Map<String, String> normalize(List<MultiKeyBinding> keybindings) {
        Map<String, String> norm = new TreeMap<String, String>();
        
        for(MultiKeyBinding mkb : keybindings) {
            StringBuilder strokes = new StringBuilder();
            
            for(Iterator<KeyStroke> i = mkb.getKeyStrokeList().iterator(); i.hasNext(); ) {
                KeyStroke stroke = i.next();
                String s = Utilities.keyToString(stroke);
                
                strokes.append(s);
                if (i.hasNext()) {
                    strokes.append(" ");
                }
            }
            
            String mkbId = "'" + strokes.toString() + "'";
            String normalizedActionName = mkb.getActionName() == null ? "'null'" : "'" + mkb.getActionName() + "'";
            
            assertFalse("Dulicate MultiKeyBinding '" + mkbId + "'", norm.containsKey(mkbId));
            norm.put(mkbId, normalizedActionName);
        }
        
        return norm;
    }
    
    private static String fromFile(String name) {
        URL url = Pre90403Phase1CompatibilityTest.class.getResource(name);
        if (url == null) {
            return "";
        }
        
        try {
            InputStream is = url.openStream();
            try {
                StringBuilder sb = new StringBuilder();
                byte [] buffer = new byte [1024];
                int size;

                while(0 < (size = is.read(buffer, 0, buffer.length))) {
                    String s = new String(buffer, 0, size);
                    sb.append(s);
                }

                return sb.toString();
            } finally {
                is.close();
            }
        } catch (IOException e) {
            fail("Can't read file: '" + name + "'");
            return null; // can't be reached
        }
    }
}
