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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
