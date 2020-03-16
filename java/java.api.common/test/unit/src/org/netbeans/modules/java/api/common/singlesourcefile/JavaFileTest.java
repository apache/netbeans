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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Sarvesh Kesharwani
 */
public class JavaFileTest extends NbTestCase {
    
    private static final Logger LOG = Logger.getLogger(JavaFileTest.class.getName());
    
    public JavaFileTest(String name) {
        super(name);
    }
    
    public void testSingleJavaSourceRun() throws IOException {
        File f1 = new File(new File(new File(getDataDir().getAbsolutePath()), "files"), "TestSingleJavaFile.java");
        FileObject javaFO = FileUtil.toFileObject(f1);
        SingleJavaSourceRunActionProvider runActionProvider = new SingleJavaSourceRunActionProvider();
        if (!isJDK11OrNewer()) {
            assertFalse("The action is only enabled on JDK11 and newer", runActionProvider.isActionEnabled("run.single", Lookup.EMPTY));
            return;
        }
        RunProcess process = runActionProvider.invokeActionHelper("run.single", javaFO);
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String result = builder.toString();
        assertEquals("hello world", result);
    }
    
    private boolean isJDK11OrNewer() {
        String javaVersion = System.getProperty("java.specification.version");
        if (javaVersion.startsWith("1.")) {
            javaVersion = javaVersion.substring(2);
        }
        int version = Integer.parseInt(javaVersion);
        return version >= 11;
    }
    
}
