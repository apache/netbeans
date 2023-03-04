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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.test.MockLookup;

public class ClusterUtilsTest extends NbTestCase {

    public ClusterUtilsTest(String n) {
        super(n);
    }

    public void testEvaluateClusterPath() throws Exception {
        clearWorkDir();
        TestBase.initializeBuildProperties(getWorkDir(), null);
        MockLookup.setLayersAndInstances();
        SuiteProject p = TestBase.generateSuite(getWorkDir(), "suite");
        File externalPlatform = new File(getWorkDir(), "extplaf");
        TestBase.makePlatform(externalPlatform);
        File externalCluster = new File(externalPlatform, "platform");
        File nestedPlatform = new File(getWorkDir(), "suite/plaf");
        TestBase.makePlatform(nestedPlatform);
        File plafCluster = new File(nestedPlatform, "more");
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("OpenIDE-Module", "x1");
        TestBase.createJar(new File(plafCluster, "modules/x1.jar"), Collections.<String,String>emptyMap(), mf);
        // #180475: just because suite "owns" it does not mean that this is actually from that suite.
        File nestedCluster = new File(getWorkDir(), "suite/extra");
        mf = new Manifest();
        mf.getMainAttributes().putValue("OpenIDE-Module", "x2");
        TestBase.createJar(new File(nestedCluster, "modules/x2.jar"), Collections.<String,String>emptyMap(), mf);
        SuiteProject p2 = TestBase.generateSuite(getWorkDir(), "suite2");
        File chainedCluster = p2.getHelper().resolveFile("build/cluster");
        NbModuleProject standaloneModule = TestBase.generateStandaloneModule(getWorkDir(), "extmod");
        File moduleCluster = new File(getWorkDir(), "extmod/build/cluster");
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.fixedPropertyProvider(Collections.singletonMap("cluster.path",
                "" + externalCluster +
                 ":" + plafCluster +
                 ":" + nestedCluster +
                 ":" + chainedCluster +
                 ":" + moduleCluster)));
        Set<ClusterInfo> actual = ClusterUtils.evaluateClusterPath(new File(getWorkDir(), "suite"), eval, null);
        assertEquals(new LinkedHashSet<ClusterInfo>(Arrays.asList(
                ClusterInfo.createFromCP(externalCluster, null, false, null, null, true),
                ClusterInfo.createFromCP(plafCluster, null, false, null, null, true),
                ClusterInfo.createFromCP(nestedCluster, null, false, null, null, true),
                ClusterInfo.createFromCP(chainedCluster, p2, false, null, null, true),
                ClusterInfo.createFromCP(moduleCluster, standaloneModule, false, null, null, true)
                )).toString(), actual.toString());
    }

}
