/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee.db;

import org.netbeans.modules.glassfish.tooling.utils.StringPrefixTree;

/**
 * GlassFish JDBC connection pool server property content.
 * <p/>
 * @author Tomas Kraus
 */
public class JDBCConnectionPool {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Recognized properties in JDBC connection pool.
     */
    public enum PropertyType {

        /** Unknown JDBC connection pool property. */
        UNKNOWN,

        /** JDBC resource user name. */
        USER,

        /** JDBC resource password. */
        PASSWORD,

        /** JDBC resource driver class. */
        DRIVER_CLASS,

        /** JDBC resource URL. */
        URL,

        /** JDBC resource database name. */
        DATABASE_NAME,

        /** JDBC resource server host name. */
        SERVER_NAME,

        /** JDBC resource server port name. */
        PORT_NUMBER;

        /** A <code>String</code> representation of UNKNOWN value. */
        private static final String UNKNOWN_STR = "UNKNOWN";

        /** A <code>String</code> representation of USER value. */
        private static final String USER_STR = "User";

        /** A <code>String</code> representation of PASSWORD value. */
        private static final String PASSWORD_STR = "Password";

        /** A <code>String</code> representation of DRIVER_CLASS value. */
        private static final String DRIVER_CLASS_STR = "DriverClass";

        /** A <code>String</code> representation of URL value. */
        private static final String URL_STR = "URL";

        /** A <code>String</code> representation of DATABASE_NAME value. */
        private static final String DATABASE_NAME_STR = "DatabaseName";

        /** A <code>String</code> representation of SERVER_NAME value. */
        private static final String SERVER_NAME_STR = "ServerName";

        /** A <code>String</code> representation of PORT_NUMBER value. */
        private static final String PORT_NUMBER_STR = "PortNumber";

        /** Stored <code>String</code> values for backward <code>String</code>
         *  conversion. */
        private static final StringPrefixTree<PropertyType> stringValues
                = new StringPrefixTree<>(false);

        static {
            stringValues.add(USER.toString(), USER);
            stringValues.add(PASSWORD.toString(), PASSWORD);
            stringValues.add(DRIVER_CLASS.toString(), DRIVER_CLASS);
            stringValues.add(URL.toString(), URL);
            stringValues.add(DATABASE_NAME.toString(), DATABASE_NAME);
            stringValues.add(SERVER_NAME.toString(), SERVER_NAME);
            stringValues.add(PORT_NUMBER.toString(), PORT_NUMBER);
        }

        /**
         * Returns a <code>Properties</code> with a value represented
         * by the specified <code>String</code>.
         * <p/>
         * The <code>Properties</code> returned represents existing value
         * only if specified <code>String</code> matches any <code>String</code>
         * returned by <code>toString</code> method. Otherwise <code>null</code>
         * value is returned.
         * <p>
         * @param name Value containing <code>Properties</code>
         *             <code>toString</code> representation.
         * @return <code>Properties</code> value represented
         *         by <code>String</code> or <code>UNKNOWN</code> if value
         *         was not recognized.
         */
        public static PropertyType toValue(final String name) {
            if (name != null) {
                PropertyType type = stringValues.match(name.toLowerCase());
                return type != null ? type : UNKNOWN;
            } else {
                return null;
            }
        }

        /**
         * Convert <code>Properties</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch (this) {
                case UNKNOWN:       return UNKNOWN_STR;
                case USER:          return USER_STR;
                case PASSWORD:      return PASSWORD_STR;
                case DRIVER_CLASS:  return DRIVER_CLASS_STR;
                case URL:           return URL_STR;
                case DATABASE_NAME: return DATABASE_NAME_STR;
                case SERVER_NAME:   return SERVER_NAME_STR;
                case PORT_NUMBER:   return PORT_NUMBER_STR;
                // This is unrecheable. Being here means this class does not handle
                // all possible values correctly.
                default:
                    throw new IllegalStateException(
                            "Invalid ResourceType value");
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** User property value. */
    private String user;

    /** Password value. */
    private String password;

    /** Driver class value. */
    private String driverClass;

    /** Resource URL. */
    private String url;

    /** Database name. */
    private String databaseName;

    /** Server host name. */
    private String serverName;

    /** Server host port. Negative value (usually <code>-1</code>)
     *  represents <code>null</code>. */
    private int port;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of JDBC connection pool content with no values set.
     */
    public JDBCConnectionPool() {
        this.user = null;
        this.password = null;
        this.driverClass = null;
        this.url = null;
        this.databaseName = null;
        this.serverName = null;
        this.port = -1;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get user property value.
     * <p/>
     * @return User property value.
     */
    public String getUser() {
        return user;
    }

    /**
     * Get password property value.
     * <p/>
     * @return Password value.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get driver class property value.
     * <p/>
     * @return Driver class value.
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * Get resource URL property value.
     * <p/>
     * @return Resource URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get database name property value.
     * <p/>
     * @return Database name.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get server host name property value.
     * <p/>
     * @return Server host name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get server host port property value.
     * <p/>
     * Negative value (usually <code>-1</code>) represents <code>null</code>.
     * <p/>
     * @return Server host port.
     */
    public int getPort() {
        return port;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set property value depending on {@link Properties} type
     * <code>enum</code> value.
     * <p/>
     * @return Value of <code>true</code> if property was set
     *         or <code>false</code> otherwise (what means that
     *         {@link Properties} type value vas <code>UNKNOWN</code>.
     */
    public boolean setProperty(PropertyType type, String value) {
        switch (type) {
            case USER:
                this.user = value;
                return true;
            case PASSWORD:
                this.password = value;
                return true;
            case DRIVER_CLASS:
                this.driverClass = value;
                return true;
            case URL:
                this.url = value;
                return true;
            case DATABASE_NAME:
                this.databaseName = value;
                return true;
            case SERVER_NAME:
                this.serverName = value;
                return true;
            case PORT_NUMBER:
                try {
                    this.port = Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException nfe) {
                    this.port = -1;
                    return false;
                }
            default:
                return false;
        }
    }

}
