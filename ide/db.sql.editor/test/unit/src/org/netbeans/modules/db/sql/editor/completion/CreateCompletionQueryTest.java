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
package org.netbeans.modules.db.sql.editor.completion;

/** Tests completion of SQL CREATE statement.
 *
 * @author Jiri Skrivanek
 */
public class CreateCompletionQueryTest extends CompletionQueryTestCase {

    public CreateCompletionQueryTest(String testName) {
        super(testName);
    }

    public void testCreate() {
        String sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "|";
        assertItems(doQuery(sql), "CREATE", "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT |";
        assertItems(doQuery(sql), "tab_customer", "sch_accounting", "sch_customers", "catalog_1", "catalog_2");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT * |";
        assertItems(doQuery(sql), "FROM");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT * FROM |";
        assertItems(doQuery(sql), "tab_customer", "sch_accounting", "sch_customers", "catalog_1", "catalog_2");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT * FROM tab_customer;\n" +
            "|";
        assertItems(doQuery(sql), "CREATE", "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT * FROM tab_customer;\n" +
            "  SELECT |";
        assertItems(doQuery(sql), "tab_customer", "sch_accounting", "sch_customers", "catalog_1", "catalog_2");
        sql =
            "CREATE PROCEDURE p1()\n" +
            "BEGIN\n" +
            "  SELECT * FROM tab_customer;\n" +
            "  SELECT | FROM tab_customer;\n" +
            "END";
        assertItems(doQuery(sql), "col_customer_id", "tab_customer");
        sql = "C|";
        assertItems(doQuery(sql), "CREATE");
        sql = "CREATE |";
        assertItems(doQuery(sql), "DATABASE", "FUNCTION", "PROCEDURE",
                                  "SCHEMA", "TABLE", "TEMPORARY", "VIEW");
        sql = "CREATE TEMPORARY |";
        assertItems(doQuery(sql), "TABLE");
        sql = "CREATE VIEW |";
        assertItems(doQuery(sql), "AS");
        sql = "CREATE VIEW xy AS |";
        assertItems(doQuery(sql), "SELECT");
        // This is a basic to verify "normal" select behaviour, based on
        // structure from CompletionQueryTestCase.java
        sql = "CREATE VIEW xy AS SELECT |";
        assertItems(doQuery(sql), "tab_customer", "sch_accounting", "sch_customer",
                                  "catalog_1", "catalog_2");
    }
}
