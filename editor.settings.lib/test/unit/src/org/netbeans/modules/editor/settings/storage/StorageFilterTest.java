/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
