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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tools.ant.BuildFileRule;
import org.junit.Rule;

/**
 *
 * @author pzajac
 */
public class TestDepsTest extends TestBase {
    
    @Rule
    public final BuildFileRule buildRule = new BuildFileRule();
    
    public TestDepsTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty("project.file");
        ModuleListParserTest.deleteCaches();
    }
    
    public void testDepsTest () throws Exception {
      // create test
      File projectxml = extractFile(TestDepsTest.class.getResourceAsStream("TestDepsProject.xml"),"project.xml");  
      System.setProperty("project.file", projectxml.getAbsolutePath());
      buildRule.configureProject(getBuildFileInClassPath("TestDeps.xml"));
      buildRule.executeTarget("test-deps");
    }
    
    public void testDepsNoTestDeps() throws Exception {
      File projectxml = extractFile(TestDepsTest.class.getResourceAsStream("TestDepsProjectNoTestDeps.xml"),"project.xml");
      System.setProperty("project.file", projectxml.getAbsolutePath());
      buildRule.configureProject(getBuildFileInClassPath("TestDeps.xml"));
      buildRule.executeTarget("test-deps-no-test-deps");
    }
    
  public void testMisingModuleEntryTestDeps() throws Exception {
      File projectxml = extractFile(TestDepsTest.class.getResourceAsStream("TestDepsMissingModuleEntry.xml"),"project.xml");
      System.setProperty("project.file", projectxml.getAbsolutePath());
      buildRule.configureProject(getBuildFileInClassPath("TestDeps.xml"));
      buildRule.executeTarget("test-deps-missing-module-entry-test-deps");
    }

    private File extractFile(InputStream is, String fileName) throws IOException {
        File f = new File(getWorkDir(),fileName);
        byte bytes[] = new byte[50000];
        try (FileOutputStream fos = new FileOutputStream(f)) {
            int len = is.read(bytes);
            fos.write(bytes,0,len);
        }
        return f;
    }
    
    
}
