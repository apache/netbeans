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
package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author abadea
 */
public class DerbyOptionsTest extends TestBase {

    private File userdir;
    private File externalDerby;
    
    public DerbyOptionsTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        userdir = new File(getWorkDir(), ".netbeans");
        userdir.mkdirs();
        
        // create a fake installation of an external derby database
        externalDerby = new File(userdir, "derby");
        createFakeDerbyInstallation(externalDerby);
    }

    public void testDerbyLocationIsNullWhenBundledDerbyNotInstalled() throws IOException {
        // assert the bundled derby is installed
        Lookups.executeWith(Lookups.singleton(new InstalledFileLocatorImpl(userdir)),
                new Runnable() {

                    @Override
                    public void run() {
                        final File bundledDerby = new File(userdir, DerbyOptions.INST_DIR);
                        if (bundledDerby.exists()) {
                            try {
                                FileUtil.toFileObject(bundledDerby).delete();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }

                        assertNull(DerbyOptions.getDefaultInstallLocation());

                        DerbyOptions.getDefault().setLocation(externalDerby.getAbsolutePath());
                        assertFalse(DerbyOptions.getDefault().isLocationNull());

                        DerbyOptions.getDefault().setLocation("");
                        assertTrue(DerbyOptions.getDefault().isLocationNull());
                    }
                });
    }
    
    public void testLocationWhenNDSHPropertySetIssue76908() throws IOException {
        DerbyOptions.getDefault().setSystemHome(null);
        
        assertEquals("", DerbyOptions.getDefault().getSystemHome());

        File ndshSystemHome = new File(getWorkDir(), ".netbeans-derby-ndsh");
        if (!ndshSystemHome.mkdirs()) {
            throw new IOException("Could not create " + ndshSystemHome);
        }
        File systemHome = new File(getWorkDir(), ".netbeans-derby");
        if (!systemHome.mkdirs()) {
            throw new IOException("Could not create " + systemHome);
        }

        // returning the value of the netbeans.derby.system.home property when systemHome is not set...
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, ndshSystemHome.getAbsolutePath());
        assertEquals(ndshSystemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());

        // ... but returning systemHome when it is set
        DerbyOptions.getDefault().setSystemHome(systemHome.getAbsolutePath());
        assertEquals(systemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());
    }
    
    public static final class InstalledFileLocatorImpl extends InstalledFileLocator {
        
        private final File userdir;
        
        public InstalledFileLocatorImpl(File userdir) {
            this.userdir = userdir;
        }
        
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            File f = new File(userdir, relativePath);
            return f.exists() ? f : null;
        }
    }
}
