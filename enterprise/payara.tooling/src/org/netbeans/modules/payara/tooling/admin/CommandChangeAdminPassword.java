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
 * Payara server change administrator's password administration
 * command entity.
 * <p/>
 * @author Tomas Kraus
 */
@RunnerHttpClass(runner=RunnerAsadminChangeAdminPassword.class)
@RunnerRestClass(runner=RunnerAsadminChangeAdminPassword.class)
public class CommandChangeAdminPassword extends CommandJava {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for change administrator's password command. */
    private static final String COMMAND = "change-admin-password";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara administrator's new password to be set.
     *  Value of <code>null</code> or empty <code>String</code> means
     *  no password. */
    final String password;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server administration command entity
     * with specified server command, Java SE home and class path.
     * <p/>
     * @param javaHome Java SE home used to select JRE for Payara server.
     * @param password Payara administrator's new password to be set.
     */
    public CommandChangeAdminPassword(final String javaHome,
            final String password) {
        super(COMMAND, javaHome);
        this.password = password;
    }

}
