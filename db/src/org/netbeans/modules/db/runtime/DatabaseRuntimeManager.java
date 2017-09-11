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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.db.runtime;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * This class managers the list of registered database runtimes. Database runtimes
 * encapsulate instances of a database server which can be automatically started 
 * by the IDE when a connection is being made to this server.
 *
 * @see org.netbeans.spi.db.explorer.DatabaseRuntime
 *
 * @author Nam Nguyen, Andrei Badea
 */
public final class DatabaseRuntimeManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseRuntimeManager.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    /**
     * The path where the runtimes are registered in the SystemFileSystem.
     */
    private static final String RUNTIMES_PATH = "Databases/Runtimes"; // NOI18N
    
    /**
     * The singleton database runtime manager instance.
     */
    private static DatabaseRuntimeManager DEFAULT = null;
    
    /**
     * The Lookup.Result instance containing all the DatabaseRuntime instances.
     */
    private Lookup.Result<DatabaseRuntime> result = getLookupResult();

    /**
     * Returns the singleton database runtime manager instance.
     */
    public static synchronized DatabaseRuntimeManager getDefault() {
        if (DEFAULT == null) {
            LOGGER.finest("Instantiated DatabaseRuntimeManager.");
            DEFAULT = new DatabaseRuntimeManager();
        }
        return DEFAULT;
    }
    
    public DatabaseRuntime[] getRuntimes() {
        Collection<? extends DatabaseRuntime> runtimes = result.allInstances();
        return runtimes.toArray(new DatabaseRuntime[runtimes.size()]);
    }
    
    public static synchronized boolean isInstantiated() {
        LOGGER.finest("Is DatabaseRuntimeManager instantiated? " + (DEFAULT != null));
        return DEFAULT != null;
    }

    /**
     * Returns the runtimes registered for the specified JDBC driver.
     *
     * @param jdbcDriverClassName the JDBC driver to search for; must not be null.
     *
     * @return the runtime registered for the specified JDBC driver or null
     *         if no runtime is registered for this driver.
     *
     * @throws NullPointerException if the specified JDBC driver is null.
     */
    public DatabaseRuntime[] getRuntimes(String jdbcDriverClassName) {
        if (jdbcDriverClassName == null) {
            throw new NullPointerException();
        }
        List<DatabaseRuntime> runtimeList = new LinkedList<DatabaseRuntime>();
        for (DatabaseRuntime runtime : result.allInstances()) {
            if (LOG) {
                LOGGER.log(Level.FINE, "Runtime: " + runtime.getClass().getName() + " for driver " + runtime.getJDBCDriverClass()); // NOI18N
            }
            if (jdbcDriverClassName.equals(runtime.getJDBCDriverClass())) {
                runtimeList.add(runtime);
            }
        }
        return runtimeList.toArray(new DatabaseRuntime[runtimeList.size()]);
    }
    
    private synchronized Lookup.Result<DatabaseRuntime> getLookupResult() {
        return Lookups.forPath(RUNTIMES_PATH).lookupResult(DatabaseRuntime.class);
    }
}
