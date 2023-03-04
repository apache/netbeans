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
package org.netbeans.modules.autoupdate.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class ListModulesTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger("TEST-" + ListModulesTest.class.getName());
    
    public ListModulesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        System.setProperty("netbeans.user", getWorkDirPath() + File.separator + "userdir");

        // initialize whole infra
        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testAModuleIsPrinted() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        CommandLine.getDefault().process(new String[] { "--modules",  "--list" }, System.in, os, err, new File("."));

        assertEquals("No error", 0, err.size());

        if (os.toString().indexOf("org.netbeans.bootstrap") < 0) {
            fail("We want default module to be found: " + os.toString());
        }
        if (os.toString().indexOf("org.my.module") != -1) {
            fail("module not found yet: " + os.toString());
        }
    }

    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f;
        int i = 0;
        for (;;) {
            f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) break;
        }
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
}
