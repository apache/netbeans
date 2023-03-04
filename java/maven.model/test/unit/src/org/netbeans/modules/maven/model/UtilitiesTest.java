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

package org.netbeans.modules.maven.model;

import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class UtilitiesTest extends NbTestCase {

    public UtilitiesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    protected @Override String logRoot() {
        return Utilities.class.getName();
    }

    public void testPerformPOMModelOperations() throws Exception {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                model.getProject().addModule("child1");
                model.getProject().addModule("child2");
            }
        }));
        assertEquals("<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "    <modules>\n" +
                "        <module>child1</module>\n" +
                "        <module>child2</module>\n" +
                "    </modules>\n" +
                "</project>\n",
                pom.asText().replace("\r\n", "\n"));
    }

    public void testPerformNothing() throws Exception {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        CharSequence log = Log.enable(logRoot(), Level.FINE);
        Utilities.performPOMModelOperations(pom, Collections.<ModelOperation<POMModel>>emptyList());
        assertFalse(log.toString(), log.toString().contains("changes in"));
    }

}
