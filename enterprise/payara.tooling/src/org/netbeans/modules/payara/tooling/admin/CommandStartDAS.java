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
package org.netbeans.modules.payara.tooling.admin;

/**
 * Payara Server Start DAS Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerLocal.class)
@RunnerRestClass(runner=RunnerLocal.class)
public class CommandStartDAS extends CommandJavaClassPath {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** No command string is needed for Start DAS command but we may use it
     *  in logs. */
    private static final String COMMAND = "start-das";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** JVM options to be passed to java executable.
        Typically options as as <code>-D&lt;name&gt;=&lt;value&gt;</code>
        or <code>-X&lt;option&gt</code>. 
    */
    final String javaOpts;

    /** Payara specific arguments to be passed to
     *  bootstrap main method, e.g. <code>--domain domain_name</code>. */
    final String payaraArgs;
    
    /** Payara server domain directory (full path). */
    final String domainDir;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server start DAS command entity.
     * @param javaHome      Java SE home used to select JRE for Payara
     *                      server.
     * @param classPath     Java SE class path.
     * @param javaOptions   JVM options to be passed to java executable.
     * @param payaraArgs Payara specific arguments to be passed
     *                      to bootstrap main method.
     * @param domainDir     Payara server domain directory (full path).
     */
    public CommandStartDAS(final String javaHome, final String classPath,
            final String javaOptions, final String payaraArgs,
            final String domainDir) {
        super(COMMAND, javaHome, classPath);
        this.javaOpts = javaOptions;
        this.payaraArgs = payaraArgs;
        this.domainDir = domainDir;
    }

}
