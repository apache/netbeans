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

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Test that project association by module inclusion list works.
 * @author Jesse Glick
 */
public class UpdateTrackingFileOwnerQueryTest extends TestBase {

    public UpdateTrackingFileOwnerQueryTest(String name) {
        super(name);
    }

    /* XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testOwnershipNetBeansOrg() throws Exception {
        // Basic module:
        assertOwnership("o.apache.tools.ant.module", "nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-apache-tools-ant-module.jar");
        // Explicitly listed additions:
        assertOwnership("o.apache.tools.ant.module", "nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/ant/nblib/bridge.jar");
        // Pattern matches (here "ant/lib/"):
        assertTrue("ant module built (cannot scan by pattern unless files exist)", file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/ant/lib/ant.jar").isFile());
        assertOwnership("o.apache.tools.ant.module", "nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/ant/lib/ant.jar");
        // These two always included:
        assertOwnership("o.apache.tools.ant.module", "nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/config/Modules/org-apache-tools-ant-module.xml");
        assertOwnership("o.apache.tools.ant.module", "nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/update_tracking/org-apache-tools-ant-module.xml");
        // Different pattern match ("modules/ext/jh*.jar"):
        assertOwnership("javahelp", "nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/ext/jh-2.0_05.jar");
        // Use of release dir:
        assertOwnership("extbrowser", "nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/lib/extbrowser.dll");
    }
     */
    
    public void testOwnershipExternal() throws Exception {
        // Will not normally exist when test is run:
        assertOwnership(resolveEEPPath("/suite1/action-project"), resolveEEPPath("/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"));
        assertOwnership(resolveEEPPath("/suite1/action-project"), resolveEEPPath("/suite1/build/cluster/update_tracking/org-netbeans-examples-modules-action.xml"));
    }

    private void assertOwnership(String project, String file) throws Exception {
        FileObject projectFO = FileUtil.toFileObject(PropertyUtils.resolveFile(nbRootFile(), project));
        assertNotNull("have project " + project, projectFO);
        Project p = ProjectManager.getDefault().findProject(projectFO);
        assertNotNull("have a project in " + project, p);
        // This has the side effect of forcing a scan of the module universe:
        ClassPath.getClassPath(projectFO.getFileObject("src"), ClassPath.COMPILE);
        FileObject fileFO = FileUtil.toFileObject(PropertyUtils.resolveFile(nbRootFile(), file));
        if (fileFO != null) { // OK if not currently built
            assertEquals("correct owner by FileObject of " + file, p, FileOwnerQuery.getOwner(fileFO));
        }
        assertEquals("correct owner by URI of " + file, p, FileOwnerQuery.getOwner(
                Utilities.toURI(PropertyUtils.resolveFile(nbRootFile(), file))));
    }
    
}
