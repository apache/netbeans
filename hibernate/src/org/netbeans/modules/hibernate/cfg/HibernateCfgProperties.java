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

package org.netbeans.modules.hibernate.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.cfg.Environment;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.internal.classic.ClassicQueryTranslatorFactory;
import org.openide.util.NbBundle;

/**
 * This class contains all the properties in the Hibernate configuration file
 *
 * @author Dongmei Cao
 */
public class HibernateCfgProperties {
    
    public static final String[] dialects = new String[]{
        "org.hibernate.dialect.DB2Dialect", //NOI18N
        "org.hibernate.dialect.DB2390Dialect", //NOI18N
        "org.hibernate.dialect.DB2400Dialect", //NOI18N
        "org.hibernate.dialect.DerbyDialect", //NOI18N
        "org.hibernate.dialect.FirebirdDialect", //NOI18N
        "org.hibernate.dialect.FrontbaseDialect", //NOI18N
        "org.hibernate.dialect.HSQLDialect", //NOI18N
        "org.hibernate.dialect.InformixDialect", //NOI18N
        "org.hibernate.dialect.IngresDialect", //NOI18N
        "org.hibernate.dialect.InterbaseDialect", //NOI18N
        "org.hibernate.dialect.MckoiDialect", //NOI18N
        "org.hibernate.dialect.MySQLDialect", //NOI18N
        "org.hibernate.dialect.MySQLInnoDBDialect", //NOI18N
        "org.hibernate.dialect.MySQLMyISAMDialect", //NOI18N
        "org.hibernate.dialect.OracleDialect", //NOI18N
        "org.hibernate.dialect.Oracle9Dialect", //NOI18N
        "org.hibernate.dialect.PointbaseDialect", //NOI18N
        "org.hibernate.dialect.PostgreSQLDialect", //NOI18N
        "org.hibernate.dialect.ProgressDialect", //NOI18N
        "org.hibernate.dialect.SAPDBDialect", //NOI18N
        "org.hibernate.dialect.SQLServerDialect", //NOI18N
        "org.hibernate.dialect.SybaseDialect", //NOI18N
        "org.hibernate.dialect.SybaseAnywhereDialect" //NOI18N
    };
    
    public static String[] driverClassess = new String[]{
        "com.ibm.db2.jdbc.app.DB2Driver", //NOI18N
        "com.informix.jdbc.IfxDriver", //NOI18N
        "com.mckoi.JDBCDriver", //NOI18N
        "com.mysql.jdbc.Driver", //NOI18N
        "com.pointbase.jdbc.jdbcUniversalDriver", //NOI18N
        "com.sun.sql.jdbc.sqlserver.SQLServerDriver", //NOI18N
        "com.sun.sql.jdbc.sybase.SybaseDriver", //NOI18N
        "interbase.interclient.Driver", //NOI18N
        "oracle.jdbc.driver.OracleDriver", //NOI18N
        "oracle.jdbc.OracleDriver", //NOI18N
        "org.apache.derby.jdbc.ClientDriver", //NOI18N
        "org.firebirdsql.jdbc.FBDriver", //NOI18N
        "org.hsqldb.jdbcDriver", //NOI18N
        "org.postgresql.Driver" //NOI18N
    };

    public final static String[] jdbcProps = new String[] {
        Environment.DRIVER,
        Environment.URL,
        Environment.USER,
        Environment.PASS,
        Environment.POOL_SIZE
    };

    public final static String[] datasourceProps = new String[] {
        Environment.DATASOURCE,
        Environment.JNDI_URL,
        Environment.JNDI_CLASS,
        Environment.USER,
        Environment.PASS
    };

    public final static String[] optionalConfigProps = new String[] {
        Environment.DIALECT,
        Environment.SHOW_SQL,
        Environment.FORMAT_SQL,
        Environment.DEFAULT_SCHEMA,
        Environment.DEFAULT_CATALOG,
        Environment.SESSION_FACTORY_NAME,
        Environment.MAX_FETCH_DEPTH,
        Environment.DEFAULT_BATCH_FETCH_SIZE,
        Environment.DEFAULT_ENTITY_MODE,
        Environment.ORDER_UPDATES,
        Environment.GENERATE_STATISTICS,
        Environment.USE_IDENTIFIER_ROLLBACK,
        Environment.USE_SQL_COMMENTS
    };

    public final static String[] optionalJdbcConnProps = new String[] {
        Environment.STATEMENT_FETCH_SIZE,
        Environment.STATEMENT_BATCH_SIZE,
        Environment.BATCH_VERSIONED_DATA,
        Environment.BATCH_STRATEGY,
        Environment.USE_SCROLLABLE_RESULTSET,
        Environment.USE_STREAMS_FOR_BINARY,
        Environment.USE_GET_GENERATED_KEYS,
        Environment.CONNECTION_PROVIDER,
        Environment.ISOLATION,
        Environment.AUTOCOMMIT,
        Environment.RELEASE_CONNECTIONS
    };

    public final static String[] optionalCacheProps = new String[] {
//        Environment.CACHE_PROVIDER,
        Environment.USE_MINIMAL_PUTS,
        Environment.USE_QUERY_CACHE,
        Environment.USE_SECOND_LEVEL_CACHE,
        Environment.QUERY_CACHE_FACTORY,
        Environment.CACHE_REGION_PREFIX,
        Environment.USE_STRUCTURED_CACHE
    };

    public final static String[] optionalTransactionProps = new String[] {
        Environment.TRANSACTION_STRATEGY,
//        Environment.USER_TRANSACTION,
//        Environment.TRANSACTION_MANAGER_STRATEGY,
        Environment.FLUSH_BEFORE_COMPLETION,
        Environment.AUTO_CLOSE_SESSION
    };

    public final static String[] optionalMiscProps = new String[] {
        Environment.CURRENT_SESSION_CONTEXT_CLASS,
        Environment.QUERY_TRANSLATOR,
        Environment.QUERY_SUBSTITUTIONS,
        Environment.HBM2DDL_AUTO,
        Environment.USE_REFLECTION_OPTIMIZER
    };

    // A map to hint the possible values for all the Hibernate properties:
    // null for no hint
    // String[] for selecting one of the values
    private final static String[] TRUE_FALSE = new String[] {"true", "false" }; // NOI18N
    private final static String[] RELEASE_MODES = new String[] {"auto", "on_close", "after_transaction", "after_statement"}; // NOI18N
    private final static String[] SESSION_CONTEXT = new String[] {"jta", "thread", "managed", NbBundle.getMessage(HibernateCfgProperties.class, "LBL_CustomClass")}; // NOI18N
    private final static String[] QUERY_FACTORY_CLASS = new String[] {ASTQueryTranslatorFactory.class.getName(),
        ClassicQueryTranslatorFactory.class.getName()}; // NOI18N
    private final static String[] HBM2DDL_AUTO = new String[] {"validate", "update", "create", "create-drop" }; // NOI18N

    private static Map<String, Object> possiblePropertyValues = new HashMap<String, Object>();
    static {
        possiblePropertyValues.put(Environment.DRIVER, driverClassess);
        possiblePropertyValues.put(Environment.URL, null);
        possiblePropertyValues.put(Environment.USER, null);
        possiblePropertyValues.put(Environment.PASS, null);
        possiblePropertyValues.put(Environment.POOL_SIZE, null);

        possiblePropertyValues.put(Environment.DATASOURCE, null);
        possiblePropertyValues.put(Environment.JNDI_URL, null);
        possiblePropertyValues.put(Environment.JNDI_CLASS, null);
        possiblePropertyValues.put(Environment.USER, null);
        possiblePropertyValues.put(Environment.PASS, null);

        possiblePropertyValues.put(Environment.DIALECT, dialects);
        possiblePropertyValues.put(Environment.SHOW_SQL, TRUE_FALSE);
        possiblePropertyValues.put(Environment.FORMAT_SQL, TRUE_FALSE);
        possiblePropertyValues.put(Environment.DEFAULT_SCHEMA, null);
        possiblePropertyValues.put(Environment.DEFAULT_CATALOG, null);
        possiblePropertyValues.put(Environment.SESSION_FACTORY_NAME, null);
        possiblePropertyValues.put(Environment.MAX_FETCH_DEPTH, null);
        possiblePropertyValues.put(Environment.DEFAULT_BATCH_FETCH_SIZE, null);
        possiblePropertyValues.put(Environment.DEFAULT_ENTITY_MODE, null);
        possiblePropertyValues.put(Environment.ORDER_UPDATES, TRUE_FALSE);
        possiblePropertyValues.put(Environment.GENERATE_STATISTICS, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_IDENTIFIER_ROLLBACK, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_SQL_COMMENTS, TRUE_FALSE);

        possiblePropertyValues.put(Environment.STATEMENT_FETCH_SIZE, null);
        possiblePropertyValues.put(Environment.STATEMENT_BATCH_SIZE, null);
        possiblePropertyValues.put(Environment.BATCH_VERSIONED_DATA, TRUE_FALSE);
        possiblePropertyValues.put(Environment.BATCH_STRATEGY, null );
        possiblePropertyValues.put(Environment.USE_SCROLLABLE_RESULTSET, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_STREAMS_FOR_BINARY, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_GET_GENERATED_KEYS, TRUE_FALSE);
        possiblePropertyValues.put(Environment.CONNECTION_PROVIDER, null);
        possiblePropertyValues.put(Environment.ISOLATION, null);
        possiblePropertyValues.put(Environment.AUTOCOMMIT, TRUE_FALSE);
        possiblePropertyValues.put(Environment.RELEASE_CONNECTIONS, RELEASE_MODES );

//        possiblePropertyValues.put(Environment.CACHE_PROVIDER, null);
        possiblePropertyValues.put(Environment.USE_MINIMAL_PUTS, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_QUERY_CACHE, TRUE_FALSE);
        possiblePropertyValues.put(Environment.USE_SECOND_LEVEL_CACHE, TRUE_FALSE);
        possiblePropertyValues.put(Environment.QUERY_CACHE_FACTORY, null);
        possiblePropertyValues.put(Environment.CACHE_REGION_PREFIX, null);
        possiblePropertyValues.put(Environment.USE_STRUCTURED_CACHE, TRUE_FALSE);

        possiblePropertyValues.put(Environment.TRANSACTION_STRATEGY, null);
//        possiblePropertyValues.put(Environment.USER_TRANSACTION, null);
//        possiblePropertyValues.put(Environment.TRANSACTION_MANAGER_STRATEGY, null);
        possiblePropertyValues.put(Environment.FLUSH_BEFORE_COMPLETION, TRUE_FALSE);
        possiblePropertyValues.put(Environment.AUTO_CLOSE_SESSION, TRUE_FALSE);

        possiblePropertyValues.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, SESSION_CONTEXT);
        possiblePropertyValues.put(Environment.QUERY_TRANSLATOR, QUERY_FACTORY_CLASS);
        possiblePropertyValues.put(Environment.QUERY_SUBSTITUTIONS, null);
        possiblePropertyValues.put(Environment.HBM2DDL_AUTO, HBM2DDL_AUTO);
        possiblePropertyValues.put(Environment.USE_REFLECTION_OPTIMIZER, TRUE_FALSE);
    };

    public static Object  getPossiblePropertyValue( String propName ) {
        return possiblePropertyValues.get( propName );
    }
}
