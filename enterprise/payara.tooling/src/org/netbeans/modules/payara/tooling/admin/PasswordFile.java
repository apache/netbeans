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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import static org.netbeans.modules.payara.tooling.utils.ServerUtils.PF_DOMAIN_CONFIG_DIR_NAME;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Support for <code>asadmin</code> <code>--passwordfile</code> file format.
 * <p/>
 * <code>asadmin</code> <code>--passwordfile</code> argument specifies the name,
 * including the full path, of a file that contains password entries in
 * a specific format.
 * <p/>
 * Note that any password file created to pass as an argument by using
 * the <code>--passwordfile</code> option should be protected with file system
 * permissions. Additionally, any password file being used for a
 * transient purpose, such as setting up SSH among nodes, should be
 * deleted after it has served its purpose.
 * <p/>
 * The entry for a password must have the AS_ADMIN_ prefix followed by
 * the password name in uppercase letters, an equals sign, and the
 * password.
 * <p/>
 * The entries in the file that are read by the asadmin utility are as
 * follows:
 * <ul><li><code>AS_ADMIN_PASSWORD=administration-password</code></li>
 * <li><code>AS_ADMIN_MASTERPASSWORD=master-password</code></li>
 * The entries in this file that are read by subcommands are as
 * follows:
 * <li><code>AS_ADMIN_NEWPASSWORD=new-administration-password</code> (read by
 * the <code>start-domain</code> subcommand)</li>
 * <li><code>AS_ADMIN_USERPASSWORD=user-password</code> (read by the
 * <code>create-file-user</code> subcommand)</li>
 * <li><code>AS_ADMIN_ALIASPASSWORD=alias-password</code> (read by the
 * <code>create-password-alias</code> subcommand)</li>
 * <li><code>AS_ADMIN_MAPPEDPASSWORD=mapped-password</code> (read by the
 * <code>create-connector-security-map</code> subcommand)</li>
 * <li><code>AS_ADMIN_WINDOWSPASSWORD=windows-password</code> (read by the
 * <code>create-node-dcom</code>, <code>install-node-dcom</code>, and
 * <code>update-node-dcom</code> subcommands)</li>
 * <li><code>AS_ADMIN_SSHPASSWORD=sshd-password</code> (read by the
 * <code>create-node-ssh</code>, <code>install-node</code>,
 * <code>install-node-ssh</code>, and <code>update-node-ssh</code>
 * subcommands)</li>
 * <li><code>AS_ADMIN_SSHKEYPASSPHRASE=sshd-passphrase</code> (read by the
 * <code>create-node-ssh</code>, <code>install-node</code>,
 * <code>install-node-ssh</code>, and <code>update-node-ssh</code>
 * subcommands)</li>
 * <li><code>AS_ADMIN_JMSDBPASSWORD=jdbc-user-password</code> (read by the
 * <code>configure-jms-cluster</code> subcommand)</li></ul>
 * These password entries are stored in clear text in the password
 * file. To provide additional security, the create-password-alias
 * subcommand can be used to create aliases for passwords that are
 * used by remote subcommands. The password for which the alias is
 * created is stored in an encrypted form. If an alias exists for a
 * password, the alias is specified in the entry for the password as
 * follows:
 * <p/>
 * <code>AS_ADMIN_password-name=${ALIAS=password-alias-name}</code>
 * For example:
 * <ul><li><code>AS_ADMIN_SSHPASSWORD=${ALIAS=ssh-password-alias}</code></li>
 * <li><code>AS_ADMIN_SSHKEYPASSPHRASE=${ALIAS=ssh-key-passphrase-alias}</code></li></ul>
 * <p/>
 * @author Tomas Kraus
 */
public class PasswordFile {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(PasswordFile.class);

    /** Name of password file. */
    public static final String PASSWORD_FILE_NAME = "password-file";

    /** Password file permissions when file is being created before writing
     *  it's content. */
    private static final Set<PosixFilePermission> CREATE_FILE_PERMISSIONS
            = new HashSet<>();
    static {
        CREATE_FILE_PERMISSIONS.add(PosixFilePermission.OWNER_READ);
        CREATE_FILE_PERMISSIONS.add(PosixFilePermission.OWNER_WRITE);
    }

    /** Password file permissions when file is finished. */
    private static final Set<PosixFilePermission> FINAL_FILE_PERMISSIONS
            = new HashSet<>();
    static {
        FINAL_FILE_PERMISSIONS.add(PosixFilePermission.OWNER_READ);
    }

    /** Key to value assignment character. */
    private static final char ASSIGN_VALUE = '=';

    /** Administrator password key (mandatory). */
    private static final String AS_ADMIN_PASSWORD = "AS_ADMIN_PASSWORD";

    /** Master password key (optional). */
    private static final String AS_ADMIN_MASTERPASSWORD = "AS_ADMIN_MASTERPASSWORD";

    /** New administrator password to be set (optional). */
    private static final String AS_ADMIN_NEWPASSWORD = "AS_ADMIN_NEWPASSWORD";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    public static Path buildPasswordFilePath(final PayaraServer server) {
        final String METHOD = "buildPasswordFilePath";
        String domainsFolder = server.getDomainsFolder();
        String domainName = server.getDomainName();
        if (domainsFolder == null || domainName == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
        }
        return Paths.get(domainsFolder, domainName,
                PF_DOMAIN_CONFIG_DIR_NAME, PASSWORD_FILE_NAME);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Password file path. */
    Path file;

    /** Administrator password value (mandatory). */
    private String adminPassword;

    /** Master password value (optional). */
    private String masterPassword;

    /** New administrator password to be set (optional). */
    private String adminNewPassword;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Support for <code>asadmin</code>
     * <code>--passwordfile</code> file format.
     * <p/>
     * Content of password file is not read from file so at least administrator
     * password from {@link PayaraServer} must be provided. 
     * <p/>
     * @param server Payara server instance.
     */
    PasswordFile(final PayaraServer server) {
        file = buildPasswordFilePath(server);
        this.adminPassword = server.getAdminPassword();
        masterPassword = null;
        adminNewPassword = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get password file path as {@link String}.
     */
    public String getFilePath() {
        return file.toString();
    }

    /**
     * Get administrator password value (mandatory).
     * <p/>
     * @return Administrator password value (mandatory).
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Get administrator password value (mandatory).
     * <p/>
     * @param adminPassword Administrator password value (mandatory).
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * Get master password value (optional).
     * <p/>
     * @return Master password value (optional).
     */
    public String getMasterPassword() {
        return masterPassword;
    }

    /**
     * Get master password value (optional).
     * <p/>
     * @param masterPassword Master password value (optional).
     */
    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    /**
     * Get new administrator password to be set (optional).
     * <p/>
     * @return New administrator password to be set (optional).
     */
    public String getAdminNewPassword() {
        return adminNewPassword;
    }

    /**
     * Get new administrator password to be set (optional).
     * <p/>
     * @param adminNewPassword New administrator password to be set (optional).
     */
    public void setAdminNewPassword(String adminNewPassword) {
        this.adminNewPassword = adminNewPassword;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build password file content to be written into file.
     * <p/>
     * @return Password file content.
     */
    private String dataToWrite() {
        int len = AS_ADMIN_PASSWORD.length() + 1 + adminPassword.length();
        if (masterPassword != null) {
            len += AS_ADMIN_MASTERPASSWORD.length()
                    + 1 + masterPassword.length();
        }
        if (adminNewPassword != null) {
            len += AS_ADMIN_NEWPASSWORD.length()
                    + 1 + adminNewPassword.length();
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(AS_ADMIN_PASSWORD).append(ASSIGN_VALUE);
        sb.append(adminPassword).append(OsUtils.LINES_SEPARATOR);
        if (masterPassword != null) {
            sb.append(AS_ADMIN_MASTERPASSWORD).append(ASSIGN_VALUE);
            sb.append(masterPassword).append(OsUtils.LINES_SEPARATOR);
        }
        if (adminNewPassword != null) {
            sb.append(AS_ADMIN_NEWPASSWORD).append(ASSIGN_VALUE);
            sb.append(adminNewPassword).append(OsUtils.LINES_SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * Create password file to be written with access permissions to read
     * and write by user only.
     * <p/>
     * @return Value of <code>true</code> if new file was created
     *         or <code>false</code> otherwise
     */
    private boolean createFilePosix() {
        final String METHOD = "createFilePosix";
        boolean success = false;
        try {
            if (Files.notExists(file, new LinkOption[0])) {
                Files.createFile(file, PosixFilePermissions
                        .asFileAttribute(CREATE_FILE_PERMISSIONS));
                success = true;
            } else {
                Files.setPosixFilePermissions(file, CREATE_FILE_PERMISSIONS);
                LOGGER.log(Level.INFO, METHOD, "exists", file.toString());
            }
        } catch (UnsupportedOperationException uoe) {
            LOGGER.log(Level.INFO, METHOD, "unsupported", file.toString());
        } catch (FileAlreadyExistsException faee) {
            LOGGER.log(Level.INFO, METHOD, "exists", file.toString());
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, METHOD, "ioException", ioe);
        }
        return success;
    }

    /**
     * Update password file permissions when finished.
     * <p/>
     * File should exist.
     */
    private boolean finishFilePosix() {
        final String METHOD = "finishFilePosix";
        boolean success = false;
        try {
            Files.setPosixFilePermissions(file, FINAL_FILE_PERMISSIONS);
            success = true;
        } catch (UnsupportedOperationException uoe) {
            LOGGER.log(Level.INFO, METHOD, "unsupported", file.toString());
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, METHOD, "ioException", ioe);
        }
        return success;
    }

    /**
     * Write Payara password file using stored values.
     * <p/>
     * Attempts to set file access permissions to read by user only.
     * <p/>
     * @return Value of <code>true</code> when file was successfully written
     *         or <code>false</code> otherwise.
     */
    public boolean write() {
        final String METHOD = "write";
        if (adminPassword == null) {
            //throw new CommandException("noAdminPassword");
            adminPassword = "";
        }
        boolean success = true;
        Writer out = null;
        createFilePosix();
        try {
            out = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8");
            out.write(dataToWrite());
        } catch (IOException ioe) {
            success = false;
            LOGGER.log(Level.INFO, METHOD, "writeException",
                    new Object[] {file.toString(), ioe.getMessage()});
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    success = false;
                    LOGGER.log(Level.INFO, METHOD,
                            "closeException", file.toString());
                }
                finishFilePosix();
            }
        }
        return success;
    }
}
