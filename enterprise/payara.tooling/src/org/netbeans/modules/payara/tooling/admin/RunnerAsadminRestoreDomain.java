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

import static org.netbeans.modules.payara.tooling.admin.RunnerJava.PARAM_ASSIGN_VALUE;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 *
 * @author Peter Benedikovic
 */
public class RunnerAsadminRestoreDomain extends RunnerAsadmin {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(RunnerAsadminRestoreDomain.class);

    /** Specifies the domain dir. */
    private static final String DOMAIN_DIR_PARAM = "--domaindir";

    /** Specifies the directory where the backup archive is stored. */
    private static final String BACKUP_DIR_PARAM = "--backupdir";

    /** Specifies the name of the backup archive. */
    private static final String BACKUP_FILE_PARAM = "--filename";

    /** Specifies the force param needed to restore from non-standard location. */
    private static final String FORCE_PARAM = "--force";


    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds <code>change-admin-password</code> command query string.
     */
    private static String query(final PayaraServer server,
            final Command command) {
        final String METHOD = "query";
        CommandRestoreDomain restoreCommand;
        String domainsFolder = OsUtils.escapeString(server.getDomainsFolder());
        String domainName = OsUtils.escapeString(server.getDomainName());
        if (domainName == null || domainsFolder == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
        }
        if (command instanceof CommandRestoreDomain) {
            restoreCommand = (CommandRestoreDomain)command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DOMAIN_DIR_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainsFolder);
        sb.append(PARAM_SEPARATOR);
        sb.append(FORCE_PARAM);
        sb.append(PARAM_SEPARATOR);
        sb.append(BACKUP_FILE_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(restoreCommand.domainBackup.getAbsolutePath());
        sb.append(PARAM_SEPARATOR);
        sb.append(domainName);
        System.out.println("Restore command params: " + sb.toString());
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandRestoreDomain command;

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
    public RunnerAsadminRestoreDomain(final PayaraServer server,
            final Command command) {
        super(server, command, query(server, command));
        final String METHOD = "init";
        if (command instanceof CommandRestoreDomain) {
            this.command = (CommandRestoreDomain)command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
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
                new String[] {"Command restore-domain failed"});
        return processIOContent;
    }
}
