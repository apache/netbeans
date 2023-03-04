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

package org.netbeans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsNoFallbackTest extends NbTestCase {

    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private String branding;
    private Locale locale;
    
    
    public StampsNoFallbackTest(String testName) {
        super(testName);
    }            
    
    
    @Override
    protected void setUp() throws Exception {
        branding = NbBundle.getBranding();
        locale = Locale.getDefault();
        
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        createModule("org.openide.awt", platform, 50000L);
        createModule("org.openide.nodes", platform, 60000L);
        createModule("org.netbeans.api.languages", ide, 90000L);
        createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Thread.sleep(100);
        
        createPopulateZip();

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }


    public void testPopulateTheCacheDirectory() throws Exception {
        System.setProperty("netbeans.fallback.cache", "none");
        
        InputStream is = Stamps.getModulesJARs().asStream("my/dir/file");
        assertNull("Cache not found", is);
    }

    static void createModule(String cnb, File cluster, long accesTime) throws IOException {
        StampsTest.createModule(cnb, cluster, accesTime);
    }

    private void createPopulateZip() throws IOException {
        File cache = new File(new File(new File(ide, "var"), "cache"), "populate.zip");
        cache.getParentFile().mkdirs();
        JarOutputStream os = new JarOutputStream(new FileOutputStream(cache));
        os.putNextEntry(new ZipEntry("my/dir/file"));
        os.write("Ahoj".getBytes());
        os.closeEntry();
        os.close();
    }
}
