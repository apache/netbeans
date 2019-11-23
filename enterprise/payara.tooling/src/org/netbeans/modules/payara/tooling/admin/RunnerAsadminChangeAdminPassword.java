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

import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Change administrator password command execution using local
 * asadmin interface.
 * <p/>
 * @author Tomas Kraus
 */
public class RunnerAsadminChangeAdminPassword extends RunnerAsadmin {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(RunnerAsadminChangeAdminPassword.class);

    /** Specifies the domain of the administrator user. */
    private static final String DOMAIN_NAME_PARAM = "--domain_name";

    /** Specifies the parent directory of the domain specified
     *  in the --domain_name option. */
    private static final String DOMAINDIR_PARAM = "--domaindir";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds <code>change-admin-password</code> command query string.
     */
    private static String query(final PayaraServer server,
            final Command command) {
        final String METHOD = "query";
        String domainsFolder = OsUtils.escapeString(server.getDomainsFolder());
        String domainName = OsUtils.escapeString(server.getDomainName());
        if (domainName == null || domainsFolder == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
        }
        StringBuilder sb = new StringBuilder(
                DOMAIN_NAME_PARAM.length() + 1 + domainName.length() + 1
                + DOMAINDIR_PARAM.length() + 1 + domainsFolder.length());
        sb.append(DOMAINDIR_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainsFolder);
        sb.append(PARAM_SEPARATOR);
        sb.append(DOMAIN_NAME_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainName);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandChangeAdminPassword command;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * command line asadmin interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerAsadminChangeAdminPassword(final PayaraServer server,
            final Command command) {
        super(server, command, query(server, command));
        final String METHOD = "init";
        if (command instanceof CommandChangeAdminPassword) {
            this.command = (CommandChangeAdminPassword)command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
            passwordFile.setAdminNewPassword(this.command.password);
    }
 
    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create internal <code>ProcessIOContent</code> object corresponding
     * to command execution IO.
     */
    @Override
    protected ProcessIOContent createProcessIOContent() {
        ProcessIOContent processIOContent = new ProcessIOContent();
        processIOContent.addOutput(
                new String[] {"Command", "executed successfully"},
                new String[] {"Command change-admin-password failed"});
        return processIOContent;
    }

}
