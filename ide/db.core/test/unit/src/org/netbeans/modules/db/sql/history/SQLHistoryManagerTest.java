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
package org.netbeans.modules.db.sql.history;

import java.io.IOException;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;

/**
 * A Test based on NbTestCase. It is a NetBeans extension to JUnit TestCase
 * which among othres allows to compare files via assertFile methods, create
 * working directories for testcases, write to log files, compare log files
 * against reference (golden) files, etc.
 * 
 * More details here http://xtest.netbeans.org/NbJUnit/NbJUnit-overview.html.
 * 
 * @author John Baker
 */
public class SQLHistoryManagerTest extends NbTestCase {

    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    public static final String SQL_HISTORY_FILE_NAME = "sql_history.xml";  // NOI18N

    /** Default constructor.
     * @param testName name of particular test case
     */
    public SQLHistoryManagerTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SQLHistoryManagerTest("testUpdateListRemoveEqualNumber"));
        suite.addTest(new SQLHistoryManagerTest("testUpdateListRemoveLessNumber3"));
        suite.addTest(new SQLHistoryManagerTest("testUpdateListRemoveLessNumber1"));
        suite.addTest(new SQLHistoryManagerTest("testUpdateListRemoveAll"));


        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    // run only selected test case
    //junit.textui.TestRunner.run(new SQLHistoryPersistentManagerTest("test1"));
    }

    /** Called before every test case. */
    @Override
    public void setUp() {       
    }

    /** Called after every test case. */
    @Override
    public void tearDown() {
        try {
            clearWorkDir();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testUpdateListRemoveEqualNumber() {
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// derby", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().setListSize(2);
        assertEquals(2, SQLHistoryManager.getInstance().getSQLHistory().size());
    }
    
    public void testUpdateListRemoveLessNumber3() {
        SQLHistoryManager.getInstance().setListSize(100);
        SQLHistoryManager.getInstance().getSQLHistory().clear();
            // Create a list of SQL statements
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// derby", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres", "select * from TRAVEL.TRIPTYPE", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres", "select * from TRAVEL.PERSON", new Date()));
        assertEquals(4, SQLHistoryManager.getInstance().getSQLHistory().size());
        SQLHistoryManager.getInstance().setListSize(3);
        assertEquals(3, SQLHistoryManager.getInstance().getSQLHistory().size());
    }
    
    public void testUpdateListRemoveLessNumber1() {
        SQLHistoryManager.getInstance().setListSize(100);
        SQLHistoryManager.getInstance().getSQLHistory().clear();
            // Create a list of SQL statements
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// derby", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres1", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres2", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres3", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres4", "select * from TRAVEL.TRIP", new Date()));
        assertEquals(5, SQLHistoryManager.getInstance().getSQLHistory().size());
        SQLHistoryManager.getInstance().setListSize(1);
            assertEquals(1, SQLHistoryManager.getInstance().getSQLHistory().size());      
    }
    
      public void testUpdateListRemoveAll() {
        SQLHistoryManager.getInstance().setListSize(100);
        SQLHistoryManager.getInstance().getSQLHistory().clear();
            // Create a list of SQL statements
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// derby", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres1", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres2", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres3", "select * from TRAVEL.TRIP", new Date()));
        SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry("jdbc:// postgres4", "select * from TRAVEL.TRIP", new Date()));
        assertEquals(5, SQLHistoryManager.getInstance().getSQLHistory().size());
        SQLHistoryManager.getInstance().setListSize(0);
        assertEquals(0, SQLHistoryManager.getInstance().getSQLHistory().size());
    }
}
