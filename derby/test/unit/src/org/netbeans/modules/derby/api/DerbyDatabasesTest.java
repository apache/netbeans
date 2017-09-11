/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.derby.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.ws.Holder;
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

                    assertEquals(3, sampleDBFiles.size());
                    assertTrue(sampleDBFiles.contains("log"));
                    assertTrue(sampleDBFiles.contains("seg0"));
                    assertTrue(sampleDBFiles.contains("service.properties"));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void testDatabaseNotExtractedToExistingDirectoryIssue80122() {
        final Holder<Boolean> exceptionHappend = new Holder<>(false);
        
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
                    exceptionHappend.value = true;
                }
            }
        });
        
        assertTrue("Extracting sample db was interrupted", exceptionHappend.value);
    }

    public void testDatabaseNotExtractedIfDBExists() {
        final Holder<Boolean> exceptionHappend = new Holder<>(false);
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample1", true);
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample1", true);
                } catch (IOException ex) {
                    exceptionHappend.value = true;
                }
            }
        });
        
        assertTrue("Extracting sample db was interrupted", exceptionHappend.value);
    }
    
    public void testDatabaseSilentlyNotExtractedIfExists() {
        final Holder<Boolean> exceptionHappend = new Holder<>(false);
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {

            @Override
            public void run() {
                try {
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample2", false);
                    DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample2", false);
                } catch (IOException ex) {
                    exceptionHappend.value = true;
                }
            }
        });
        
        assertFalse("Extracting sample db was not interrupted", exceptionHappend.value);
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
