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

/** Tests completion of SQL keywords.
 *
 * @author Jiri Skrivanek
 */
public class KeywordCompletionQueryTest extends CompletionQueryTestCase {

    public KeywordCompletionQueryTest(String testName) {
        super(testName);
    }

    public void testEmptyStatement() {
        String sql = "|";
        assertItems(doQuery(sql), "CREATE", "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql = " |";
        assertItems(doQuery(sql), "CREATE", "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql = "D|";
        assertItems(doQuery(sql), "DELETE", "DROP");
    }

    public void testSelect() {
        String sql = "SELECT|";
        assertItems(doQuery(sql));
        sql = "SELECT * |";
        assertItems(doQuery(sql), "FROM");
        sql = "SELECT col_customer_id |";
        assertItems(doQuery(sql), "FROM");
        sql = "SELECT * FROM|";
        assertItems(doQuery(sql));
        sql = "SELECT * FROM t |";
        assertItems(doQuery(sql), "GROUP", "ORDER", "WHERE");
        sql = "SELECT * FROM (t)|";
        assertItems(doQuery(sql), "GROUP", "ORDER", "WHERE");
        sql = "SELECT * FROM t WHERE|";
        assertItems(doQuery(sql));
        sql = "SELECT * FROM t WHERE c1 > 1 |";
        assertItems(doQuery(sql), "GROUP", "ORDER");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP|";
        assertItems(doQuery(sql));
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP |";
        assertItems(doQuery(sql), "BY");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP BY c3 |";
        assertItems(doQuery(sql), "HAVING");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP |";
        assertItems(doQuery(sql), "BY");
        sql = "SELECT * FROM t WHERE c1 > 1 ORDER |";
        assertItems(doQuery(sql), "BY");

        sql = "SELECT * FROM inner join t1 on c1 |";
        assertItems(doQuery(sql), "WHERE");
    }

    public void testSubqueries() {
        String sql = "SELECT * FROM t WHERE c1 > (SELECT * |";
        assertItems(doQuery(sql), "FROM");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2 |";
        assertItems(doQuery(sql), "GROUP", "ORDER", "WHERE");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2 WHERE c2 = c3 |";
        assertItems(doQuery(sql), "GROUP", "ORDER");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2) |";
        assertItems(doQuery(sql), "GROUP", "ORDER");
    }

    public void testDelete() {
        String sql = "DELETE|";
        assertItems(doQuery(sql));
        sql = "DELETE |"; // multiple table delete supported
        assertItems(doQuery(sql), "FROM", "tab_customer", "sch_accounting", "sch_customers", "catalog_1", "catalog_2");
        sql = "DELETE FROM t |";
        assertItems(doQuery(sql), "GROUP", "ORDER", "WHERE");
    }

    public void testDrop() {
        String sql = "DROP |";
        assertItems(doQuery(sql), "TABLE");
    }

    public void testInsert() {
        String sql = "INSERT |";
        assertItems(doQuery(sql), "INTO");
        sql = "INSERT INTO t |";
        assertItems(doQuery(sql), "VALUES");
        sql = "INSERT INTO tab_customer (col_customer_id) |";
        assertItems(doQuery(sql), "VALUES");
    }

    public void testUpdate() {
        String sql = "UPDATE t |";
        assertItems(doQuery(sql), "SET");
        sql = "UPDATE t SET c1 = c2 |";
        assertItems(doQuery(sql), "WHERE");
    }
}
