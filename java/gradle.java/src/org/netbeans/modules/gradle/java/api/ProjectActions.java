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
package org.netbeans.modules.gradle.java.api;

import org.netbeans.api.extexecution.base.ExplicitProcessParameters;

/**
 * Utilities and constants related to Gradle/Java project actions.
 * @author sdedic
 * @since 1.12
 */
public final class ProjectActions {
    /**
     * Replaceable token for JVM arguments project property. Generates project property for NB Tooling Gradle plugin, if the extra JVM arguments are present, otherwise
     * generates an empty String. The token is interpolated in <code>action-mapping.xml</code> that can be customized by the user. This feature cooperates with
     * NetBeans Tooling Gradle plugin provided by org.netbeans.gradle module.
     * <div class="nonnormative">
     * The token can be used in <code>action-mapping.xml</code> as followes:
     * {@snippet file="org/netbeans/modules/gradle/java/execute/example-action-mapping.xml" region="exampleActionMapping"}
     * </div>
     * The Gradle Java project support consumes {@link ExplicitProcessParameters} from the action's context Lookup, and populates the replaceable token mapping.
     * <div class="nonnormative">
     * The following code injects a specific system property to the VM configuration, and passes "hello Dolly" parameters to the application as main class' parameters. Project
     * actions can be invoked with custom parameters as in the following example:
     * {@snippet file="org/netbeans/modules/gradle/java/execute/JavaExecTokenProviderTest.java" region="testExamplePassJvmAndArguments"}
     * </div>
     */
    public static String TOKEN_JAVAEXEC_JVMARGS = "javaExec.jvmArgs"; // NOI18N

    /**
     * Replaceable token for program parameters as a commandline option. Generates --args <i>&lt;parameter-list></i>, if the extra parameters are present, otherwise
     * generates an empty String. See {@link #TOKEN_JAVAEXEC_JVMARGS} for code examples.
     * @since 1.9
     */
    public static String TOKEN_JAVAEXEC_ARGS = "javaExec.args"; // NOI18N

    /**
     * Replaceable token for program working directory. Generates project property for NB Tooling Gradle plugin, which is used in <code>action-mapping.xml</code>
     * and can be customized by the user. This feature cooperates with NetBeans Tooling Gradle plugin provided by org.netbeans.gradle module.
     * The Gradle Java project support consumes {@link ExplicitProcessParameters} from the action's context Lookup, and populates the replaceable token mapping
     * from {@link ExplicitProcessParameters#getWorkingDirectory()}.
     *
     * @since 1.15
     */
    public static String TOKEN_JAVAEXEC_CWD = "javaExec.workingDir"; // NOI18N

    /**
     * Replaceable token for program environment variables. Generates project property for NB Tooling Gradle plugin, which is used in <code>action-mapping.xml</code>
     * and can be customized by the user. This feature cooperates with NetBeans Tooling Gradle plugin provided by org.netbeans.gradle module.
     * The Gradle Java project support consumes {@link ExplicitProcessParameters} from the action's context Lookup, and populates the replaceable token mapping
     * from {@link ExplicitProcessParameters#getEnvironmentVariables()}.
     *
     * @since 1.15
     */
    public static String TOKEN_JAVAEXEC_ENV = "javaExec.environment"; // NOI18N

    private ProjectActions() {}
}
