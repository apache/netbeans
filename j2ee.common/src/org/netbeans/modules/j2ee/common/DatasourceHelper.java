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

package org.netbeans.modules.j2ee.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 * An utility class for working with data sources.
 *
 * @author Andrei Badea
 *
 * @since 1.7
 */
public class DatasourceHelper {

    private DatasourceHelper() {
    }

    /**
     * Finds the database connections whose database URL and user name equal
     * the database URL and the user name of the passed data source.
     *
     * @param  datasource the data source.
     *
     * @return the list of database connections; never null.
     *
     * @throws NullPointerException if the datasource parameter was null.
     */
    public static List<DatabaseConnection> findDatabaseConnections(Datasource datasource) {
        if (datasource == null) {
            throw new NullPointerException("The datasource parameter cannot be null."); // NOI18N
        }
        String databaseUrl = datasource.getUrl();
        String user = datasource.getUsername();
        if (databaseUrl == null || user == null) {
            return Collections.emptyList();
        }
        List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();
        for (DatabaseConnection dbconn : ConnectionManager.getDefault().getConnections()) {
            if (databaseUrl.equals(dbconn.getDatabaseURL()) && user.equals(dbconn.getUser())) {
                result.add(dbconn);
            }
        }
        if (result.size() > 0) {
            return Collections.unmodifiableList(result);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Finds the data source with the given JNDI name in the module and
     * project data sources of the given provider.
     *
     * @param  provider the {@link J2eeModuleProvider provider} whose data sources 
     *         are to be searched; cannot be null.
     * @param  jndiName the JNDI name to search for; cannot be null.
     *
     * @return the found data source or null if no data source was found.
     *
     * @throws NullPointerException if either the <code>provider</code>
     *         or the <code>jndiName</code> parameter was null.
     *
     * @since 1.11
     */
    public static Datasource findDatasource(J2eeModuleProvider provider, String jndiName) throws ConfigurationException {
        if (provider == null) {
            throw new NullPointerException("The provider parameter cannot be null."); // NOI18N
        }
        if (jndiName == null) {
            throw new NullPointerException("The jndiName parameter cannot be null."); // NOI18N
        }
        for (Datasource datasource : provider.getServerDatasources()) {
            if (jndiName.equals(datasource.getJndiName())) {
                return datasource;
            }
        }
        for (Datasource datasource : provider.getModuleDatasources()) {
            if (jndiName.equals(datasource.getJndiName())) {
                return datasource;
            }
        }
        return null;
    }
}
