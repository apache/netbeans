/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import junit.framework.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureJoinTest extends TestCase {

    private TableProviderImpl provider;
    private TableClosure closure;

    public TableClosureJoinTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        Map<String, Set<String>> tablesAndRefs = new HashMap<>();
        Set<String> empty = Collections.emptySet();

        tablesAndRefs.put("BAR", empty);
        tablesAndRefs.put("FOO", empty);
        tablesAndRefs.put("ROOM", empty);
        tablesAndRefs.put("STUDENT", empty);
        tablesAndRefs.put("TEACHER", empty);
        tablesAndRefs.put("STUDENT_TEACHER", new HashSet(Arrays.asList(new String[] { "TEACHER", "STUDENT" })));
        tablesAndRefs.put("ROOM_STUDENT", new HashSet(Arrays.asList(new String[] { "STUDENT", "ROOM" })));
        tablesAndRefs.put("FOO_ROOM_STUDENT", new HashSet(Arrays.asList(new String[] { "FOO", "ROOM_STUDENT" })));

        provider = new TableProviderImpl(null, null, tablesAndRefs);
        closure = new TableClosure(provider);
    }

    @Override
    public void tearDown() {
        closure = null;
    }

    public void testBasic() {
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));

        assertTables(new String[] { "ROOM", "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "ROOM_STUDENT", "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "ROOM", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER" }, closure.getAvailableTables());

        closure.addAllTables();

        // assert adding all tables selects the tables based on the "normal" direction of the
        // relationships, not on the "inverse" direction, as it happens for join tables.
        // that is, the STUDENT and TEACHER tables are marked as referenced,
        // not STUDENT_TEACHER -- since the direction of the relationships is
        // STUDENT_TEACHER -> (TEACHER, STUDENT)
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "FOO", "ROOM", "ROOM_STUDENT", "STUDENT", "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.removeAllTables();

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }


    /**
     * Tests that adding a table which causes the addition of a join table which causes
     * the addition of another join table works. Here adding STUDENT causes
     * ROOM_STUDENT to be added, which causes FOO_ROOM_STUDENT to be added.
     */
    public void testRecursiveJoin() {
        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));
        closure.addTables(Collections.singleton(provider.getTableByName("FOO")));
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }

    /**
     * Tests that adding and removing a table from a set containing a
     * related join table does not cause the reordering or the references (that is,
     * it is still the join table that is marked as referenced, not the tables it joins).
     */
    public void testRemoveDoesNotCauseReferenceReordering() {
        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));
        closure.addTables(Collections.singleton(provider.getTableByName("FOO")));
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        // the result is ok, tested in testRecursiveJoin()

        closure.addTables(Collections.singleton(provider.getTableByName("BAR")));

        assertTables(new String[] { "BAR", "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("BAR")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT", "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }

    /**
     * Tests that an already selected table is never added as a join table. For example,
     * adding ROOM, STUDENT, STUDENT_TEACHER and TEACHER will add ROOM_STUDENT as a join
     * table, but should not mark STUDENT_TEACHER as a join (referenced) table.
     */
    public void testNeverAddingAlreadySelectedTablesAsJoinTables() {
        HashSet<Table> tables = new HashSet<>();
        tables.add(provider.getTableByName("ROOM"));
        tables.add(provider.getTableByName("STUDENT"));
        tables.add(provider.getTableByName("STUDENT_TEACHER"));
        tables.add(provider.getTableByName("TEACHER"));

        closure.addTables(tables);

        assertTables(new String[] { "ROOM", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "STUDENT", "TEACHER", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT" }, closure.getAvailableTables());
    }

    private void assertTables(String[] expected, Set<Table> actual) {
        assertEquals(expected.length, actual.size());
        for (String tableName : expected) {
            assertNotNull(actual.contains(tableName));
        }
    }
}
