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
package org.netbeans.modules.glassfish.tooling.data;

import java.util.List;
import java.util.Map;

/**
 * GlassFish Server Entity.
 * <p/>
 * Local GlassFish Server entity instance which is used when not defined in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class StartupArgsEntity implements StartupArgs {

    // Instance attributes                                                    //
    /** Command line arguments passed to bootstrap jar. */
    private List<String>glassfishArgs;

    /** Command line arguments passed to JVM. */
    private List<String> javaArgs;

    /** Environment variables set before JVM execution. */
    private Map<String, String> environmentVars;

    /** Installation home of Java SDK used to run GlassFish. */
    private String javaHome;

    // Constructors                                                           //
    /**
     * Constructs empty class instance. No default values are set.
     */
    public StartupArgsEntity() {
    }

    /**
     * Constructs class instance with all values supplied.
     * <p/>
     * @param glassfishArgs   Command line arguments passed to bootstrap jar.
     * @param javaArgs        Command line arguments passed to JVM.
     * @param environmentVars Environment variables set before JVM execution.
     * @param javaHome        Installation home of Java SDK used to
     *                        run GlassFish.
     */
    public StartupArgsEntity(List<String>glassfishArgs, List<String> javaArgs,
            Map<String, String> environmentVars, String javaHome) {
        this.glassfishArgs = glassfishArgs;
        this.javaArgs = javaArgs;
        this.environmentVars = environmentVars;
        this.javaHome = javaHome;
    }

    // Getters and Setters                                                    //
    /**
     * Get command line arguments passed to bootstrap jar.
     * <p/>
     * @return Command line arguments passed to bootstrap jar.
     */
    @Override
    public List<String> getGlassfishArgs() {
        return glassfishArgs;
    }

    /**
     * Set command line arguments passed to bootstrap jar.
     * <p/>
     * @param glassfishArgs Command line arguments passed to bootstrap jar.
     */
    public void setGlassfishArgs(List<String> glassfishArgs) {
        this.glassfishArgs = glassfishArgs;
    }

    /**
     * Get command line arguments passed to JVM.
     * <p/>
     * @return Command line arguments passed to JVM.
     */
    @Override
    public List<String> getJavaArgs() {
        return javaArgs;
    }

    /**
     * Set command line arguments passed to JVM.
     * <p/>
     * @param javaArgs Command line arguments passed to JVM.
     */
    public void getJavaArgs(List<String> javaArgs) {
        this.javaArgs = javaArgs;
    }

    /**
     * Get environment variables set before JVM execution.
     * <p/>
     * @return Environment variables set before JVM execution.
     */
    @Override
    public Map<String, String> getEnvironmentVars() {
        return environmentVars;
    }

    /**
     * Set environment variables set before JVM execution.
     * <p/>
     * @param environmentVars Environment variables set before JVM execution.
     */
    public void setEnvironmentVars(Map<String, String> environmentVars) {
        this.environmentVars = environmentVars;
    }

    /**
     * Get installation home of Java SDK used to run GlassFish.
     * <p/>
     * @return Installation home of Java SDK used to run GlassFish.
     */
    @Override
    public String getJavaHome() {
        return javaHome;
    }
    
    /**
     * Set installation home of Java SDK used to run GlassFish.
     * <p/>
     * @param javaHome Installation home of Java SDK used to run GlassFish.
     */
    public void getJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

}
