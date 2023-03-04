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

package org.netbeans.lib.ddl.impl;

import java.beans.Beans;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.adaptors.DatabaseMetaDataAdaptor;


public class Specification implements DatabaseSpecification {
    
    /** Used DBConnection */
    private HashMap desc;

    /** Used JDBC Connection */
    private Connection jdbccon;

    /** Owned factory */
    SpecificationFactory factory;

    /** Metadata adaptor */
    String adaptorClass;
    DatabaseMetaData dmdAdaptor;

    public static final String CREATE_TABLE = "CreateTableCommand";
    public static final String RENAME_TABLE = "RenameTableCommand";
    public static final String DROP_TABLE = "DropTableCommand";
    public static final String COMMENT_TABLE = "CommentTableCommand";
    public static final String ADD_COLUMN = "AddColumnCommand";
    public static final String MODIFY_COLUMN = "ModifyColumnCommand";
    public static final String RENAME_COLUMN = "RenameColumnCommand";
    public static final String REMOVE_COLUMN = "RemoveColumnCommand";
    public static final String CREATE_INDEX = "CreateIndexCommand";
    public static final String DROP_INDEX = "DropIndexCommand";
    public static final String ADD_CONSTRAINT = "AddConstraintCommand";
    public static final String DROP_CONSTRAINT = "DropConstraintCommand";
    public static final String CREATE_VIEW = "CreateViewCommand";
    public static final String RENAME_VIEW = "RenameViewCommand";
    public static final String COMMENT_VIEW = "CommentViewCommand";
    public static final String DROP_VIEW = "DropViewCommand";
    public static final String CREATE_PROCEDURE = "CreateProcedureCommand";
    public static final String DROP_PROCEDURE = "DropProcedureCommand";
    public static final String CREATE_FUNCTION = "CreateFunctionCommand";
    public static final String DROP_FUNCTION = "DropFunctionCommand";
    public static final String CREATE_TRIGGER = "CreateTriggerCommand";
    public static final String DROP_TRIGGER = "DropTriggerCommand";
    public static final String DEFAULT_DATABASE = "SetDefaultDatabaseCommand";
    public static final String DEFAULT_SCHEMA = "SetDefaultSchemaCommand";

    /** Constructor */
    public Specification(HashMap description)
    {
        desc = description;
    }

    /** Constructor */
    public Specification(HashMap description, Connection c)
    {
        desc = description;
        jdbccon = c;
    }

    /** Returns all database properties */
    @Override
    public Map getProperties()
    {
        return (Map)desc;
    }

    /** Returns command description */
    @Override
    public Map getCommandProperties(String command)
    {
        return (Map)desc.get(command);
    }

    /** Returns used connection */
    @Override
    public DBConnection getConnection()
    {
        return (DBConnection)desc.get("connection"); // NOI18N
    }

    @Override
    public DatabaseSpecificationFactory getSpecificationFactory()
    {
        return factory;
    }

    @Override
    public void setSpecificationFactory(DatabaseSpecificationFactory fac)
    {
        factory = (SpecificationFactory)fac;
    }

    @Override
    public String getMetaDataAdaptorClassName()
    {
        if (adaptorClass == null || adaptorClass.length() == 0) {
            adaptorClass = "org.netbeans.lib.ddl.adaptors.DefaultAdaptor"; // NOI18N
        }

        return adaptorClass;
    }

    @Override
    public void setMetaDataAdaptorClassName(String name)
    {
        if (name.startsWith("Database.Adaptors.")) // NOI18N
            adaptorClass = name;
        else
            adaptorClass = "Database.Adaptors."+name; // NOI18N
        //		System.out.println("Metadata adaptor class set = "+adaptorClass);
        dmdAdaptor = null;
    }

    /** Returns database metadata */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        try {

            if (dmdAdaptor == null) {
                if (jdbccon != null) {
                    String adc = getMetaDataAdaptorClassName();
                    if (adc != null) {
                        ClassLoader loader = Class.forName(adc).getClassLoader();
                        dmdAdaptor = (DatabaseMetaData) Beans.instantiate(loader, adc);
                        if (dmdAdaptor instanceof DatabaseMetaDataAdaptor) {
                            ((DatabaseMetaDataAdaptor)dmdAdaptor).setConnection(jdbccon);
                        } else throw new ClassNotFoundException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_AdaptorInterface")); //NOI18N
                    } else throw new ClassNotFoundException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_AdaptorUnspecClass")); //NOI18N
                }
            }

            return dmdAdaptor;

        } catch (Exception ex) {
            Logger.getLogger(Specification.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            throw new SQLException(ex.getMessage());
        }
    }

    /** Opens JDBC Connection.
    * This method usually calls command when it need to process something. 
    * But you can call it explicitly and leave connection open until last
    * command gets executed. Don't forget to close it.
    */
    @Override
    public Connection openJDBCConnection()
    throws DDLException
    {
        if (jdbccon != null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ConnOpen")); //NOI18N
        DBConnection dbcon = getConnection();
        if (dbcon == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ConnNot")); //NOI18N
        try {
            jdbccon = dbcon.createJDBCConnection();
        } catch (Exception e) {
            throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ConnNot"));
        }

        return jdbccon;
    }

    /** Returns JDBC connection.
    * Commands must test if the connection is not open yet; if you simply call
    * openJDBCConnection without test (and the connection will be open by user),
    * a DDLException throws. This is a self-checking mechanism; you must always
    * close used connection.
    */ 
    @Override
    public Connection getJDBCConnection()
    {
        return jdbccon;
    }

    @Override
    public void closeJDBCConnection()
    throws DDLException
    {
        if (jdbccon == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ConnNot")); //NOI18n
        try {
            jdbccon.close();
            jdbccon = null;
        } catch (SQLException e) {
            throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ConnUnableClose")); //NOI18N
        }
    }

    /** Creates command identified by commandName. Command names will include
    * create/rename/drop table/view/index/column and comment table/column. It 
    * returns null if command specified by commandName was not found. Used 
    * system allows developers to extend db-specification files and simply 
    * address new commands (everybody can implement createXXXCommand()).
    */
    @Override
    public DDLCommand createCommand(String commandName)
    throws CommandNotSupportedException
    {
        return createCommand(commandName, null);
    }

    /** Creates command identified by commandName on table tableName.
    * Returns null if command specified by commandName was not found. It does not
    * check tableName existency; it simply waits for relevant execute() command
    * which fires SQLException.
    */	
    public DDLCommand createCommand(String commandName, String tableName)
    throws CommandNotSupportedException
    {
        String classname;
        Class cmdclass;
        AbstractCommand cmd;
        HashMap cprops = (HashMap)desc.get(commandName);
        if (cprops != null) classname = (String)cprops.get("Class"); // NOI18N
        //else throw new CommandNotSupportedException(commandName, "command "+commandName+" is not supported by system");
        else throw new CommandNotSupportedException(commandName,
            MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_CommandNotSupported"), commandName)); // NOI18N
        try {
            cmdclass = Class.forName(classname);
            cmd = (AbstractCommand)cmdclass.newInstance();
        } catch (Exception e) {
            throw new CommandNotSupportedException(commandName,
                MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableFindOrInitCommand"), classname, commandName, e.getMessage())); // NOI18N
        }

        cmd.setObjectName(tableName);
        cmd.setSpecification(this);
        cmd.setFormat((String)cprops.get("Format")); // NOI18N
        return cmd;
    }

    /** Create table command
    * @param tableName Name of the table
    */
    public CreateTable createCommandCreateTable(String tableName)
    throws CommandNotSupportedException
    {
        return (CreateTable)createCommand(CREATE_TABLE, tableName);
    }

    /** Comment table command
    * @param tableName Name of the table
    * @param comment New comment
    */
    public CommentTable createCommandCommentTable(String tableName, String comment)
    throws CommandNotSupportedException
    {
        CommentTable cmd = (CommentTable)createCommand(COMMENT_TABLE, tableName);
        cmd.setComment(comment);
        return cmd;
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public AbstractCommand createCommandDropTable(String tableName)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_TABLE, tableName);
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public RenameTable createCommandRenameTable(String tableName, String newName)
    throws CommandNotSupportedException
    {
        RenameTable cmd = (RenameTable)createCommand(RENAME_TABLE, tableName);
        cmd.setNewName(newName);
        return cmd;
    }

    /** Add column */
    public AddColumn createCommandAddColumn(String tableName)
    throws CommandNotSupportedException
    {
        return (AddColumn)createCommand(ADD_COLUMN, tableName);
    }

    /** Modify column */
    public ModifyColumn createCommandModifyColumn(String tableName)
    throws CommandNotSupportedException
    {
        ModifyColumn cmd = (ModifyColumn)createCommand(MODIFY_COLUMN, tableName);
        return cmd;
    }

    /** Rename column */
    public RenameColumn createCommandRenameColumn(String tableName)
    throws CommandNotSupportedException
    {
        RenameColumn cmd = (RenameColumn)createCommand(RENAME_COLUMN, tableName);
        return cmd;
    }

    /** Remove column
    * @param tableName Name of the table
    */
    public RemoveColumn createCommandRemoveColumn(String tableName)
    throws CommandNotSupportedException
    {
        RemoveColumn rcol = (RemoveColumn)createCommand(REMOVE_COLUMN, tableName);
        return rcol;
    }

    /** Create index
    * @param indexName Name of index
    * @param tableName Name of the table
    */
    public CreateIndex createCommandCreateIndex(String tableName)
    throws CommandNotSupportedException
    {
        CreateIndex cicmd = (CreateIndex)createCommand(CREATE_INDEX, tableName);
        return cicmd;
    }

    /** Drop index
    * @param indexName Name of index
    */
    public DropIndex createCommandDropIndex(String tablename)
    throws CommandNotSupportedException
    {
        DropIndex dcmd = (DropIndex)createCommand(DROP_INDEX, tablename);
        return dcmd;
    }

    /** Create view
    * @param viewname Name of index
    */
    public CreateView createCommandCreateView(String viewname)
    throws CommandNotSupportedException
    {
        return (CreateView)createCommand(CREATE_VIEW, viewname);
    }

    /** Drop table command
    * @param tableName Name of the table
    */
    public RenameView createCommandRenameView(String tableName, String newName)
    throws CommandNotSupportedException
    {
        RenameView cmd = (RenameView)createCommand(RENAME_VIEW, tableName);
        cmd.setNewName(newName);
        return cmd;
    }

    /** Comment view command
    * @param tableName Name of the view
    * @param comment New comment
    */
    public CommentView createCommandCommentView(String viewName, String comment)
    throws CommandNotSupportedException
    {
        CommentView cmd = (CommentView)createCommand(COMMENT_VIEW, viewName);
        cmd.setComment(comment);
        return cmd;
    }

    /** Drop view
    * @param viewname Name of index
    */
    public AbstractCommand createCommandDropView(String viewname)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_VIEW, viewname);
    }

    /** Create procedure
    * @param viewname Name of procedure
    */
    public CreateProcedure createCommandCreateProcedure(String name)
    throws CommandNotSupportedException
    {
        return (CreateProcedure)createCommand(CREATE_PROCEDURE, name);
    }

    /** Drop procedure
    * @param viewname Name of procedure
    */
    public AbstractCommand createCommandDropProcedure(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_PROCEDURE, name);
    }

    /** Create function
    * @param viewname Name of function
    */
    public CreateFunction createCommandCreateFunction(String name)
    throws CommandNotSupportedException
    {
        return (CreateFunction)createCommand(CREATE_FUNCTION, name);
    }

    /** Drop function
    * @param viewname Name of function
    */
    public AbstractCommand createCommandDropFunction(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_FUNCTION, name);
    }

    /** Create trigger
    * @param viewname Name of trigger
    */
    public CreateTrigger createCommandCreateTrigger(String name, String tablename, int timing)
    throws CommandNotSupportedException
    {
        CreateTrigger ctrig = (CreateTrigger)createCommand(CREATE_TRIGGER, name);
        ctrig.setTableName(tablename);
        ctrig.setTiming(timing);
        return ctrig;
    }

    /** Drop trigger
    * @param viewname Name of trigger
    */
    public AbstractCommand createCommandDropTrigger(String name)
    throws CommandNotSupportedException
    {
        return (AbstractCommand)createCommand(DROP_TRIGGER, name);
    }

    public SetDefaultDatabase createSetDefaultDatabase(String dbname)
        throws CommandNotSupportedException {
        SetDefaultDatabase dcmd = (SetDefaultDatabase)createCommand(DEFAULT_DATABASE);
        dcmd.setDatabase(dbname);
        dcmd.setObjectName(dbname);
        return dcmd;
    }

    public SetDefaultSchema createSetDefaultSchema(String schemaName)
        throws CommandNotSupportedException {
        SetDefaultSchema dcmd = (SetDefaultSchema)createCommand(DEFAULT_SCHEMA);
        dcmd.setSchema(schemaName);
        dcmd.setObjectName(schemaName);
        return dcmd;
    }

    /** Returns type map */
    public Map getTypeMap()
    {
        return (Map)desc.get("TypeMap"); // NOI18N
    }

    /** Returns DBType where maps specified java type */
    @Override
    public String getType(int type)
    {
        String typestr = "";
        String ret;
        Map typemap = getTypeMap();

        switch(type) {
        case java.sql.Types.ARRAY: typestr = "ARRAY"; break; // NOI18N
        case java.sql.Types.BIGINT: typestr = "BIGINT"; break; // NOI18N
        case java.sql.Types.BINARY: typestr = "BINARY"; break; // NOI18N
        case java.sql.Types.BIT: typestr = "BIT"; break; // NOI18N
        case java.sql.Types.BLOB: typestr = "BLOB"; break; // NOI18N
        case java.sql.Types.BOOLEAN: typestr = "BOOLEAN"; break; // NOI18N
        case java.sql.Types.CHAR: typestr = "CHAR"; break; // NOI18N
        case java.sql.Types.CLOB: typestr = "CLOB"; break; // NOI18N
        case java.sql.Types.DATE: typestr = "DATE"; break; // NOI18N
        case java.sql.Types.DECIMAL: typestr = "DECIMAL"; break; // NOI18N
        case java.sql.Types.DISTINCT: typestr = "DISTINCT"; break; // NOI18N
        case java.sql.Types.DOUBLE: typestr = "DOUBLE"; break; // NOI18N
        case java.sql.Types.FLOAT: typestr = "FLOAT"; break; // NOI18N
        case java.sql.Types.INTEGER: typestr = "INTEGER"; break; // NOI18N
        case java.sql.Types.JAVA_OBJECT: typestr = "JAVA_OBJECT"; break; // NOI18N
        case java.sql.Types.LONGVARBINARY: typestr = "LONGVARBINARY"; break; // NOI18N
        case java.sql.Types.LONGNVARCHAR: typestr = "LONGNVARCHAR"; break; // NOI18N
        case java.sql.Types.LONGVARCHAR: typestr = "LONGVARCHAR"; break; // NOI18N
        case java.sql.Types.NUMERIC: typestr = "NUMERIC"; break; // NOI18N
        case java.sql.Types.NCHAR: typestr = "NCHAR"; break; // NOI18N
        case java.sql.Types.NCLOB: typestr = "NCLOB"; break; // NOI18N
        case java.sql.Types.NULL: typestr = "NULL"; break; // NOI18N
        case java.sql.Types.NVARCHAR: typestr = "NVARCHAR"; break; // NOI18N
        case java.sql.Types.OTHER: typestr = "OTHER"; break; // NOI18N        
        case java.sql.Types.REAL: typestr = "REAL"; break; // NOI18N
        case java.sql.Types.REF: typestr = "REF"; break; // NOI18N
        case java.sql.Types.ROWID: typestr = "ROWID"; break; // NOI18N
        case java.sql.Types.SMALLINT: typestr = "SMALLINT"; break; // NOI18N
        case java.sql.Types.SQLXML: typestr = "SQLXML"; break; // NOI18N
        case java.sql.Types.TIME: typestr = "TIME"; break; // NOI18N
        case java.sql.Types.TIMESTAMP: typestr = "TIMESTAMP"; break; // NOI18N
        case java.sql.Types.TINYINT: typestr = "TINYINT"; break; // NOI18N
        case java.sql.Types.VARBINARY: typestr = "VARBINARY"; break; // NOI18N
        case java.sql.Types.VARCHAR: typestr = "VARCHAR"; break; // NOI18N
        default:
            Logger.getLogger(Specification.class.getName()).log(Level.INFO, "Unknown type {0}", type);
            assert false : "Unknown type " + type;
        }

        ret = (String) typemap.get("java.sql.Types." + typestr); // NOI18N
        if (ret == null)
            ret = typestr;
        
        return ret;
    }

    /** Returns DBType where maps specified java type */
    public static int getType(String type)
    {
        if (type.equals("java.sql.Types.ARRAY")) return java.sql.Types.ARRAY; // NOI18N
        if (type.equals("java.sql.Types.BIGINT")) return java.sql.Types.BIGINT; // NOI18N
        if (type.equals("java.sql.Types.BINARY")) return java.sql.Types.BINARY; // NOI18N
        if (type.equals("java.sql.Types.BIT")) return java.sql.Types.BIT; // NOI18N
        if (type.equals("java.sql.Types.BLOB")) return java.sql.Types.BLOB; // NOI18N
        if (type.equals("java.sql.Types.BOOLEAN")) return java.sql.Types.BOOLEAN; // NOI18N
        if (type.equals("java.sql.Types.CHAR")) return java.sql.Types.CHAR; // NOI18N
        if (type.equals("java.sql.Types.CLOB")) return java.sql.Types.CLOB; // NOI18N
        if (type.equals("java.sql.Types.DATE")) return java.sql.Types.DATE; // NOI18N
        if (type.equals("java.sql.Types.DECIMAL")) return java.sql.Types.DECIMAL; // NOI18N
        if (type.equals("java.sql.Types.DISTINCT")) return java.sql.Types.DISTINCT; // NOI18N
        if (type.equals("java.sql.Types.DOUBLE")) return java.sql.Types.DOUBLE; // NOI18N
        if (type.equals("java.sql.Types.FLOAT")) return java.sql.Types.FLOAT; // NOI18N
        if (type.equals("java.sql.Types.INTEGER")) return java.sql.Types.INTEGER; // NOI18N
        if (type.equals("java.sql.Types.JAVA_OBJECT")) return java.sql.Types.JAVA_OBJECT; // NOI18N
        if (type.equals("java.sql.Types.LONGVARBINARY")) return java.sql.Types.LONGVARBINARY; // NOI18N
        if (type.equals("java.sql.Types.LONGNVARCHAR")) return java.sql.Types.LONGNVARCHAR; // NOI18N
        if (type.equals("java.sql.Types.LONGVARCHAR")) return java.sql.Types.LONGVARCHAR; // NOI18N
        if (type.equals("java.sql.Types.NUMERIC")) return java.sql.Types.NUMERIC; // NOI18N
        if (type.equals("java.sql.Types.NCHAR")) return java.sql.Types.NCHAR; // NOI18N
        if (type.equals("java.sql.Types.NCLOB")) return java.sql.Types.NCLOB; // NOI18N
        if (type.equals("java.sql.Types.NULL")) return java.sql.Types.NULL; // NOI18N
        if (type.equals("java.sql.Types.NVARCHAR")) return java.sql.Types.NVARCHAR; // NOI18N
        if (type.equals("java.sql.Types.OTHER")) return java.sql.Types.OTHER; // NOI18N
        if (type.equals("java.sql.Types.REAL")) return java.sql.Types.REAL; // NOI18N
        if (type.equals("java.sql.Types.REF")) return java.sql.Types.REF; // NOI18N
        if (type.equals("java.sql.Types.ROWID")) return java.sql.Types.ROWID; // NOI18N
        if (type.equals("java.sql.Types.SMALLINT")) return java.sql.Types.SMALLINT; // NOI18N
        if (type.equals("java.sql.Types.SQLXML")) return java.sql.Types.SQLXML; // NOI18N
        if (type.equals("java.sql.Types.TIME")) return java.sql.Types.TIME; // NOI18N
        if (type.equals("java.sql.Types.TIMESTAMP")) return java.sql.Types.TIMESTAMP; // NOI18N
        if (type.equals("java.sql.Types.TINYINT")) return java.sql.Types.TINYINT; // NOI18N
        if (type.equals("java.sql.Types.VARBINARY")) return java.sql.Types.VARBINARY; // NOI18N
        if (type.equals("java.sql.Types.VARCHAR")) return java.sql.Types.VARCHAR; // NOI18N
        Logger.getLogger(Specification.class.getName()).log(Level.INFO, "Unknown type name {0}, so return -1", type);
        assert false : "Unknown type name " + type;
        return -1;
    }
}
