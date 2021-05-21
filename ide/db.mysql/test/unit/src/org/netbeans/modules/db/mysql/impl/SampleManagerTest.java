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

package org.netbeans.modules.db.mysql.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.SQLExecutor;
import org.netbeans.modules.db.mysql.spi.sample.SampleProvider;
import org.netbeans.modules.db.mysql.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author David Van Couvering
 */
public class SampleManagerTest extends TestBase {
    private static final String SAMPLE_NAME = "testSample";
    private static final String SAMPLE_NAME2 = "testSample2";
    private static final String BAD_SAMPLE_NAME = "badSample";
    private Collection<SampleProvider> providers;
    
    public SampleManagerTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DatabaseConnection dbconn = getDatabaseConnection(true);
        SQLExecutor.execute(dbconn, "DROP TABLE IF EXISTS triptype");
        SQLExecutor.execute(dbconn, "DROP TABLE IF EXISTS customer");
        setUpProviders();
    }

    @Test
    public void testIsSample() {
        assertTrue(SampleManager.isSample(SAMPLE_NAME));
        assertTrue(SampleManager.isSample(SAMPLE_NAME2));
        assertTrue(SampleManager.isSample(BAD_SAMPLE_NAME));
        assertFalse(SampleManager.isSample(SAMPLE_NAME+"nope"));
    }

    @Test
    public void testCreateSample() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);

        SampleManager.createSample(SAMPLE_NAME, dbconn);

        verifyTableExists("triptype");

        SampleManager.createSample(SAMPLE_NAME2, dbconn);

        verifyTableExists("customer");

        try {
            SampleManager.createSample(BAD_SAMPLE_NAME, dbconn);
            fail("Should have thrown an exception");
        } catch (DatabaseException dbe) {
            // expected
        }
        
        try {
            SampleManager.createSample("foo", dbconn);
            fail("Should have thrown an exception");
        } catch (DatabaseException dbe) {
            // expected
        }
    }

    @Test
    public void testGetSampleNames() {
        List<String> samples = SampleManager.getSampleNames();
        assertTrue(samples.contains(SAMPLE_NAME));
        assertTrue(samples.contains(SAMPLE_NAME2));
        assertTrue(samples.contains(BAD_SAMPLE_NAME));
        assertFalse(samples.contains("foo"));
    }

    @Test
    public void testAddSampleProvider() throws Exception {
        final String sampleName = "testAddSample";
        assertFalse(SampleManager.isSample(sampleName));
        assertFalse(SampleManager.getSampleNames().contains(sampleName));

        SampleProvider testProvider = new SampleProvider() {

            public void create(String sampleName, DatabaseConnection conn) throws DatabaseException {
                return;
            }

            public boolean supportsSample(String name) {
                if (name.equals(sampleName)) {
                    return true;
                }
                return false;
            }

            public List<String> getSampleNames() {
                List<String> samples = new ArrayList<String>();
                samples.add(sampleName);
                return samples;
            }
        };

        addSampleProvider("testProvider", testProvider);

        assertTrue(SampleManager.getSampleNames().contains(sampleName));
        assertTrue(SampleManager.isSample(sampleName));
    }

    private void setUpProviders() throws Exception {
        createProviderFiles(getSampleProviderFolder());
        providers = SampleProviderHelper.getProviders();
        assertNotNull(providers);
    }

    private void createProviderFiles(FileObject folder) throws Exception {
        addSampleProvider("TestSampleProvider", new TestSampleProvider());
        addSampleProvider("TestSampleProvider2", new TestSampleProvider2());
    }

    private void addSampleProvider(String providerName, SampleProvider provider) throws Exception {
       FileObject folder = getSampleProviderFolder();
       FileObject fo = FileUtil.createData(folder, providerName + ".instance");
       fo.setAttribute("instanceCreate", provider);
       fo.setAttribute("instanceOf", SampleProvider.class.getName());

    }
    private FileObject getSampleProviderFolder() throws Exception {
       return FileUtil.createFolder(FileUtil.getConfigRoot(), SampleProvider.SAMPLE_PROVIDER_PATH);
    }


    private void verifyTableExists(String tableName) throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);
        connect(dbconn);

        String sql = "SELECT * FROM " + tableName;
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        assertFalse(info.hasExceptions());
    }

    private static class TestSampleProvider implements SampleProvider {
        private static List<String> SAMPLE_NAMES = new ArrayList<String>();

        static {
            SAMPLE_NAMES.add(SAMPLE_NAME);
            SAMPLE_NAMES.add(BAD_SAMPLE_NAME);
        }

        public void create(String sampleName, DatabaseConnection dbconn) throws DatabaseException {
            if (sampleName.equals(SAMPLE_NAME)) {
                String sql = "DROP TABLE IF EXISTS triptype; ";
                SQLExecutor.execute(dbconn, sql);

                sql = "create table triptype ( " +
                      "triptypeid INTEGER NOT NULL, " +
                      "name VARCHAR(15), " +
                      "description VARCHAR(50), " +
                      "lastupdated TIMESTAMP" +
                    ");" +
                    " alter table triptype" +
                    " add constraint triptypePK" +
                    " PRIMARY KEY (triptypeid);" +
                    " insert into triptype values( 1, 'TRNG', 'Training', NULL);" +
                    " insert into triptype values( 2, 'SALES', 'Sales', NULL);";

                SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
                if(info.hasExceptions()) {
                    throw new DatabaseException(info.getExceptions().get(0));
                }
            } else {
                throw new DatabaseException("Baaad sample, no biscuit");
            }
        }

        public boolean supportsSample(String name) {
            return SAMPLE_NAMES.contains(name);
        }

        public List<String> getSampleNames() {
            return SAMPLE_NAMES;
        }

    }

    private static class TestSampleProvider2 implements SampleProvider {
        private static List<String> SAMPLE_NAMES = new ArrayList<String>();

        static {
            SAMPLE_NAMES.add(SAMPLE_NAME2);
        }

        public void create(String sampleName, DatabaseConnection dbconn) throws DatabaseException {
            if (! supportsSample(sampleName)) {
                throw new DatabaseException("Sample not supported");
            }
            
            SQLExecutor.execute(dbconn, "DROP TABLE IF EXISTS triptype;");
            
            String sql = "CREATE TABLE customer (" +
                   "customer_id INTEGER PRIMARY KEY NOT NULL," +
                   " discount_code CHARACTER(1) NOT NULL," +
                   " zip VARCHAR(10) NOT NULL," +
                   " name VARCHAR(30)," +
                   " addressline1 VARCHAR(30)," +
                   " addressline2 VARCHAR(30)," +
                   " city VARCHAR(25)," +
                   " state CHARACTER(2)," +
                   " phone CHARACTER(12)," +
                   " fax CHARACTER(12)," +
                   " email VARCHAR(40)," +
                   " credit_limit INTEGER ) ;" +
                " INSERT INTO customer" +
                " values(" +
                "1,'N','33015','JumboCom','111 E. Las Olas Blvd','Suite 51','Fort Lauderdale','FL','305-777-4632','305-777-4635','jumbocom@gmail.com',100000" +
                ");" +
                " INSERT INTO customer" +
                " values(" +
                "2,'M','33055','Livermore Enterprises','9754 Main Street','P.O. Box 567','Miami','FL','305-456-8888','305-456-8889','www.tsoftt.com',50000" +
                ");";
            SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
            if(info.hasExceptions()) {
                throw new DatabaseException(info.getExceptions().get(0));
            }

        }

        public boolean supportsSample(String name) {
            return SAMPLE_NAMES.contains(name);
        }

        public List<String> getSampleNames() {
            return SAMPLE_NAMES;
        }

    }
}
