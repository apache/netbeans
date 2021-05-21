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
package org.netbeans.modules.payara.common.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import static org.netbeans.modules.payara.tooling.utils.ServerUtils.PF_DOMAIN_CONFIG_DIR_NAME;
import static org.netbeans.modules.payara.tooling.utils.ServerUtils.addPathElement;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara <code>admin-keyfile</code>.
 * <p/>
 * This class will be moved into Payara Tooling Library after beta release.
 * Now it's part of NetBeans to avoid new untested Payara Tooling Library
 * changes.
 * <p/>
 * @author Tomas Kraus
 */
public class AdminKeyFile {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Parse content of <code>admin-keyfile</code>.
     * <p/>
     * Currently <code>admin-keyfile</code> contains one line with 3 fields
     * separated by ';':<br/>
     * <ul><li><code>&lt;file&gt; :: &lt;line&gt;</code></li>
     * <li><code>&lt;line&gt; :: &lt;user&gt; ';' &lt;password hash&gt; ';' &lt;tool&gt; &lt;eol&gt;</code></li>
     * <li><code>&lt;eol&gt; :: '\n' || '\r' '\n'</code></li>
     * <li><code>&lt;user&gt; :: [^;\r\n]+</code></li>
     * <li><code>&lt;password hash&gt; :: [^;\r\n]+</code></li>
     * <li><code>&lt;tool&gt; :: [^;\r\n]+</code></li></ul>
     * Empty values of <code>&lt;user&gt;</code>,
     * <code>&lt;password hash&gt;</code> and <code>&lt;tool&gt;</code>
     * are not allowed.
     */
    private static class Parser {

        /**
         * State machine input classes.
         */
        private enum Input {
            /** Content of user, password hash or tool strings. */
            STRING,
            /** Separator character. */
            SEPARATOR,
            /** CR character, beginning of CRLF sequence. */
            CR,
            /** LF Character. */
            LF;

            /** Enumeration length. */
            private static final int length = Input.values().length;

            /**
             * Get input class value for provided character.
             * <p/>
             * @param c Character to check tor  input class.
             * @return Input class of provided character.
             */
            private static Input value(final char c) {
                switch (c) {
                    case AdminKeyFile.SEPARATOR:
                        return SEPARATOR;
                    case '\r':
                        return CR;
                    case '\n':
                        return LF;
                    default:
                        return STRING;
                }
            }
        }
        
        /**
         * State machine internal states.
         */
        private enum State {
            /** Initial state, expecting user <code>String</code>
             *  1st character. */
            START,
            /** Reading user <code>String</code> characters until
             *  1st separator. */
            USER_N,
            /** Expecting password hash <code>String</code> 1st character. */
            HASH_S,
            /** Reading password hash <code>String</code> characters until
             *  2st separator. */
            HASH_N,
            /** Expecting tool <code>String</code> 1st character. */
            TOOL_S,
            /** Reading tool <code>String</code> characters until
                end of line or end of file. */
            TOOL_N,
            /** Got '\r', expecting '\n' from EOL. */
            CR,
            /** Full line received. */
            LF,
            /** Error state. */
            ERROR;

            /** Enumeration length. */
            private static final int length = State.values().length;

            /** Transition table for [State, Input]. */
            private static final State transition[][] = {
              // STRING  SEPARATOR  CR     LF
                {USER_N,  ERROR,    CR,    LF}, // START
                {USER_N, HASH_S,    CR,    LF}, // USER_N
                {HASH_N,  ERROR,    CR,    LF}, // HASH_S
                {HASH_N, TOOL_S,    CR,    LF}, // HASH_N
                {TOOL_N,  ERROR,    CR,    LF}, // TOOL_S
                {TOOL_N,  ERROR,    CR,    LF}, // TOOL_N
                { ERROR,  ERROR, ERROR,    LF}, // CR
                { ERROR,  ERROR, ERROR,    LF}, // LF
                { ERROR,  ERROR, ERROR, ERROR}  // ERROR
            };

            /**
             * State machine transition.
             * <p/>
             * @param s Current machine state.
             * @param i current input class.
             * @return Next machine state.
             */
            private static State next(final State s, final Input i) {
                return transition[s.ordinal()][i.ordinal()];
            }
        }

        /** Default user builder size to avoid resizing. */
        private static final int USER_BUILDER_SIZE = 16;

        /** Default password hash builder size to avoid resizing. */
        private static final int HASH_BUILDER_SIZE = 96;

        /** Default tool builder size to avoid resizing. */
        private static final int TOOL_BUILDER_SIZE = 16;

        /** Internal <code>admin-keyfile</code> reader buffer size. */
        private static final int BUFFER_SIZE = 128;

        /** User <code>String</code> being built. */
        final StringBuilder user;

        /** Password hash <code>String</code> being built. */
        final StringBuilder hash;
        
        /** Tool <code>String</code> being built. */
        final StringBuilder tool;

        /** Reader on <code>admin-keyfile</code>. */
        private final Reader in;

        /** Internal buffer for <code>admin-keyfile</code> reader. */
        private final char buff[];

        /** Number of characters stored in <code>admin-keyfile</code>
         *  reader buffer. */
        private int len;

        /** Machine internal state. */
        private State state;
    
        /**
         * Creates an instance of <code>admin-keyfile</code> content parser.
         * <p/>
         * @param in {@see Reader} on <code>admin-keyfile</code> positioned
         *           at the beginning of the file.
         */
        private Parser(final Reader in) {
            user = new StringBuilder(USER_BUILDER_SIZE);
            hash = new StringBuilder(HASH_BUILDER_SIZE);
            tool = new StringBuilder(TOOL_BUILDER_SIZE);
            this.in = in;
            buff = new char[BUFFER_SIZE];
            len = 0;
            state = State.START;
        }

        /**
         * Reads and parses content of <code>admin-keyfile</code>.
         * <p/>
         * {@see Reader} provided in constructor is not closed at the end
         * of reading and parsing. It must be done by method caller.
         * <p/>
         * @return Value of <code>true</code> when <code>admin-keyfile</code>
         *         was read successfully or <code>false</code> otherwise.
         */
        private boolean parse() {
            boolean exit = !read();
            while(!exit) {
                for (int pos = 0; pos < len; pos++) {
                    state = action(buff[pos]);
                }
                exit = !read();
            }
            return state == State.TOOL_N || state == State.LF;
        }

        /**
         * Read data from <code>admin-keyfile</code> {@see Reader} and store
         * them into internal buffer.
         * <p/>
         * @return Value of <code>true</code> when read finished successfully
         *         or <code>false</code> otherwise.
         */
        private boolean read() {
            try {
                return (len = in.read(buff)) >= 0;
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO,
                        "Error reading admin-keyfile: {0}", ioe.getMessage());
                return false;
            }
        }

        /**
         * Run parser action based on current state and character class.
         * <p/>
         * @param c Current character being processed from {@see Reader} buffer.
         * @return Next state transition based on current state
         *         and character class.
         */
        private State action(final char c) {
            Input cl = Input.value(c);
            switch (state) {
                case START: switch (cl) {
                        case STRING:
                            userChar(c);
                            break;
                    } break;
                case USER_N: switch (cl) {
                        case STRING:
                            userChar(c);
                            break;
                    } break;
                case HASH_S: switch (cl) {
                        case STRING:
                            hashChar(c);
                            break;
                    } break;
                case HASH_N: switch (cl) {
                        case STRING:
                            hashChar(c);
                            break;
                    } break;
                case TOOL_S: switch (cl) {
                        case STRING:
                            toolChar(c);
                            break;
                    } break;
                case TOOL_N: switch (cl) {
                        case STRING:
                            toolChar(c);
                            break;
                    } break;
            }
            return State.next(state, cl);
        }

        /**
         * Append current character to user <code>String</code>.
         * <p/>
         * @param c Current character from {@see Reader} buffer.
         */
        private void userChar(final char c) {
            user.append(c);
        }

        /**
         * Append current character to password hash <code>String</code>.
         * <p/>
         * @param c Current character from {@see Reader} buffer.
         */
        private void hashChar(final char c) {
            hash.append(c);
        }

        /**
         * Append current character to tool <code>String</code>.
         * <p/>
         * @param c Current character from {@see Reader} buffer.
         */
        private void toolChar(final char c) {
            tool.append(c);
        }

        /**
         * Get value of user <code>String</code> read
         * from <code>admin-keyfile</code>.
         * <p/>
         * @rerurn Value of user <code>String</code>.
         */
        private String getUser() {
            return user.toString();
        }

        /**
         * Get value of password hash <code>String</code> read
         * from <code>admin-keyfile</code>.
         * <p/>
         * @rerurn Value of password hash <code>String</code>.
         */
        private String getPasswordHash() {
            return hash.toString();
        }

        /**
         * Get value of tool <code>String</code> read
         * from <code>admin-keyfile</code>.
         * <p/>
         * @rerurn Value of tool <code>String</code>.
         */
        private String getTool() {
            return tool.toString();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local Payara module logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(AdminKeyFile.class);

    /** Default Payara administrator username. */
    public static final String DEFAULT_USER = "admin";

    /** Default Payara <code>admin-keyfile</code> file tool. */
    public static final String DEFAULT_TOOL = "asadmin";

    /** Password reset <code>String</code> in password hash field. */
    public static final String PASSWORD_RESET = "RESET";

    /** Name of <code>admin-keyfile</code> file. */
    public static final String ADMIN_KEYFILE_NAME = "admin-keyfile";

    /** Separator of <code>admin-keyfile</code> fields. */
    public static final char SEPARATOR = ';';

    /** Hash algorithm prefix in <code>admin-keyfile</code>. */
    public static final char HASH_ALGORITHM_PREFIX = '{';

    /** Hash algorithm suffix in <code>admin-keyfile</code>. */
    public static final char HASH_ALGORITHM_SUFFIX = '}';

    /** Password encoding used to compute password hash. */
    public static final String PASSWORD_ENCODING = "UTF-8";

    /** <code>MessageDigest</code> algorithm to encode password. */
    public static final String HASH_ALGORITHM = "SHA-1";

    /** Hash algorithm name in <code>admin-keyfile</code>. */
    public static final String HASH_ALGORITHM_GALSSFISH = "SSHA";
    
    /** End of line. */
    public static final String EOL = System.getProperty("line.separator");

    /** Default random password length. */
    public static final int RANDOM_PASSWORD_LENGTH = 12;

    /** Random password character map. */
    private static final char[] passwordChars = {
        // Capital letters
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z',
        // Lower letters
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z',
        // Numeric literals
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        // Special characters
        '!', '#', '%', '&', '+', '-', '.', '@', '^', '_'
    };

    /** Random password character map size to use for 1st character. */
    private static final int CHARS_PW0 = 52;

    /** Random password character map size to use for inner characters. */
    private static final int CHARS_PW = passwordChars.length;

    /** Random password character map size to use for last character. */
    private static final int CHARS_PWL= 62;

    /** Minimal password size. Keep this value at least 3. */
    private static final int MIN_PW_SIZE = 3;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build <code>admin-keyfile</code> path for provided server instance.
     * <p/>
     * @param server Payara server instance.
     * @return Full <code>admin-keyfile</code> path.
     */
    public static String buildAdminKeyFilePath(final PayaraServer server) {
        String domainsFolder = server.getDomainsFolder();
        String domainName = server.getDomainName();
        if (domainsFolder == null || domainName == null) {
            throw new IllegalArgumentException(
                    "Domains folder and domain name shall not be null.");
        }
        StringBuilder sb = new StringBuilder(
                domainsFolder.length() + OsUtils.FILE_SEPARATOR_LENGTH
                + domainName.length() + OsUtils.FILE_SEPARATOR_LENGTH
                + PF_DOMAIN_CONFIG_DIR_NAME.length()
                + OsUtils.FILE_SEPARATOR_LENGTH + ADMIN_KEYFILE_NAME.length());
        sb.append(domainsFolder);
        addPathElement(sb, domainName);
        addPathElement(sb, PF_DOMAIN_CONFIG_DIR_NAME);
        addPathElement(sb, ADMIN_KEYFILE_NAME);
        return sb.toString();
    }

    /**
     * Generate random password of given length.
     * <p/>
     * @param len PAssword length. Minimal size is 3.
     */
    public static String randomPassword(int len) {
        // Update to minimal size.
        if (len < MIN_PW_SIZE) {
            len = MIN_PW_SIZE;
        }
        Random random = new Random();
        StringBuilder pw = new StringBuilder(len);
        if (len > 0) {
            int inLen = len - 1;
            pw.append(passwordChars[random.nextInt(CHARS_PW0)]);
            for (int i = 1; i < inLen; i++) {
                pw.append(passwordChars[random.nextInt(CHARS_PW)]);
            }
            if (len > 1) {
                pw.append(passwordChars[random.nextInt(CHARS_PWL)]);
            }
        }
        return pw.toString();
    }
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara <code>admin-keyfile</code> path. */
    private final String adminKeyFile;

    /** Payara administrator username stored
     *  in <code>admin-keyfile</code>. */
    private String user;

    /** Payara administrator password hash stored
     *  in <code>admin-keyfile</code>. */
    private String passwordHash;

    /** Payara tool stored in <code>admin-keyfile</code>. */
    private String tool;

    /** Payara administrator password hash contains <code>RESET</code>
     *  <code>String</code>. */
    private boolean reset;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara <code>admin-keyfile</code>.
     * <p/>
     * Default user and tool values and <code>null</code> password hash value
     * are set.
     * <p/>
     * @param server Payara server instance.
     */
    public AdminKeyFile(final PayaraServer server) {
        adminKeyFile = buildAdminKeyFilePath(server);
        user = DEFAULT_USER;
        passwordHash = null;
        tool = DEFAULT_TOOL;
        reset = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Read Payara <code>admin-keyfile</code> file and store it into
     * this instance if read successfully.
     * <p/>
     * @return Value of <code>true</code> when file was successfully read
     *         and stored or <code>false</code> otherwise.
     */
    public boolean read() {
        boolean success = false;
        Parser parser = null;
        Reader in = null;
        try {
            in = new FileReader(adminKeyFile);
            parser = new Parser(in);
            success = parser.parse();
        } catch (IOException ioe) {
            success = false;
            LOGGER.log(Level.INFO, "Caught IOException when reading {0}: {1}",
                    new Object[] {adminKeyFile, ioe.getMessage()});
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    success = false;
                    LOGGER.log(Level.INFO,
                            "Cannot close {0} Reader", adminKeyFile);
                }
            }
        }
        if (success && parser != null) {
            user = parser.getUser();
            passwordHash = parser.getPasswordHash();
            tool = parser.getTool();
            reset = PASSWORD_RESET.equalsIgnoreCase(passwordHash);
        }
        return success;
    }

    /**
     * Write Payara <code>admin-keyfile</code> file using stored values.
     * <p/>
     * @return Value of <code>true</code> when file was successfully written
     *         or <code>false</code> otherwise.
     */
    public boolean write() {
        if (user == null) {
            throw new IllegalStateException("No user is set");
        }
        if (passwordHash == null) {
            throw new IllegalStateException("No password hash is set");
        }
        if (tool == null) {
            throw new IllegalStateException("No tool is set");
        }
        boolean success = true;
        StringBuilder sb = new StringBuilder(user.length() + 1
                + passwordHash.length() + 1 + tool.length() + EOL.length());
        sb.append(user);
        sb.append(SEPARATOR);
        sb.append(passwordHash);
        sb.append(SEPARATOR);
        sb.append(tool);
        // Log admin-keyfile content without EOL.
        LOGGER.log(Level.INFO, "Writting admin-keyfile: {0}", sb.toString());
        sb.append(EOL);
        Writer out = null;     
        try {
            out = new FileWriter(adminKeyFile);
            out.write(sb.toString());
        } catch (IOException ioe) {
            success = false;
            LOGGER.log(Level.INFO, "Caught IOException when writting {0}: {1}",
                    new Object[] {adminKeyFile, ioe.getMessage()});
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    success = false;
                    LOGGER.log(Level.INFO,
                            "Cannot close {0} Writer", adminKeyFile);
                }
            }
        }
        return success;
    }

    /**
     * Does Payara administrator password hash contains <code>RESET</code>
     * <code>String</code>?
     * <p/>
     * @return Value of <code>true</code> when password hash does contain 
     *         <code>RESET</code> <code>String</code> or <code>false</code>
     *         otherwise.
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * Update password hash using supplied password <code>String</code>.
     * <p/>
     * @param password New password to be set.
     */
    public boolean setPassword(final String password) {
        boolean success = true;
        byte[] passwordBytes = null;
        MessageDigest md = null;
        try {
            passwordBytes = password.getBytes(PASSWORD_ENCODING);
            md = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (UnsupportedEncodingException uee) {
            success = false;
            LOGGER.log(Level.INFO,
                    "Cannot convert password to bytes array: {0}",
                    uee.getMessage());
        } catch (NoSuchAlgorithmException nae) {
            success = false;
            LOGGER.log(Level.INFO,
                    "Cannot initialize message digest to produce {0}: {1}",
                    new Object[] {HASH_ALGORITHM, nae.getMessage()});
        }
        if (success && passwordBytes != null && md != null) {
            Base64.Encoder b64Enc = Base64.getEncoder();
            md.reset();
            md.update(passwordBytes);
            String b64Hash = b64Enc.encodeToString(md.digest());
            if (b64Hash != null) {
                StringBuilder sb = new StringBuilder(
                        1 + HASH_ALGORITHM_GALSSFISH.length()
                        + 1 + b64Hash.length());
                sb.append(HASH_ALGORITHM_PREFIX);
                sb.append(HASH_ALGORITHM_GALSSFISH);
                sb.append(HASH_ALGORITHM_SUFFIX);
                sb.append(b64Hash);
                passwordHash = sb.toString();
            } else {
                success = false;
            }
        }
        return success;
    }

}
