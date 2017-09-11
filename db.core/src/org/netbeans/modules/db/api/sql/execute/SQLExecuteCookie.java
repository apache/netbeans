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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.api.sql.execute;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.nodes.Node;

/**
 * Cookie for executing SQL files.
 *
 * <p>This interface allows a client to execute the SQL statement(s)
 * contained in the implementing object (currently the
 * DataObject for SQL files). Therefore calling
 * the {@link #execute} method will execute the statement(s) contained
 * in the respective file and display the results.</p>
 *
 * @author Andrei Badea
 */
public interface SQLExecuteCookie extends Node.Cookie {

    // XXX this should not be a cookie, just a plain interface;
    // will be fixed when lookups are added to DataObjects

    /**
     * Call this set the current database connection for this cookie.
     * The database connection will be used by the {@link #execute} method.
     */
    public void setDatabaseConnection(DatabaseConnection dbconn);

    /** Allow to set database connection.
     * The database connection will be used by the {@link #execute} method.
     *
     * @return a database connection or null
     * @since 1.10
     */
    public DatabaseConnection getDatabaseConnection();

    /**
     * Call this to execute the statements in the object implementing the
     * cookie and display them in the result window.
     */
    public void execute();
}
