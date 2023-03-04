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

package org.netbeans.modules.maven.model.pom.impl;

import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Resource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ResourceImplTest extends NbTestCase {
    
    public ResourceImplTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testIncludes() throws Exception { // #198361
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                Resource res = model.getFactory().createResource();
                res.setTargetPath("META-INF"); //NOI18N
                res.setDirectory("src"); //NOI18N
                res.addInclude("stuff/"); //NOI18N
                Build build = model.getFactory().createBuild();
                build.addResource(res);
                model.getProject().setBuild(build);
            }
        }));
        assertEquals("<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "    <build>\n" +
                "        <resources>\n" +
                "            <resource>\n" +
                "                <targetPath>META-INF</targetPath>\n" +
                "                <directory>src</directory>\n" +
                "                <includes>\n" +
                "                    <include>stuff/</include>\n" +
                "                </includes>\n" +
                "            </resource>\n" +
                "        </resources>\n" +
                "    </build>\n" +
                "</project>\n",
                pom.asText().replace("\r\n", "\n"));
    }

}
