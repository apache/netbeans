/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
