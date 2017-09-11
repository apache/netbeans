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
import java.util.*;
import org.netbeans.lib.ddl.*;

/**
* Interface for commands.
* DatabaseSpecification instances keeps information about used database type (object was
* created using factory's method createSpecification()) and used connection info.
* It should be used as factory for DDLCommands.
* It also converts java classes into native database types and vice versa.
*
* @author Slavek Psenicka
*/
public interface DatabaseSpecification {

    /** Returns database metadata */
    public DatabaseMetaData getMetaData() throws SQLException;

    public String getMetaDataAdaptorClassName();
    public void setMetaDataAdaptorClassName(String name);

    /** Returns used connection */
    public DBConnection getConnection();

    /** Creates and returns java.sql.Connection object */
    public Connection openJDBCConnection() throws DDLException;

    /** Returns java.sql.Connection, if present and open */
    public Connection getJDBCConnection();

    /** Returns factory */
    public DatabaseSpecificationFactory getSpecificationFactory();

    /** Sets factory */
    public void setSpecificationFactory(DatabaseSpecificationFactory fac);

    /** Closes the connection.
    * If you forget to close the connection, next open should throw
    * DDLException. This is an internal dummy-trap.
    */
    public void closeJDBCConnection() throws DDLException;

    /** Returns all database properties.
    * It contains all command properties. Used to obtain settings independent
    * on commands.
    */
    public Map getProperties();

    /** Returns properties of command.
    * This description should be used for formatting commands, it contains
    * available information for DatabaseSpecification. 
    * @param command Name of command. 
    */
    public Map getCommandProperties(String command);

    /** Creates command identified by commandName. Command names will include
    * create/rename/drop table/view/index/column and comment table/column. It 
    * returns null if command specified by commandName was not found. Used 
    * system allows developers to extend db-specification files and simply 
    * address new commands (everybody can implement createXXXCommand()).
    * @param command Name of command. 
    */
    public DDLCommand createCommand(String commandName) throws CommandNotSupportedException;

    /** Returns DBType where maps specified java type.
    */
    public String getType(int sqltype);
}

/*
* <<Log>>
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/13/99  Slavek Psenicka 
*  1    Gandalf   1.0         9/10/99  Slavek Psenicka 
* $
*/
