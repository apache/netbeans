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

package org.netbeans.modules.testng;

import java.util.Map;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Marian Petras
 */
public abstract class TestNGPluginTrampoline {

    /** the trampoline singleton, defined by {@link TestNGPlugin} */
    public static TestNGPluginTrampoline DEFAULT;

    /**
     * Provokes initialization of class TestNGPlugin.
     */
    {
        Class c = TestNGPlugin.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Used by {@link TestNGPlugin}. */
    public TestNGPluginTrampoline() {}
    
    /**
     */
    public abstract boolean createTestActionCalled(
            TestNGPlugin plugin,
            FileObject[] filesToTest);
    
    /**
     * Returns a specification of a Java element or file representing test
     * for the given source Java element or file.
     *
     * @param  sourceLocation  specification of a Java element or file
     * @return  specification of a corresponding test Java element or file,
     *          or {@code null} if no corresponding test Java file is available
     */
    public abstract Location getTestLocation(
            TestNGPlugin plugin,
            Location sourceLocation);
    
    /**
     * Returns a specification of a Java element or file that is tested
     * by the given test Java element or test file.
     *
     * @param  testLocation  specification of a Java element or file
     * @return  specification of a Java element or file that is tested
     *          by the given Java element or file.
     */
    public abstract Location getTestedLocation(
            TestNGPlugin plugin,
            Location testLocation);
    
    /**
     * Determines whether the given plugin is capable of creating tests
     * for the given files at the moment.
     * The default implementation returns {@code true}.
     *
     * @param  plugin  plugin to be queried
     * @param  fileObjects  {@code FileObject}s for which the tests are about
     *                      to be created
     * @return  {@code true} if the given plugin is able of creating tests
     *          for the given {@code FileObject}s, {@code false} otherwise
     * @see  #createTests
     */
    public abstract boolean canCreateTests(
            TestNGPlugin plugin,
            FileObject... fileObjects);

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
    public abstract FileObject[] createTests(
            TestNGPlugin plugin,
            FileObject[] filesToTest,
            FileObject targetRoot,
            Map<CreateTestParam, Object> params);

}
