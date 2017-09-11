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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureDisabledTest extends TestCase {

    private TableProviderImpl provider;
    private TableClosure closure;

    public TableClosureDisabledTest(String testName) {
        super(testName);
    }

    public void setUp() {
        String schema = "Test_Schema";
        String catalog = null;
        Map<String, Set<String>> tablesAndRefs = new HashMap<String, Set<String>>();
        Map<String, DisabledReason> disabledReasons = new HashMap<String, DisabledReason>();
        Set<String> empty = Collections.emptySet();

        tablesAndRefs.put("ROOM", empty);
        tablesAndRefs.put("STUDENT", empty);
        tablesAndRefs.put("TEACHER", empty);
        tablesAndRefs.put("STUDENT_TEACHER", new HashSet(Arrays.asList(new String[] { "TEACHER", "STUDENT" })));
        tablesAndRefs.put("ZOO1", empty);
        tablesAndRefs.put("ZOO2", empty);
        tablesAndRefs.put("ZOO1_ZOO2", new HashSet(Arrays.asList(new String[] { "ZOO1", "ZOO2" })));

        disabledReasons.put("ROOM", new DisabledReason("Disabled", "Description"));
        disabledReasons.put("STUDENT", new DisabledReason("Disabled", "Description"));
        disabledReasons.put("ZOO1_ZOO2", new DisabledReason("Disabled", "Description"));

        provider = new TableProviderImpl(catalog, schema, tablesAndRefs, disabledReasons);
        closure = new TableClosure(provider);
    }

    public void tearDown() {
        closure = null;
    }

    public void testAddAllWithClosureEnabledDoesntAddDisabledTables() {
        closure.addAllTables();


        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testAddAllWithClosureDisabledDoesntAddDisabledTables() {
        closure.setClosureEnabled(false);
        closure.addAllTables();

        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testCannotAddDisabledReferencedTable() {
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT_TEACHER")));

        // STUDENT, which is also referenced, but disabled, was not added
        assertTables(new String[] { "STUDENT_TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1", "ZOO2", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testCannotAddDisabledJoinTable() {
        closure.addTables(Collections.singleton(provider.getTableByName("ZOO1")));
        closure.addTables(Collections.singleton(provider.getTableByName("ZOO2")));

        // ZOO1_ZOO2, which is a join table for the added tables, but disabled, was not added
        assertTables(new String[] { "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "STUDENT_TEACHER", "TEACHER", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    private void assertTables(String[] expected, Set<Table> actual) {
        assertEquals(expected.length, actual.size());
        for (String tableName : expected) {
            assertNotNull(actual.contains(tableName));
        }
    }
}
