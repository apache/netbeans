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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.server.properties;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hejl
 */
public class InstancePropertiesTest extends NbTestCase {

    private static final String PLUGIN_NAME = "test"; // NOI18N

    public InstancePropertiesTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);
        prefs.removeNode();
    }

    public void testCreate() {
        InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
        assertNotNull(manager);

        InstanceProperties props1 = manager.createProperties(PLUGIN_NAME);
        String id = props1.getId();
        assertNotNull(props1);

        List<InstanceProperties> allProps = manager.getProperties(PLUGIN_NAME);
        assertEquals(1, allProps.size());
        assertEquals(id, allProps.get(0).getId());

        InstanceProperties props2 = manager.createProperties(PLUGIN_NAME);
        assertNotNull(props2);
        assertNotSame(id, props2.getId());

        allProps = manager.getProperties(PLUGIN_NAME);
        assertEquals(2, allProps.size());

        Set<String> ids = new HashSet<String>();
        Collections.addAll(ids, props1.getId(), props2.getId());
        for (InstanceProperties props : allProps) {
            ids.remove(props.getId());
        }
        assertTrue(ids.isEmpty());
    }

    public void testStorage() throws InterruptedException {
        InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
        assertNotNull(manager);

        InstanceProperties props = manager.createProperties(PLUGIN_NAME);
        assertNotNull(props);

        props.putString("property1", "value1"); // NOI18N
        props.putString("property2", "value2"); // NOI18N
        assertEquals("value1", props.getString("property1", "default")); // NOI18N
        assertEquals("value2", props.getString("property2", "default")); // NOI18N
        props.removeKey("property1"); // NOI18N
        assertEquals("default", props.getString("property1", "default")); // NOI18N

        props.putBoolean("property3", true); // NOI18N
        props.putBoolean("property4", false); // NOI18N
        assertTrue(props.getBoolean("property3", false)); // NOI18N
        assertFalse(props.getBoolean("property4", true)); // NOI18N
        props.removeKey("property3"); //NOI18N
        assertFalse(props.getBoolean("property3", false)); // NOI18N

        props.putDouble("property5", 5.0); // NOI18N
        props.putDouble("property6", 9.0); // NOI18N
        assertEquals(5.0, props.getDouble("property5", 1.0)); // NOI18N
        assertEquals(9.0, props.getDouble("property6", 1.0)); // NOI18N
        props.removeKey("property5"); //NOI18N
        assertEquals(1.0, props.getDouble("property5", 1.0)); // NOI18N

        props.putFloat("property7", 6.0f); // NOI18N
        props.putFloat("property8", 8.0f); // NOI18N
        assertEquals(6.0f, props.getFloat("property7", 1.0f)); // NOI18N
        assertEquals(8.0f, props.getFloat("property8", 1.0f)); // NOI18N
        props.removeKey("property7"); //NOI18N
        assertEquals(1.0f, props.getFloat("property7", 1.0f)); // NOI18N

        props.putInt("property9", 5); // NOI18N
        props.putInt("property10", 9); // NOI18N
        assertEquals(5, props.getInt("property9", 1)); // NOI18N
        assertEquals(9, props.getInt("property10", 1)); // NOI18N
        props.removeKey("property9"); //NOI18N
        assertEquals(1, props.getInt("property9", 1)); // NOI18N

        props.putLong("property11", 6L); // NOI18N
        props.putLong("property12", 8L); // NOI18N
        assertEquals(6L, props.getLong("property11", 1L)); // NOI18N
        assertEquals(8L, props.getLong("property12", 1L)); // NOI18N
        props.removeKey("property11"); //NOI18N
        assertEquals(1L, props.getLong("property11", 1L)); // NOI18N
    }

    public void testRemove() {
        InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
        assertNotNull(manager);

        InstanceProperties props = manager.createProperties(PLUGIN_NAME);
        assertNotNull(props);

        props.remove();

        List<InstanceProperties> allProps = manager.getProperties(PLUGIN_NAME);
        assertTrue(allProps.isEmpty());

        try {
            props.getString("property", "default"); // NOI18N
            fail("Did not throw exception after remove"); // NOI18N
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCache() {
        InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
        assertNotNull(manager);

        InstanceProperties props = manager.createProperties(PLUGIN_NAME);
        assertNotNull(props);

        List<InstanceProperties> allProps = manager.getProperties(PLUGIN_NAME);
        assertEquals(1, allProps.size());

        assertEquals(props, allProps.get(0));

        props.remove();

        allProps = manager.getProperties(PLUGIN_NAME);
        assertTrue(allProps.isEmpty());
    }

}
