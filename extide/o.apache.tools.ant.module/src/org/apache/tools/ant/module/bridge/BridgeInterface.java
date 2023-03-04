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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * What is implemented by bridge.jar.
 * @author Jesse Glick
 */
public interface BridgeInterface {

    /**
     * Actually run a build script.
     * @param buildFile an Ant build script
     * @param targets a list of target names to run, or null to run the default target
     * @param in an input stream for console input
     * @param out an output stream with the ability to have hyperlinks
     * @param err an error stream with the ability to have hyperlinks
     * @param properties any Ant properties to define
     * @param concealedProperties  the names of the properties whose values should not be visible to the user
     * @param verbosity the intended logging level
     * @param displayName a user-presentable name for the session
     * @param interestingOutputCallback will be called if and when some interesting output appears, or input is requested
     * @param handle a progress handle to update if appropriate (switch to sleeping and back to indeterminate)
     * @param io raw I/O handle for more advanced output
     * @return true if the build succeeded, false if it failed for any reason
     */
    boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties,
            Set<? extends String> concealedProperties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io);
    
    /**
     * Try to stop a running build.
     * The implementation may wait for a while to stop at a safe point,
     * and/or stop forcibly.
     * @param process the thread which is currently running the build (in which {@link #run} was invoked)
     */
    void stop(Thread process);
    
    /**
     * Get some informational value of the Ant version.
     * @return the version
     */
    String getAntVersion();
    
    /**
     * Check whether Ant 1.6 is loaded.
     * If so, additional abilities may be possible, such as namespace support.
     */
    boolean isAnt16();
    
    /**
     * Get a proxy for IntrospectionHelper, to introspect task + type structure.
     */
    IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz);
    
    /**
     * See Project.toBoolean.
     */
    boolean toBoolean(String val);
    
    /**
     * Get values of an enumeration class.
     * If it is not actually an enumeration class, return null.
     */
    String[] getEnumeratedValues(Class<?> c);
    
}
