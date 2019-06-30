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

import java.io.File;

/**
 * Offline asadmin command used to restore domain in junit tests.
 *
 * @author Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerAsadminRestoreDomain.class)
@RunnerRestClass(runner = RunnerAsadminRestoreDomain.class)
public class CommandRestoreDomain extends CommandJava {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for change administrator's password command. */
    private static final String COMMAND = "restore-domain";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Domain backup archive. */
    final File domainBackup;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server administration command entity
     * with specified server command, Java SE home and class path.
     * <p/>
     * @param javaHome Java SE home used to select JRE for Payara server.
     * @param domainBackup archive that contains domain restore.
     */
    public CommandRestoreDomain(final String javaHome,
            final File domainBackup) {
        super(COMMAND, javaHome);
        this.domainBackup = domainBackup;
    }
}
