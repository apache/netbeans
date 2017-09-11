/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.options.keymap.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jhavlin
 */
public class KeyStrokeUtilsTest extends NbTestCase {

    private static final String DEFAULT_PROFILE = "NetBeans";           //NOI18N

    public KeyStrokeUtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(TestKeymapManager.class);
    }

    public void testGetKeyStrokesForAction() {
        List<KeyStroke[]> s1 = KeyStrokeUtils.getKeyStrokesForAction(
                "testX1", null);
        List<KeyStroke[]> s2 = KeyStrokeUtils.getKeyStrokesForAction(
                "testX2", null);
        assertEquals(1, s1.size());
        assertEquals(1, s2.size());
        assertEquals(1, s1.get(0).length);
        assertEquals(2, s2.get(0).length);
        assertEquals((int) 'O', s1.get(0)[0].getKeyCode());
        assertEquals((int) 'T', s2.get(0)[0].getKeyCode());
        assertEquals((int) 'M', s2.get(0)[1].getKeyCode());
    }

    public static class TestKeymapManager extends KeymapManager {

        private String profile = DEFAULT_PROFILE;
        private ShortcutAction sa1 = new TestShortcutAction("testX1");
        private ShortcutAction sa2 = new TestShortcutAction("testX2");
        private Map<ShortcutAction, Set<String>> defaultKeyMap =
                new HashMap<ShortcutAction, Set<String>>();
        private Map<ShortcutAction, Set<String>> currentKeyMap =
                new HashMap<ShortcutAction, Set<String>>();
        private Map<String, Set<ShortcutAction>> actionMap =
                new HashMap<String, Set<ShortcutAction>>();

        public TestKeymapManager() {
            super("Test");
            Set<ShortcutAction> allActions = new HashSet<ShortcutAction>();
            Collections.addAll(allActions, sa1, sa2);
            actionMap.put(DEFAULT_PROFILE, allActions);
            defaultKeyMap.put(sa1, Collections.singleton("C-O"));
            defaultKeyMap.put(sa2, Collections.singleton("M-T M-M"));
            currentKeyMap.putAll(defaultKeyMap);
        }

        @Override
        public Map<String, Set<ShortcutAction>> getActions() {
            return actionMap;
        }

        @Override
        public void refreshActions() {
        }

        @Override
        public Map<ShortcutAction, Set<String>> getKeymap(String profileName) {
            return currentKeyMap;
        }

        @Override
        public Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName) {
            return defaultKeyMap;
        }

        @Override
        public void saveKeymap(String profileName, Map<ShortcutAction, Set<String>> actionToShortcuts) {
            // nothing
        }

        @Override
        public List<String> getProfiles() {
            return Collections.singletonList(profile);
        }

        @Override
        public String getCurrentProfile() {
            return profile;
        }

        @Override
        public void setCurrentProfile(String profileName) {
            this.profile = profileName;
        }

        @Override
        public void deleteProfile(String profileName) {
            // nothing
        }

        @Override
        public boolean isCustomProfile(String profileName) {
            return !profileName.equals(DEFAULT_PROFILE);
        }

        private class TestShortcutAction implements ShortcutAction {

            public TestShortcutAction(String name) {
                this.name = name;
            }
            private String name;

            @Override
            public String getDisplayName() {
                return name;
            }

            @Override
            public String getId() {
                return name;
            }

            @Override
            public String getDelegatingActionId() {
                return null;
            }

            @Override
            public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
                if (keymapManagerName.equals(TestKeymapManager.this.getName())) {
                    return this;
                } else {
                    return null;
                }
            }
        }
    }
}
