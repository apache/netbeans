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

package org.netbeans.modules.web.freeform;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Base class for web module project tests.
 * @author Pavel Buzek
 */
abstract class TestBaseWeb extends NbTestCase {

    static {
        TestBaseWeb.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected TestBaseWeb (String name) {
        super(name);
    }
    
    protected File egdir;
    protected FileObject buildProperties;
    protected FreeformProject jakarta;
    protected FileObject helloWorldServlet;
    protected FileObject helloWorldJsp;
    protected FileObject jakartaIndex;
    
    protected void setUp() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        super.setUp();
        egdir = FileUtil.normalizeFile(new File(getDataDir(), "example-projects"));
        assertTrue("example dir " + egdir + " exists", egdir.exists());
        FileObject jakartaDir = FileUtil.toFileObject(egdir).getFileObject("web_jakarta");
        assertNotNull("found projdir", jakartaDir);
        Project _jakarta = ProjectManager.getDefault().findProject(jakartaDir);
        assertNotNull("have a project", _jakarta);
        jakarta = (FreeformProject)_jakarta;
        helloWorldServlet = jakartaDir.getFileObject("src/mypackage/HelloWorld.java");
        assertNotNull("found HelloWorld.java", helloWorldServlet);
        helloWorldJsp = jakartaDir.getFileObject("web/hello.jsp");
        assertNotNull("found hello.jsp", helloWorldJsp);
        jakartaIndex = jakartaDir.getFileObject("web/index.html");
        assertNotNull("found index.html", jakartaIndex);
        buildProperties = jakartaDir.getFileObject("build.properties");
        assertNotNull("found build.properties", buildProperties);
        
    }
    
}
