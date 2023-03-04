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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author John Baker, Jiri Skrivanek
 */
public class SQLHistoryPersistenceManagerTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public SQLHistoryPersistenceManagerTest(String testName) {
        super(testName);
    }

    /** Called after every test case. */
    @Override
    public void tearDown() throws IOException {
        clearWorkDir();
    }
    
    /** Test testExecuteStatements passes if no exceptions occur. */
    public void testExecuteStatements() throws Exception {
        class TestSQLHistoryManager extends SQLHistoryManager {

            @Override
            protected FileObject getConfigRoot() {
                try {
                    return FileUtil.toFileObject(getWorkDir());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected String getRelativeHistoryPath() {
                return "";
            }
        };
        
        Date refDate = new Date();
        SQLHistoryManager testableManager = new TestSQLHistoryManager();
        // History does not yet exists as file
        assertNull(testableManager.getHistoryRoot(false));
        testableManager.getSQLHistory().add(new SQLHistoryEntry("jdbc:// mysql", "select * from TRAVEL.PERSON", refDate));
        // History does not yet exists as file
        testableManager.save();
        assertNull(testableManager.getHistoryRoot(false));
        testableManager.getSQLHistory().add(new SQLHistoryEntry("jdbc:// oracle", "select * from PERSON", refDate));
        final Semaphore s = new Semaphore(0);
        PropertyChangeListener releasingListener =
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (SQLHistoryManager.PROP_SAVED.equals(
                                evt.getPropertyName())) {
                            s.release(); //release semaphore when data are saved
                        }
                    }
                };
        testableManager.addPropertyChangeListener(releasingListener);
        testableManager.save();
        // History does not yet exists as file
        assertNull(testableManager.getHistoryRoot(false));
        // Enforce writing of history
        s.tryAcquire(6, TimeUnit.SECONDS);
        testableManager.removePropertyChangeListener(releasingListener);
        // History file need to exist now!
        assertNotNull(testableManager.getHistoryRoot(false));
        assertTrue(testableManager.getHistoryRoot(false).isData());
        
        // Create a second SQLHistoryManager and ensure the content survived
        // the serialization/deserialization cycle
        SQLHistoryManager testableManager2 = new TestSQLHistoryManager();
        assertEquals(2, testableManager2.getSQLHistory().size());
        List<String> expectedURLs = new ArrayList<>();
        expectedURLs.add("jdbc:// mysql");
        expectedURLs.add("jdbc:// oracle");
        List<String> expectedSQLs = new ArrayList<>();
        expectedSQLs.add("select * from TRAVEL.PERSON");
        expectedSQLs.add("select * from PERSON");
        for(SQLHistoryEntry she: testableManager2.getSQLHistory()) {
            expectedSQLs.remove(she.getSql());
            expectedURLs.remove(she.getUrl());
            assertEquals(refDate, she.getDate());
        }
        assertTrue(expectedSQLs.isEmpty());
        assertTrue(expectedURLs.isEmpty());
    }

    /** Tests parsing of date format. */
    public void testDateParsing() throws Exception {
        final URL u = this.getClass().getResource("sql_history.xml");
        final FileObject fo = FileUtil.toFileObject(new File(u.toURI()));
        SQLHistoryManager testableManager = new SQLHistoryManager() {
            @Override
            protected FileObject getHistoryRoot(boolean create) throws IOException {
                return fo;
            }
        };
        
        List<SQLHistoryEntry> sqlHistoryList = new ArrayList<SQLHistoryEntry>(testableManager.getSQLHistory());
        for (SQLHistoryEntry sqlHistory : sqlHistoryList) {
            assertNotNull(sqlHistory.getDate());
        }
    }
}
