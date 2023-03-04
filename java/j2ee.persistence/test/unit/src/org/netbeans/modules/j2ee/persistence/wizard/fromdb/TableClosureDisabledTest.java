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

    @Override
    public void setUp() {
        String schema = "Test_Schema";
        String catalog = null;
        Map<String, Set<String>> tablesAndRefs = new HashMap<>();
        Map<String, DisabledReason> disabledReasons = new HashMap<>();
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

    @Override
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
