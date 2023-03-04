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

package org.netbeans.modules.db.api.sql.execute;

import junit.framework.Test;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.db.test.DBTestBase;

/**
 *
 * @author David Van Couvering
 */
public class SQLExecutorTest extends DBTestBase {
    
    private DatabaseConnection dbconn;

    public SQLExecutorTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dbconn = getDatabaseConnection(true);

        createTestTable();

        if (isMySQL()) {
            createRentalTable();
        }
    }
    
    private void createRentalTable() throws Exception {
        assertTrue(isMySQL());

        String sql = "USE " + getSchema() + "; CREATE TABLE rental ( " +
          "rental_id INT NOT NULL AUTO_INCREMENT, " +
          "rental_date DATETIME NOT NULL, " +
          "inventory_id MEDIUMINT UNSIGNED NOT NULL, " +
          "customer_id SMALLINT UNSIGNED NOT NULL, " +
          "return_date DATETIME DEFAULT NULL, " +
          "staff_id TINYINT UNSIGNED NOT NULL, " +
          "last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
          "PRIMARY KEY (rental_id), " +
          "UNIQUE KEY  (rental_date,inventory_id,customer_id), " +
          "KEY idx_fk_inventory_id (inventory_id), " +
          "KEY idx_fk_customer_id (customer_id), " + 
          "KEY idx_fk_staff_id (staff_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        checkExecution(SQLExecutor.execute(dbconn, sql));
}

    public void testExecuteOnClosedConnection() throws Exception {
        DatabaseConnection broken = getDatabaseConnection(false);

        ConnectionManager.getDefault().disconnect(broken);

        try {
            SQLExecutor.execute(broken, "SELECT ydayaday");
            fail("No exception when executing on a closed connection");
        } catch (DatabaseException dbe) {
            // expected
        }
    }

    public void testExecute() throws Exception {
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, "SELECT * FROM " + getTestTableName() + ";");
        checkExecution(info);
        assertTrue(info.getStatementInfos().size() == 1);

        info = SQLExecutor.execute(dbconn, "SELECT * FROM " + getTestTableName() + "; SELECT " + getTestTableIdName() + " FROM " + getTestTableName() + ";");
        checkExecution(info);
        assertTrue(info.getStatementInfos().size() == 2);
    }

    public void testBadExecute() throws Exception {
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, "SELECT * FROM BADTABLE;");

        assertTrue(info.hasExceptions());
    }
            
    private void checkExecution(SQLExecutionInfo info) throws Exception {
        assertNotNull(info);

        Throwable throwable = null;
        if (info.hasExceptions()) {
            for (StatementExecutionInfo stmtinfo : info.getStatementInfos()) {
                if (stmtinfo.hasExceptions()) {
                    System.err.println("The following SQL had exceptions:");
                } else {
                    System.err.println("The following SQL executed cleanly:");
                }
                System.err.println(stmtinfo.getSQL());

                for  (Throwable t : stmtinfo.getExceptions()) {
                    t.printStackTrace();
                    
                    throwable = t;
                }
            }

            Exception e = new Exception("Executing SQL generated exceptions - see output for details");
            e.initCause(throwable);
            throw e;
        }        
    }
    
    public void testMySQLStoredFunction() throws Exception {
        if (! isMySQL()) {
            return;
        }

       SQLExecutor.execute(dbconn, "DROP FUNCTION inventory_in_stock");
       SQLExecutor.execute(dbconn, "DROP FUNCTION inventory_held_by_customer");
        
       String sql =
            "DELIMITER $$\n" +
            "CREATE FUNCTION inventory_held_by_customer(p_inventory_id INT) RETURNS INT " +
            "READS SQL DATA " +
            "BEGIN " +
              "DECLARE v_customer_id INT; # Testing comment in this context\n" +
              "DECLARE EXIT HANDLER FOR NOT FOUND RETURN NULL; # Another comment\n" +
              "SELECT customer_id INTO v_customer_id " +
              "FROM rental " +
              "WHERE return_date IS NULL " +
              "AND inventory_id = p_inventory_id; " +
              "RETURN v_customer_id; " +
            "END $$ " +
            "DELIMITER ;\n" +

            "DELIMITER $$\n" +
            "CREATE FUNCTION inventory_in_stock(p_inventory_id INT) RETURNS BOOLEAN " +
            "READS SQL DATA " +
            "BEGIN " +
            "    DECLARE v_rentals INT; #Testing comment in this context\n" +
            "    DECLARE v_out     INT; #Another comment\n" +
            
            "    #AN ITEM IS IN-STOCK IF THERE ARE EITHER NO ROWS IN THE rental TABLE\n" +
            "    #FOR THE ITEM OR ALL ROWS HAVE return_date POPULATED\n" +
            "    SELECT COUNT(*) INTO v_rentals " +
            "    FROM rental " +
            "    WHERE inventory_id = p_inventory_id; " +
            "    IF v_rentals = 0 THEN " +
            "      RETURN TRUE; " +
            "    END IF; " +
            "    SELECT COUNT(rental_id) INTO v_out " +
            "    FROM inventory LEFT JOIN rental USING(inventory_id) " +
            "    WHERE inventory.inventory_id = p_inventory_id " +
            "    AND rental.return_date IS NULL; " +
            "    IF v_out > 0 THEN " +
            "      RETURN FALSE; " +
            "    ELSE " +
            "      RETURN TRUE; " +
            "    END IF; " +
            "END";

        checkExecution(SQLExecutor.execute(dbconn, sql));
    }

    public void testExecuteLogger() throws Exception {
        String tablename = getTestTableName();
        String sql = "INSERT INTO " + tablename + " values(1); " +
                "INSERT INTO " + tablename + " values(2); " +
                "INSERT INTO FOO values('this should fail'); " +
                "SELECT * FROM " + tablename + ";";

        TestLogger logger = new TestLogger();

        SQLExecutor.execute(dbconn, sql, logger);
        assertEquals(4, logger.statementCount);
        assertEquals(1, logger.errorCount);
        assertEquals(3, logger.errorStatement);
        assertTrue(logger.gotFinish);
        assertFalse(logger.gotCancel);
    }

    private static class TestLogger implements SQLExecuteLogger {
        public int statementCount = 0;
        public int errorCount = 0;
        public int errorStatement = 0;
        public boolean gotFinish = false;
        public boolean gotCancel = false;

        public void log(StatementExecutionInfo info) {
            statementCount++;
            if (info.hasExceptions()) {
                errorCount++;
                errorStatement = statementCount;
            }
        }

        public void finish(long executionTime) {
            gotFinish = true;
        }

        // I'm not sure how to trigger a cancel...
        public void cancel() {
            gotCancel = true;
        }
    }
    

}
