/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
