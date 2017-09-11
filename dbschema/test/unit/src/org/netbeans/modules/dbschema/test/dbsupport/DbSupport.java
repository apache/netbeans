/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dbschema.test.dbsupport;

import java.sql.Connection;
import java.util.Collection;

/**
 *
 * @author David
 */
public abstract class DbSupport {
    public enum VENDOR { MYSQL, JAVADB, POSTGRES, UNKNOWN };

    public enum FEATURE { SEQUENCE, AUTOINCREMENT } ;

    public static VENDOR getVendor(String driverClass) {
        if ( "com.mysql.jdbc.Driver".equals(driverClass)) {
            return VENDOR.MYSQL;
        } else if ("org.apache.derby.jdbc.ClientDriver".equals(driverClass)) {
            return VENDOR.JAVADB;
        } else if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driverClass)) {
            return VENDOR.JAVADB;
        } else if ("org.postgresql.Driver".equals(driverClass)) {
            return VENDOR.POSTGRES;
        } else {
            return VENDOR.UNKNOWN;
        }
    }
    public static DbSupport getInstance(String driverClass) {
        if ( "com.mysql.jdbc.Driver".equals(driverClass)) {
            return MySQLDBSupport.getInstance();
        } else if ("org.apache.derby.jdbc.ClientDriver".equals(driverClass)) {
            return JavaDbSupport.getInstance();
        } else if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driverClass)) {
            return JavaDbSupport.getInstance();
        } else if ("org.postgresql.Driver".equals(driverClass)) {
            return PostgresDbSupport.getInstance();
        } else {
            throw new RuntimeException("No support for database with driver class" + driverClass);
        }
    }

    public boolean supportsFeature(FEATURE feature) {
        return getSupportedFeatures().contains(feature);
    }

    protected abstract Collection<FEATURE> getSupportedFeatures();

    public abstract void createAITable(Connection conn, String tableName, String columnName) throws Exception;

    public abstract void createSequenceTable(Connection conn, String tableName, String columnName) throws Exception;

}
