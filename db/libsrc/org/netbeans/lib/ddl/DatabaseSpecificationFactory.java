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

package org.netbeans.lib.ddl;

import java.sql.*;
import java.util.Set;
import org.netbeans.lib.ddl.*;

/**
* The factory interface used for creating instances of DatabaseSpecification class.
* DatabaseSpecificationFactory collects information about available database
* description files. Then it's able to specify if system can control
* the database (specified by product name or live connection). It also
* provides a list of supported databases.
*
* @author Slavek Psenicka
*/
public interface DatabaseSpecificationFactory {

    /** Returns array of database products supported by system. It returns
    * string array only, not the DatabaseSpecification array.
    */
    public Set supportedDatabases();

    /** Returns true if database (specified by databaseProductName) is
    * supported by system. Does not throw exception if it doesn't.
    * @param databaseProductName Database product name as given from DatabaseMetaData
    * @return True if database product is supported.
    */	
    public boolean isDatabaseSupported(String databaseProductName);

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to the database 
    * and reads database metadata. Throws DBException if database
    * (obtained from database metadata) is not supported.
    * @param connection Database connection used to obtain database product name
    * directly from the database.
    * @return Specification object.
    */
    public DatabaseSpecification createSpecification(DBConnection connection, Connection c)
    throws DatabaseProductNotFoundException, DDLException;

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to database and
    * reads metadata as createSpecification(DBConnection connection), but always
    * uses specified databaseProductName. This is not recommended technique.
    * @param connection Database connection (is NOT used to obtain database product name)
    * @return Specification object.
    */
    public DatabaseSpecification createSpecification(DBConnection connection, String databaseProductName, Connection c) throws DatabaseProductNotFoundException;

    public DatabaseSpecification createSpecification(Connection c)
    throws DatabaseProductNotFoundException, SQLException;
}

/*
* <<Log>>
*/
