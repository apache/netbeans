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
