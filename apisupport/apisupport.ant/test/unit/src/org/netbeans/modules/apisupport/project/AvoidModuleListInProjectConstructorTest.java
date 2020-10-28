/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.openide.filesystems.FileObject;

/**
 * Verify that loading modules does not automatically do a module list scan.
 * @author Jesse Glick
 * @see "issue #59550"
 */
public class AvoidModuleListInProjectConstructorTest extends TestBase {

    public AvoidModuleListInProjectConstructorTest(String name) {
        super(name);
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testNetBeansOrgModules() throws Exception {
        assertEquals("no scans of netbeans.org initially", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/extide/org-apache-tools-ant-module.jar")).size());
        FileObject fo = nbRoot().getFileObject("extide/o.apache.tools.ant.module");
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull(p);
        assertEquals("still no scans", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + "extide" + "/modules/org-apache-tools-ant-module.jar")).size());
        assertEquals("org.apache.tools.ant.module", ProjectUtils.getInformation(p).getName());
        assertEquals("still no scans", 0, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + "extide" + "/modules/org-apache-tools-ant-module.jar")).size());
        ClassPath.getClassPath(fo.getFileObject("src"), ClassPath.COMPILE);
        assertEquals("now have scanned something", 1, ModuleList.getKnownEntries(file("nbbuild/netbeans/" + "extide" + "/modules/org-apache-tools-ant-module.jar")).size());
    }
    
}
