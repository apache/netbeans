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
package org.netbeans.modules.java.api.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.netbeans.modules.java.api.common.util.RunProcess;

/**
 *
 * @author Sarvesh Kesharwani
 */
public class TestJavaFile extends NbTestCase {
    
    private static final Logger LOG = Logger.getLogger(TestJavaFile.class.getName());
    
    public TestJavaFile(String name) {
        super(name);
    }
    
    public void testSingleJavaSourceRun() {
        try {
            
            File f1 = new File(new File(new File (getDataDir().getAbsolutePath()), "files"), "TestSingleJavaFile.java");
            FileObject javaFO = FileUtil.toFileObject(f1);
            RunProcess process = new SingleJavaSourceRunActionProvider().invokeActionHelper("run.single", javaFO);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString();
            assertEquals("hello world", result);
        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Could not read output from running Single Java file"); //NOI18N
        }
    }
    
}
