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

package org.netbeans.modules.python.project.spi;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * An interface for unit test runner implementations.
 * (Copied from the equivalent Ruby class in ruby.project)
 *<p/>
 * <i>A work in progress, bound to change</i>.
 * 
 */
public interface TestRunner {
    
    enum TestType {
        /**
         * Represents PyUnit tests.
         */
        PY_UNIT,

        // TODO - other test frameworks here (for Ruby we had RSPEC, AUTOTEST etc)
        //RSPEC,
        //AUTOTEST
    }

    TestRunner getInstance();
    
    /**
     * Checks whether this test runner supports running of tests of the
     * given <code>type</code>.
     * 
     * @param type the type of the tests to run.
     * @return true if this test runner supports the given <code>type</code>.
     */
    boolean supports(TestType type);
    
    /**
     * Runs the given test file, i.e runs all tests
     * in it.
     * 
     * @param testFile the file representing a unit test class.
     * @param debug specifies whether the test file should be run 
     * in the debug mode.
     */
    void runTest(FileObject testFile, boolean debug);
    
    /**
     * Runs a single test method.
     * 
     * @param testFile the file representing the unit test class
     * whose test method to run.
     * @param className the class name of the test method to run.
     * @param testMethod the name of the test method to run.
     * @param debug specifies whether the test method should be run in the 
     * debug mode.
     */
    void runSingleTest(FileObject testFile, String className, String testMethod, boolean debug);
    
    /**
     * Runs all units tests in the given project.
     * 
     * @param project the project whose unit tests to run.
     * @param debug specifies whether the tests of the project should 
     * be run in the debug mode.
     */
    void runAllTests(Project project, boolean debug);

}
