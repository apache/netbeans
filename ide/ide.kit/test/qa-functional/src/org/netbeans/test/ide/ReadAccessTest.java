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

package org.netbeans.test.ide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.Stamps;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Read access test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * This test starts the VM three times. During first start (with new userdir)
 * it generates caches (classes, resources, etc.). The next two starts are
 * then verifying that no JAR file is opened (e.g. the caches functioning
 * correctly).
 *
 * @author mrkam@netbeans.org, Jaroslav Tulach
 */
public class ReadAccessTest extends NbTestCase {
    private static void initCheckReadAccess() throws Exception {
        Thread.sleep(10000);
        Stamps.getModulesJARs().shutdown();

        System.getProperties().remove("netbeans.dirs");

        Set<String> allowedFiles = new HashSet<String>();
        InputStream is = ReadAccessTest.class.getResourceAsStream("allowed-file-reads.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            allowedFiles.add(line);
        }
        CountingSecurityManager.initialize(null, CountingSecurityManager.Mode.CHECK_READ, allowedFiles);
    }
    
    public ReadAccessTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        CountingSecurityManager.initialize("none", CountingSecurityManager.Mode.CHECK_READ, null);
        System.setProperty(NbModuleSuite.class.getName() + ".level", "FINEST");

        NbTestSuite suite = new NbTestSuite();
        {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                ReadAccessTest.class
            ).clusters(".*").enableModules(".*").honorAutoloadEager(true)
            .reuseUserDir(false).enableClasspathModules(false);
            conf = conf.addTest("testInitUserDir");
            suite.addTest(conf.suite());
        }

        {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                ReadAccessTest.class
            ).clusters(".*").enableModules(".*").honorAutoloadEager(true)
            .reuseUserDir(true).enableClasspathModules(false);
            conf = conf.addTest("testSndStart");
            suite.addTest(conf.suite());
        }

        {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                ReadAccessTest.class
            ).clusters(".*").enableModules(".*").honorAutoloadEager(true)
            .reuseUserDir(true).enableClasspathModules(false);
            conf = conf.addTest("testThirdStart");
            suite.addTest(conf.suite());
        }

        return suite;
    }

    public void testInitUserDir() throws Exception {
        // initializes counting, but waits till netbeans.dirs are provided
        // by NbModuleSuite
        initCheckReadAccess();
    }

    public void testSndStart() throws Exception {
        assertAccess();
        
        // initializes counting, but waits till netbeans.dirs are provided
        // by NbModuleSuite
        initCheckReadAccess();
    }

    public void testThirdStart() throws Exception {
        assertAccess();
    }

    private void assertAccess() throws Exception {
        try {
            assertTrue("Security manager is on", CountingSecurityManager.isEnabled());
            CountingSecurityManager.assertCounts("No reads during 2nd startup", 0);
        } catch (Error e) {
            e.printStackTrace(getLog("file-reads-report.txt"));
            throw e;
        }
    }
}
