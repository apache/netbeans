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
package org.netbeans.modules.java.file.launcher.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sarvesh Kesharwani
 */
public class JavaFileTest extends NbTestCase {
    
    private static final Logger LOG = Logger.getLogger(JavaFileTest.class.getName());
    
    public JavaFileTest(String name) {
        super(name);
    }
    
    public void testSingleJavaSourceRun() throws Exception {
        clearWorkDir();
        File f1 = new File(getWorkDir(), "TestSingleJavaFile.java");
        FileWriter w = new FileWriter(f1);
        w.write("public class TestSingleJavaFile {\n" +
        "    \n" +
        "    public static void main (String args[]) {\n" +
        "        System.out.print(\"hello world\");\n" +
        "    }\n" +
        "    \n" +
        "}");
        w.close();
        FileObject javaFO = FileUtil.toFileObject(f1);
        assertNotNull("FileObject found: " + f1, javaFO);
        SingleJavaSourceRunActionProvider runActionProvider = new SingleJavaSourceRunActionProvider();
        LaunchProcess process = runActionProvider.invokeActionHelper(null, "run.single", javaFO, ExplicitProcessParameters.empty());
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.call().getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String result = builder.toString();
        assertEquals("hello world", result);
        FileObject[] siblings = javaFO.getParent().getChildren();
        if (isJDK11OrNewer()) {
            assertEquals("No other sibling", 1, siblings.length);
            assertEquals(javaFO, siblings[0]);
        } else {
            assertEquals("One other sibling created", 2, siblings.length);
            if (javaFO.equals(siblings[0])) {
                assertEquals("TestSingleJavaFile.class", siblings[1].getNameExt());
            } else {
                assertEquals("TestSingleJavaFile.class", siblings[0].getNameExt());
            }
        }
    }
    
    private boolean isJDK11OrNewer() {
        return SingleSourceFileUtil.findJavaVersion() >= 11;
    }
    
}
