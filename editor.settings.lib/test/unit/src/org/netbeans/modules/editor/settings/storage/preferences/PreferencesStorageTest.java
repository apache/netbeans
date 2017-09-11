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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.settings.storage.preferences;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.StorageImpl;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;

/**
 *
 * @author Vita Stejskal
 */
public class PreferencesStorageTest extends NbTestCase {

    public PreferencesStorageTest(String name) {
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
    
    public void testSimple() throws IOException {
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", map.get("simple-value-setting-A").getValue());
    }

    public void testWriting() throws IOException {
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", map.get("simple-value-setting-A").getValue());
        
        HashMap<String, TypedValue> mm = new HashMap<String, TypedValue>();
        mm.put("simple-value-setting-A", typedValue("123"));
        mm.put("simple-value-setting-B", typedValue("xyz"));
        
        storage.save(MimePath.EMPTY, null, false, mm);
        
        // use fresh new StorageImpl
        storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong number of settings", 2, map.size());
        assertEquals("Wrong value for 'simple-value-setting-A'", "123", map.get("simple-value-setting-A").getValue());
        assertEquals("Wrong value for 'simple-value-setting-B'", "xyz", map.get("simple-value-setting-B").getValue());
    }

    // test localization through valueId
    
    public void testLocalizedValues() throws IOException {
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong value for 'localized-setting'", "Hey! This is the value from Bundle.properties!!!", map.get("localized-setting").getValue());
    }
    
    // test reading from multiple files (including remove="true")

    public void testMultiplePreferencesFiles() throws IOException {
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.parse("text/x-testA"), null, false);
        assertNotNull("Preferences map should not be null", map);
        
        check("testA-1-setting-1", map);
        assertFalse("testA-1-setting-2 should be removed", map.containsKey("testA-1-setting-2"));
        check("testA-1-setting-3", "value-of-testA-1-setting-3-from-testA-2", map);
        check("testA-2-setting-1", map);
    }
    
    // test removing settings

    public void testRemovingSettings() throws IOException {
        StorageImpl<String, TypedValue> storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        Map<String, TypedValue> map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong value for 'simple-value-setting-A'", "value-A", map.get("simple-value-setting-A").getValue());
        
        HashMap<String, TypedValue> mm = new HashMap<String, TypedValue>();
        // no simple-value-setting-A in the mm map
        mm.put("simple-value-setting-B", typedValue("ABC-123"));
        mm.put("simple-value-setting-C", typedValue("HowHowHow"));
        mm.put("simple-value-setting-D", typedValue("NowNowNow"));
        
        storage.save(MimePath.EMPTY, null, false, mm);
        
        // use fresh new StorageImpl
        storage = new StorageImpl<String, TypedValue>(new PreferencesStorage(), null);
        map = storage.load(MimePath.EMPTY, null, false);
        assertNotNull("Preferences map should not be null", map);
        assertEquals("Wrong number of settings", 3, map.size());
        check("simple-value-setting-B", "ABC-123", map);
        check("simple-value-setting-C", "HowHowHow", map);
        check("simple-value-setting-D", "NowNowNow", map);
    }
    
    private void check(String settingName, Map<String, TypedValue> settings) {
        check(settingName, "value-of-" + settingName, settings);
    }
    
    private void check(String settingName, String settingValue, Map<String, TypedValue> settings) {
        assertEquals("Wrong value for '" + settingName + "'", settingValue, settings.get(settingName).getValue());
    }
    
    private TypedValue typedValue(Object value) {
        return new TypedValue(value.toString(), value.getClass().getName());
    }
}
