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

package org.netbeans.modules.projectimport.eclipse.core.spi;

/**
 * Represents an Eclipse launch configuration (*.launch file).
 */
public final class LaunchConfiguration {

    /** {@link #getType} for running the plain Java launcher. */
    public static final String TYPE_LOCAL_JAVA_APPLICATION = "org.eclipse.jdt.launching.localJavaApplication";

    private final String name;
    private final String type;
    private final String projectName;
    private final String mainType;
    private final String programArguments;
    private final String vmArguments;
    // XXX should support classpath, and map to NB run.classpath; but format is too tricky to deal with for now

    public LaunchConfiguration(String name, String type, String projectName, String mainType, String programArguments, String vmArguments) {
        this.name = name;
        this.type = type;
        this.projectName = projectName;
        this.mainType = mainType;
        this.programArguments = programArguments;
        this.vmArguments = vmArguments;
    }

    /**
     * Gets the name of the configuration.
     * @return an identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of configuration.
     * @return a classification, e.g. {@link #TYPE_LOCAL_JAVA_APPLICATION}
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the project for which the configuration applies.
     * @return the project name (could be null for a general configuration)
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the main class run to run.
     * @return the main type (as a Java FQN), if set; else null
     */
    public String getMainType() {
        return mainType;
    }

    /**
     * Gets a list of program arguments.
     * @return a (space-separated) list of arguments, if set; else null
     */
    public String getProgramArguments() {
        return programArguments;
    }

    /**
     * Gets a list of (J)VM arguments.
     * @return a (space-separated) list of arguments, if set; else null
     */
    public String getVmArguments() {
        return vmArguments;
    }

}
