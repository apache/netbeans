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

package org.netbeans.junit;


import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite.Configuration;
import test.pkg.not.in.junit.NbModuleSuiteClusterPath;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteClusterPathFinalTest extends NbTestCase {
    
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public NbModuleSuiteClusterPathFinalTest(String testName) {
        super(testName);
    }

    public void testClusterPathFinal() throws Exception {
        if (!NbModuleSuiteTest.isCluster("ide")) {
            // skip
            return;
        }
        LinkedList<File> clusters = new LinkedList<File>();
        NbModuleSuite.S.findClusters(clusters, Collections.singletonList("ide"));
        assertFalse("Something found", clusters.isEmpty());
        assertEquals("One element found", 1, clusters.size());
        final File ideCluster = clusters.get(0);
        System.setProperty("cluster.path.final", ideCluster.getPath() + ":" + new File(ideCluster.getParent(), "nonexistent"));
        Configuration conf = NbModuleSuite.createConfiguration(NbModuleSuiteClusterPath.class).gui(false).clusters(".*");
        Test test = conf.suite();
        test.run(new TestResult());
        String val = System.getProperty("my.clusters");
        assertNotNull("The test was running", clusters);
        assertNotNull("Value has been set", val);
        assertTrue("ide cluster shall be included: " + val, val.contains(ideCluster.getPath()));
        assertFalse("no java cluster shall be included: " + val, val.matches(".*java[:;].*"));
        assertFalse("no apisupport cluster shall be included: " + val, val.matches(".*apisupport[:;].*"));
        assertFalse("no ergonomics cluster shall be included: " + val, val.matches(".*ergonomics[:;].*"));
    }
}
