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

package org.netbeans.modules.maven.runjar;

import java.io.StringReader;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class RunJarPrereqCheckerTest extends NbTestCase {

    public RunJarPrereqCheckerTest(String n) {
        super(n);
    }
    
    private FileObject d;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testWriteMapping() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>testgrp</groupId>\n" +
                "    <artifactId>testart</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        Project p = ProjectManager.getDefault().findProject(d);
        RunJarPrereqChecker.writeMapping("run", p, "my.App");
        TestFileUtils.touch(d.getFileObject("nbactions.xml"), null);
        M2ConfigProvider usr = p.getLookup().lookup(M2ConfigProvider.class);
        ActionToGoalMapping mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getDefaultConfig().getRawMappingsAsString()));
        assertEquals(1, mapping.getActions().size());
        RunJarPrereqChecker.writeMapping("run", p, "another.App");
        TestFileUtils.touch(d.getFileObject("nbactions.xml"), null);
        mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getDefaultConfig().getRawMappingsAsString()));
        assertEquals(1, mapping.getActions().size());
    }

}
