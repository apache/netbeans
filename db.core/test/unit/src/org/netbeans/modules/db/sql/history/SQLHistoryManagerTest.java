/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
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
