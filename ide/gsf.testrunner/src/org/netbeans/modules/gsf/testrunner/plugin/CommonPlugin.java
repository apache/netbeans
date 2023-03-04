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

package org.netbeans.modules.gsf.testrunner.plugin;

import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 * SPI for custom implementations of support for Unit Testing.
 * It declares methods for:
 * <ul>
 *     <li>navigation between source classes and corresponding test classes
 *         ({@link #getTestLocation getTestLocation},
 *          {@link #getTestedLocation getTestedLocation})</li>
 *     <li>creation of test class skeletons
 *         ({@link #createTests createTests})</li>
 * </ul>
 *
 * @author  Marian Petras
 */
public abstract class CommonPlugin {
    
    /**
     * Default constructor for use by subclasses.
     */
    protected CommonPlugin() {}

    /**
     * Enumeration of test creation parameters.
     */
    public enum CreateTestParam {
        
        /**
         * key for the map of test creation parameters
         * - name of the test class
         */
        CLASS_NAME(99310),
        /**
         * key for the map of test creation parameters
         * - include tests for public methods?
         */
        INC_PUBLIC(99311),
        /**
         * key for the map of test creation parameters
         * - include tests for protected methods?
         */
        INC_PROTECTED(99312),
        /**
         * key for the map of test creation parameters
         * - include tests for package-private methods?
         */
        INC_PKG_PRIVATE(99313),
        /**
         * key for the map of test creation parameters
         * - generate test initializer method ({@code setup()}/{@code @Before})?
         */
        INC_SETUP(99314),
        /**
         * key for the map of test creation parameters
         * - generate test finalizer method ({@code tearDown()}/{@code @After})?
         */
        INC_TEAR_DOWN(99315),
        /**
         * key for the map of test creation parameters
         * - generate test class initializer method ({@code @BeforeClass})?
         */
        INC_CLASS_SETUP(99323),
        /**
         * key for the map of test creation parameters
         * - generate test class finalizer method ({@code @AfterClass})?
         */
        INC_CLASS_TEAR_DOWN(99324),
        /**
         * key for the map of test creation parameters
         * - generate default test method bodies?
         */
        INC_METHOD_BODIES(99316),
        /**
         * key for the map of test creation parameters
         * - generate Javadoc comments for test methods?
         */
        INC_JAVADOC(99317),
        /**
         * key for the map of test creation parameters
         * - generate source code hints?
         */
        INC_CODE_HINT(99318),
        /**
         * key for the map of test creation parameters
         * - generate test classes for package-private classes?
         */
        INC_PKG_PRIVATE_CLASS(99319),
        /**
         * key for the map of test creation parameters
         * - generate test classes for abstract classes?
         */
        INC_ABSTRACT_CLASS(99320),
        /**
         * key for the map of test creation parameters
         * - generate test classes for exception classes?
         */
        INC_EXCEPTION_CLASS(99321),
        /**
         * key for the map of test creation parameters
         * - generate test suites for packages?
         */
        INC_GENERATE_SUITE(99322),
        /**
         * key for the map of test creation parameters
         * - generate integration test?
         */
        INC_GENERATE_INTEGRATION_TEST(99323);
        
        private final int idNumber;
        
        CreateTestParam(int idNumber) {
            this.idNumber = idNumber;
        }
        
        /**
         * Return a unique number of this enum element.
         *
         * @return  unique number of this enum element
         */
        public int getIdNumber() {
            return idNumber;
        }
        
    }
    
    /**
     * Data structure for storage of specification of a Java element or
     * a Java file.
     */
    public static final class Location {
        /**
         * holds specification of a Java file
         */
        private final FileObject fileObject;
        
        /**
         * Creates a new instance.
         *
         * @param  fileObject  the {@code FileObject}
         * 
         * 
         * 
         */
        public Location(FileObject fileObject) {
            if (fileObject == null) {
               throw new IllegalArgumentException("fileObject is null");//NOI18N
            }
            
            this.fileObject = fileObject;
        }
        
        /**
         * Returns the {@code FileObject}.
         *
         * @return  the {@code FileObject} held in this instance
         */
        public FileObject getFileObject() {
            return fileObject;
        }
        
    }
    
    /**
     * Returns a specification of a Java element or file representing test
     * for the given source Java element or file.
     *
     * @param  sourceLocation  specification of a Java element or file
     * @return  specification of a corresponding test Java element or file,
     *          or {@code null} if no corresponding test Java file is available
     */
    protected abstract Location getTestLocation(Location sourceLocation);
    
    /**
     * Returns a specification of a Java element or file that is tested
     * by the given test Java element or test file.
     *
     * @param  testLocation  specification of a Java element or file
     * @return  specification of a Java element or file that is tested
     *          by the given Java element or file.
     */
    protected abstract Location getTestedLocation(Location testLocation);
    
    /**
     * Informs whether the plugin is capable of creating tests at the moment.
     * The default implementation returns {@code true}.
     *
     * @return  {@code true} if the plugin is able of creating tests
     *          for the given {@code FileObject}s, {@code false} otherwise
     * @see  #createTests
     */
    protected boolean canCreateTests(FileObject... fileObjects) {
        return true;
    }
    
    /**
     * Creates test classes for given source classes.
     * If the plugin does not support creating tests, implementation of this
     * method should return {@code null}.
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
     * @return  created test files, or {@code null} if no test classes were
     *          created and/or updated
     * @see  #canCreateTests
     */
    protected abstract FileObject[] createTests(
            FileObject[] filesToTest,
            FileObject targetRoot,
            Map<CreateTestParam, Object> params);

    /**
     * Called immediately after the <em>Create Test</em> action was called.
     * It can be used as a trigger for additional checks and/or for displaying
     * user dialogs etc. It is always called from the event-dispatching thread.
     *
     * @param  selectedFiles  files and folders/packages that were selected
     *                        when the action was called
     * @return  {@code true} if the action can continue,
     *          {@code false} if the action should not continue;
     *          the default implementation returns always {@code true}
     */
    protected boolean createTestActionCalled(FileObject[] selectedFiles) {
        return true;
    }

}
