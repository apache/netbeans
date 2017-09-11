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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
