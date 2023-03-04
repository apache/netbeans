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
package org.netbeans.modules.derby.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DerbyDatabasesImpl;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 */
public class DerbyDatabasesTest extends TestBase {

    private File systemHome;

    public DerbyDatabasesTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        clearWorkDir();

        systemHome = new File(getWorkDir(), ".netbeans-derby");
        systemHome.mkdirs();
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {
            @Override
            public void run() {
                DerbyOptions.getDefault().setSystemHome(systemHome.getAbsolutePath());
                try {
                    JDBCDriverManager.getDefault().addDriver(JDBCDriver.create(DerbyOptions.DRIVER_DISP_NAME_NET, DerbyOptions.DRIVER_DISP_NAME_NET, DerbyOptions.DRIVER_CLASS_NET, new URL[] {}));
                } catch (DatabaseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        
    }

    public void testGetFirstFreeDatabaseName() throws Exception {
        assertEquals("testdb", DerbyDatabases.getFirstFreeDatabaseName("testdb"));

        new File(systemHome, "testdb").createNewFile();

        assertEquals("testdb1", DerbyDatabases.getFirstFreeDatabaseName("testdb"));

        new File(systemHome, "testdb1").createNewFile();

        assertEquals("testdb2", DerbyDatabases.getFirstFreeDatabaseName("testdb"));
    }

    public void testDatabaseExists() throws Exception {
        assertFalse(DerbyDatabases.databaseExists(""));
        assertFalse(DerbyDatabases.databaseExists("testdb"));

        new File(systemHome, "testdb").createNewFile();

        assertTrue(DerbyDatabases.databaseExists("testdb"));
    }

    public void testGetFirstIllegalCharacter() throws Exception {
        assertEquals((int) File.separatorChar, DerbyDatabases.getFirstIllegalCharacter("a"
                + File.separatorChar + "b"));
        assertEquals((int) '/', DerbyDatabases.getFirstIllegalCharacter("a/b"));
    }

    public void testExtractSampleDatabase() throws Exception {
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("newdb", false);
                    File newDBDir = new File(systemHome, "newdb");
                    Set sampleDBFiles = new HashSet(Arrays.asList(newDBDir.list()));

                    assertEquals(4, sampleDBFiles.size());
                    assertTrue(sampleDBFiles.contains("log"));
                    assertTrue(sampleDBFiles.contains("seg0"));
                    assertTrue(sampleDBFiles.contains("service.properties"));
                    assertTrue(sampleDBFiles.contains("README_DO_NOT_TOUCH_FILES.txt"));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void testDatabaseNotExtractedToExistingDirectoryIssue80122() {
        final boolean[] exceptionHappend = new boolean[1];
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    File sampleDir = new File(systemHome, "sample");
                    sampleDir.mkdirs();
                    
                    FileUtil.toFileObject(sampleDir).createData("test.file");

                    assertEquals("There should be no files in the sample directory", 1, sampleDir.listFiles().length);

                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample", false);
                } catch (IOException ex) {
                    exceptionHappend[0] = true;
                }
            }
        });
        
        assertTrue("Extracting sample db was interrupted", exceptionHappend[0]);
    }

    public void testDatabaseNotExtractedIfDBExists() {
        final boolean[] exceptionHappend = new boolean[1];
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample1", true);
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample1", true);
                } catch (IOException ex) {
                    exceptionHappend[0] = true;
                }
            }
        });
        
        assertTrue("Extracting sample db was interrupted", exceptionHappend[0]);
    }
    
    public void testDatabaseSilentlyNotExtractedIfExists() {
        final boolean[] exceptionHappend = new boolean[1];
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample2", false);
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample2", false);
                } catch (IOException ex) {
                    exceptionHappend[0] = true;
                }
            }
        });
        
        assertFalse("Extracting sample db was not interrupted", exceptionHappend[0]);
    }
    
    public static final class SampleDatabaseLocator extends InstalledFileLocator {

        public final File directory;

        public SampleDatabaseLocator() {
            File derbyModule = new File(URI.create(DerbyOptions.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
            directory = derbyModule.getParentFile().getParentFile();
        }

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if ("modules/ext/derbysampledb.zip".equals(relativePath)) {
                return new File(directory, relativePath);
            }
            return null;
        }
    }
}
