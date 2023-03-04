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
package org.netbeans.modules.testng.spi;

import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lukas
 */
public abstract class TestNGSupportImplementation {

    private static final Logger LOGGER = Logger.getLogger(TestNGSupportImplementation.class.getName());

    /**
     * Check whether given project instance is supported by this implementation
     *
     * @param p project to check
     * @return true if this instance supports given project
     */
    public abstract boolean isActionSupported(Action action, Project p);

    /**
     * Check whether this implementation supports given FileObjects. Default implementation return false.
     *
     * @param activatedFOs FileoBjects to check
     * @return true if this instance supports given FileObjects, false otherwise
     */
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return false;
    }

    /**
     * Configure project owning given FileObject
     *
     * @param createdFile FileObject for which the project should be configured
     */
    public abstract void configureProject(FileObject createdFile);

    /**
     * Create an instance of TestExecutor interface used for running
     * particular actions
     * 
     * @param p project for which the TestExecutor should be created
     * @return instance of TestExecutor
     */
    public abstract TestExecutor createExecutor(Project p);

    /**
     *
     */
    public interface TestExecutor {

        /**
         * Return true if configuration file for failed tests exists,
         * false otherwise
         *
         * @return true if configuration file for failed tests exists
         */
        boolean hasFailedTests();

        /**
         * Execute tests defined in test config
         *
         * @param config test config to run
         */
        public void execute(Action action, TestConfig config) throws IOException;
    }
}
