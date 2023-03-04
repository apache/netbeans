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

package org.netbeans.modules.derby.api;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.derby.DerbyDatabasesImpl;

/**
 *
 * @author Andrei Badea
 *
 * @since 1.7
 */
public final class DerbyDatabases {
    private static final DerbyDatabasesImpl IMPL = DerbyDatabasesImpl.getDefault();

    private DerbyDatabases() {}

    /**
     * Checks if the Derby database is registered and the Derby system
     * home is set.
     *
     * @return true if Derby is registered, false otherwise.
     */
    public static boolean isDerbyRegistered() {
        return IMPL.isDerbyRegistered();
    }
    
    /**
     * Returns the Derby system home.
     *
     * @return the Derby system home or null if it is not known.
     */
    public static File getSystemHome() {
        return IMPL.getSystemHome();
    }

    /**
     * Checks if the given database exists in the Derby system home.
     *
     * @return true if the database exists, false otherwise.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public static boolean databaseExists(String databaseName) {
        return IMPL.databaseExists(databaseName);
    }

    /**
     * Returns the first free database name using the specified base name.
     * The method attempts to create a database name by appending numbers to
     * the base name, like in "base1", "base2", etc. and returns the
     * first free name found.
     *
     * @return a database name or null if a free database name could not be found.
     *
     * @throws NullPointerException in the <code>baseDatabaseName</code> parameter
     *         could not be found.
     */
    public static String getFirstFreeDatabaseName(String baseDatabaseName) {
        return IMPL.getFirstFreeDatabaseName(baseDatabaseName);
    }

    /**
     * Returns the code point of the first illegal character in the given database
     * name.
     *
     * @return the code point of the first illegal character or -1 if all characters
     *         are valid.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public static int getFirstIllegalCharacter(String databaseName) {
        return IMPL.getFirstIllegalCharacter(databaseName);
    }

    /**
     * Creates a new empty database in the Derby system and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @param  databaseName the name of the database to created; cannot be nul.
     * @param  user the user to set up authentication for. No authentication
     *         will be set up if <code>user</code> is null or an empty string.
     * @param  password the password for authentication.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createDatabase(String databaseName, String user, String password) throws DatabaseException, IOException, IllegalStateException {
        return IMPL.createDatabase(databaseName, user, password);
    }

    /**
     * Creates the sample database in the Derby system home
     * using the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. If the sample database already exists
     * it is just registered.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createSampleDatabase() throws DatabaseException, IOException, IllegalStateException {
        return IMPL.createSampleDatabase();
    }

    /**
     * Creates the sample database in the Derby system home using the
     * given database name and the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while registering
     *         the new database in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createSampleDatabase(String databaseName) throws DatabaseException, IOException {
        return IMPL.createSampleDatabase(databaseName, false);
    }

}
