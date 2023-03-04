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

package org.netbeans.modules.project.ant.ui;

import org.netbeans.modules.project.ant.ui.VariablesModel;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.ui.VariablesModel.Variable;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 */
public class VariablesModelTest extends NbTestCase {
    
    public VariablesModelTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getVariables method, of class VariablesModel.
     */
    public void testBasicFunctionality() throws IOException {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        VariablesModel model = new VariablesModel();
        assertEquals(0, model.getVariables().size());
        int baseCount = PropertyUtils.getGlobalProperties().entrySet().size();
        File var1 = new File(getWorkDir(), "var1_root");
        var1.mkdir();
        File var2 = new File(getWorkDir(), "var2_root");
        var2.mkdir();
        model.add("TEST_BASE_1", var1);
        model.add("TEST_BASE_2", var2);
        assertEquals(new Variable("TEST_BASE_1", var1), model.find("TEST_BASE_1"));
        assertEquals(new Variable("TEST_BASE_2", var2), model.find("TEST_BASE_2"));
        assertEquals("TEST_BASE_1/", model.getRelativePath(var1, true));
        assertEquals("${var.TEST_BASE_2}/", model.getRelativePath(var2, false));
        assertEquals(2, model.getVariables().size());
        model.save();
        assertEquals(baseCount+2, PropertyUtils.getGlobalProperties().entrySet().size());
        model.remove(new Variable("TEST_BASE_2", var2));
        assertEquals(1, model.getVariables().size());
        model.save();
        assertEquals(baseCount+1, PropertyUtils.getGlobalProperties().entrySet().size());
        File f = new File(var1, "folder");
        f.mkdir();
        File f2 = new File(f, "folder2");
        f2.mkdir();
        assertEquals("TEST_BASE_1/folder/folder2", model.getRelativePath(f2, true));
        assertEquals("${var.TEST_BASE_1}/folder", model.getRelativePath(f, false));
        assertEquals(var1.getAbsolutePath(), PropertyUtils.getGlobalProperties().getProperty("var.TEST_BASE_1"));
    }

}
