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

package org.netbeans.spi.java.project.runner;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Implementation of {@link JavaRunner}. Looked-up in the default {@link Lookup}.
 *
 * @since 1.22
 * 
 * @author Jan Lahoda
 */
public interface JavaRunnerImplementation {

    /**
     * Implementation of {@link JavaRunner#isSupported(java.lang.String, java.util.Map)}.
     * 
     * @param command command name
     * @param toRun either the file that would be executed, or the project folder
     * @return true if and only if the given command is supported for given file/folder
     *
     * @since 1.22
     */
    public boolean isSupported(String command, Map<String, ?> properties);

    /**
     * Implementation of {@link JavaRunner#execute(java.lang.String, java.util.Map)}.
     *
     * @param command command to execute
     * @param props properties
     * @param toRun file to run
     * @throws java.io.IOException if execution fails
     *
     * @since 1.22
     */
    public ExecutorTask execute(String command, Map<String, ?> properties) throws IOException;
    
}
