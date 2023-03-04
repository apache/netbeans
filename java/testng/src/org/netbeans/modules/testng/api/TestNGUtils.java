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

package org.netbeans.modules.testng.api;

import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.CommonSettings;
import org.netbeans.modules.testng.TestConfigAccessor;
import org.netbeans.modules.testng.TestNGPlugin;
import org.netbeans.modules.testng.TestNGPluginTrampoline;
import org.netbeans.modules.testng.TestNGSettings;
import org.netbeans.modules.testng.TestUtil;
import org.netbeans.modules.testng.spi.TestConfig;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public class TestNGUtils {
    
    public static TestConfig getTestConfig(FileObject test, boolean rerun, String pkgName, String className, String methodName) {
        return TestConfigAccessor.getDefault().createTestConfig(test, rerun, pkgName, className, methodName);
    }
    
    public static boolean createTestActionCalled(FileObject[] filesToTest) {
        // Determine the plugin to be used:
        TestNGPlugin plugin = TestUtil.getPluginForProject(FileOwnerQuery.getOwner(filesToTest[0]));
        return TestNGPluginTrampoline.DEFAULT.createTestActionCalled(plugin, filesToTest);
    }
    
    /**
     * Creates test classes for given source classes.
     *
     * @param  filesToTest  source files for which test classes should be
     *                      created
     * @param  targetRoot   root folder of the target source root
     * @param  params  parameters of creating test class
     *                 - each key is an {@code Integer} whose value is equal
     *                 to some of the constants defined in the class;
     *                 the value is either
     *                 a {@code String} (for key with value {@code CLASS_NAME})
     *                 or a {@code Boolean} (for other keys)
     * @return  created test files
     */
    public static FileObject[] createTests(FileObject[] filesToTest, FileObject targetRoot, Map<CommonPlugin.CreateTestParam, Object> params) {
        // Determine the plugin to be used:
        // filesToTest might be null so use targetRoot to determine the project
        FileObject fo = filesToTest == null ? targetRoot : filesToTest[0];
        TestNGPlugin plugin = TestUtil.getPluginForProject(FileOwnerQuery.getOwner(fo));
        return TestNGPluginTrampoline.DEFAULT.createTests(plugin, filesToTest, targetRoot, params);
    }
    
    public static CommonSettings getTestNGSettings() {
        return TestNGSettings.getDefault();
    }
    
}
