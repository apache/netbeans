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

package org.netbeans.modules.dbschema;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.netbeans.modules.dbschema.util.*;

import org.netbeans.modules.dbschema.migration.archiver.*;
import org.openide.nodes.Node;

/** Describes an entire database schema.
 */
public class SchemaElement extends DBElement implements Node.Cookie {
    /** Status when the schema element is not yet prepared. */
    public static final int STATUS_NOT = 0;

    /** Status when the schema element contains unrecoverable errors. */
    public static final int STATUS_ERROR = 1;

    /** Status when the schema element contains minor errors. */
    public static final int STATUS_PARTIAL = 2;

    /** Status when the schema element has been parsed and is error-free. */
    public static final int STATUS_OK = 3;

    /** Version of the database schema API. */
    public static final int CURRENT_VERSION_NO = 2;
    private int versionNo;

    /** Creates a new schema element represented in memory.
     */
    public SchemaElement() {
        this(new Memory());
    }

    /** Creates a new schema element.
     * @param impl the pluggable implementation
     */
    public SchemaElement(Impl impl) {
        super(impl);
    }

    /** Returns the implementation for the schema.
     * @return implementation for the schema
     */
    final Impl getSchemaImpl() {
        return (Impl)getElementImpl();
    }

    /** Returns if the schema is compatible with current API version.
     * @return true if schema is compatible; false otherwise
     */
    public boolean isCompatibleVersion() {
        return (getVersionNo() == CURRENT_VERSION_NO);
    }
    
    /** Getter for property versionNo.
     * @return Value of property versionNo.
     */
    public int getVersionNo() {
        return versionNo;
    }
    
    /** Setter for property versionNo.
     * @param versionNo New value of property versionNo.
     */
    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    /** Cache for read schemas. */
    protected static Map<String, SchemaElement> schemaCache = new HashMap<>();
    
    /** Last used schema. */
    private static SchemaElement lastSchema;
    
    /** Returns the last used schema.
     * @return the last used schema
     */
    protected static SchemaElement getLastSchema() {
        return lastSchema;
    }
    
    /** Sets the last used schema.
     * @param last the last used schema
     */
    protected static void setLastSchema(SchemaElement last) {
        lastSchema = last;
    }
    
    /** Removes the specified schema from cache.
     * @param name the schema to remove
     */
    public static void removeFromCache(String name) {
        synchronized (schemaCache) {
            if (getLastSchema() != null)
                if (getLastSchema().getName().getFullName().equals(name))
                    setLastSchema(null);
            
            schemaCache.remove(name);
        }
    }
    
    /** Adds the specified schema element to cache.
     * @param schema the schema element to add
     */
    public static void addToCache(SchemaElement schema) {
        synchronized (schemaCache) {
            schemaCache.put(schema.getName().getFullName(), schema);
            SchemaElement.setLastSchema(schema);
        }
    }
    
    /** Returns the SchemaElement object associated with the schema with 
     * the given string name and object.  The second argument is meant to 
     * help define the context for loading of the schema and can be a 
     * FileObject[] or FileObject for use in the IDE or a ClassLoader for 
     * use at runtime.  Note that if if FileObject[] is used, the first match 
     * is returned if it's not already in the cache.  It might be extended 
     * later to accept a Project as well.  Any other non-null value for the 
     * second argument will result in an UnsupportedOperationException.
     * @param name the schema name
     * @param obj the schema context
     * @return the SchemaElement object for the given schema name
     */
    public static SchemaElement forName(String name, Object obj) {
        if (IDEUtil.isIDERunning())
            return SchemaElementUtil.forName(name, obj);
 
        if (obj == null)
            return forNameInternal(name, SchemaElement.class.getClassLoader());
        if (obj instanceof ClassLoader)
            return forNameInternal(name, (ClassLoader)obj);

        // if we got to this point the second object is not null, the 
        // IDE is not running, and the type of object is not one we can
        // handle
        throw new UnsupportedOperationException("Cannot lookup schema " + 
            name + " in context of type " + obj.getClass() + 
            " expected ClassLoader or null.");
    }

    /** Returns the SchemaElement object associated with the schema with the given string name, loaded by the given classloader.
     * @param name the schema name
     * @param cl the schema classloader
     * @return the SchemaElement object for the given schema name
     */
    private static SchemaElement forNameInternal(String name, ClassLoader cl) {
        SchemaElement se = getLastSchema();
  
        if (se != null && se.getName().getFullName().equals(name))
            return se;
        else
            synchronized (schemaCache) {
                se = schemaCache.get(name);
                if (se != null)
                    return se;
                
                InputStream is = cl.getResourceAsStream(NameUtil.getSchemaResourceName(name));

                if (is != null)
                    try {
                        ObjectInput i = new XMLInputStream(is);
                        se = (SchemaElement) i.readObject();
                        if (!se.isCompatibleVersion()) {
                            String message = MessageFormat.format(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("PreviousVersion"), new String[] {name}); //NOI18N
                            System.out.println(message);
                        }
                        i.close();

                        se.setName(DBIdentifier.create(name));

                        SchemaElement.addToCache(se);
                        
                        // MBO: now set the declaring schema in TableElement(transient field)
                        TableElement tables[] = se.getTables();
                        int size = (tables != null) ? tables.length : 0;
                        for (int j = 0; j < size; j++)
                            tables[j].setDeclaringSchema(se);
                    } catch (Exception exc) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                            System.out.println(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("SchemaNotFound")); //NOI18N
                    }
                else
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        System.out.println(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("SchemaNotFound")); //NOI18N

                return se;
            }
    }

    /** Returns the SchemaElement object associated with the schema with the given string name.
     * @param name the schema name
     * @return the SchemaElement object for the given schema name
     */
    public static SchemaElement forName(String name) {
        return forName(name, null);
    }

    /** Gets the parsing status of the element.
     * This is a non-blocking operation.
     * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR}, {@link
     * #STATUS_PARTIAL}, or {@link #STATUS_OK}
     */
    public int getStatus() {
        return getSchemaImpl().getStatus();
    }

    /** Sets the schema name of this schema snapshot.
     * @param id the schema name, or <code>null</code>
     * @exception DBException if the operation cannot proceed
     */
    public void setSchema(DBIdentifier schema) throws DBException {
        getSchemaImpl().setSchema(schema);
    }

    /** Gets the schema name of this schema snapshot.
     * @return the schema name, or <code>null</code> if this snapshot does not
     * have a schema name
     */
    public DBIdentifier getSchema() {
        return getSchemaImpl().getSchema();
    }

    /** Sets the catalog name of this schema snapshot.
     * @param id the catalog name, or <code>null</code>
     * @exception DBException if the operation cannot proceed
     */
    public void setCatalog(DBIdentifier catalog) throws DBException {
        getSchemaImpl().setCatalog(catalog);
    }

    /** Gets the catalog name of this schema snapshot.
     * @return the catalog name, or <code>null</code> if this snapshot does
     * not have a catalog name
     */
    public DBIdentifier getCatalog() {
        return getSchemaImpl().getCatalog();
    }

    /** Adds a new table to the schema snapshot.
     * @param el the table to add
     * @throws DBException if impossible
     */
    public void addTable(TableElement el) throws DBException {
        addTables(new TableElement[] {el});
    }

    /** Adds some new tables to the schema snapshot.
     * @param els the tables to add
     * @throws DBException if impossible
     */
    public void addTables(final TableElement[] els) throws DBException {
        for (int i = 0; i < els.length; i++) {
            if (getTable(els[i].getName()) != null)
                throwAddException("FMT_EXC_AddTable", els[i]); //NOI18N
            if (els[i].getDeclaringSchema() == null)
                els[i].setDeclaringSchema(this);
        }

        getSchemaImpl().changeTables(els, Impl.ADD);
    }

    /** Removes a table from the schema snapshot.
     * @param el the table to remove
     * @throws DBException if impossible
     */
    public void removeTable(TableElement el) throws DBException {
        removeTables(new TableElement[] {el});
    }

    /** Removes some tables from the schema snapshot.
     *  @param els the columns to remove
     * @throws DBException if impossible
     */
    public void removeTables(final TableElement[] els) throws DBException {
        getSchemaImpl().changeTables(els, Impl.REMOVE);
    }

    /** Sets the tables for this schema snapshot.
     * Previous tables are removed.
     * @param els the new tables
     * @throws DBException if impossible
     */
    public void setTables(TableElement[] els) throws DBException {
        if (els == null)
            throw new NullPointerException(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("NullTables")); //NOI18N

        getSchemaImpl().changeTables(els, Impl.SET);
    }

    /** Gets all tables in this schema snapshot.
     * @return the tables
     */
    public TableElement[] getTables() {
        return getSchemaImpl().getTables();
    }

    /** Finds a table by name.
     * @param name the name of the table for which to look
     * @return the element or <code>null</code> if not found
     */
    public TableElement getTable(DBIdentifier name) {
        return getSchemaImpl().getTable(name);
    }

    /** This method just throws localized exception. It is used during
     * adding class element, which already exists in source.
     * @param formatKey The message format key to localized bundle.
     * @param element The element which can't be added
     * @exception DBException is alway thrown from this method.
     */
    private void throwAddException(String formatKey, TableElement element) throws DBException {
        //MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = /*format.format(new Object[] { */element.getName().getName();// });
        throw new DBException(msg);
    }

    /** Getter for property url.
     * @return Value of property url.
     */
    public String getUrl() {
        return getSchemaImpl().getUrl();
    }

    /** Setter for property url.
     * @param url New value of property url.
     */
    public void setUrl(String url) throws DBException {
            getSchemaImpl().setUrl(url);
    }

    /** Getter for property username.
     * @return Value of property username.
     */
    public String getUsername() {
        return getSchemaImpl().getUsername();
    }

    /** Setter for property username.
     * @param username New value of property username.
     */
    public void setUsername(String username) throws DBException {
        getSchemaImpl().setUsername(username);
    }

    /** Getter for property driver.
     * @return Value of property driver.
     */
    public String getDriver() {
        return getSchemaImpl().getDriver();
    }

    /** Setter for property driver.
     * @param driver New value of property driver.
     */
    public void setDriver(String driver) {
        getSchemaImpl().setDriver(driver);
    }

    /** Getter for property databaseProductName.
     * @return Value of property databaseProductName.
     */
    public String getDatabaseProductName() {
        return getSchemaImpl().getDatabaseProductName();
    }

    /** Setter for property databaseProductName.
     *  @param databaseProductName New value of property databaseProductName.
     */
    public void setDatabaseProductName(String databaseProductName) throws DBException {
        getSchemaImpl().setDatabaseProductName(databaseProductName);
    }

    /** Getter for property databaseProductVersion.
     * @return Value of property databaseProductVersion.
     */
    public String getDatabaseProductVersion() {
        return getSchemaImpl().getDatabaseProductVersion();
    }

    /** Setter for property databaseProductVersion.
     * @param databaseProductVersion New value of property databaseProductVersion.
     */
    public void setDatabaseProductVersion(String databaseProductVersion) throws DBException {
        getSchemaImpl().setDatabaseProductVersion(databaseProductVersion);
    }

    /** Getter for property driverName.
     * @return Value of property driverName.
     */
    public String getDriverName() {
        return getSchemaImpl().getDriverName();
    }

    /** Setter for property driverName.
     * @param driverName New value of property driverName.
     */
    public void setDriverName(String driverName) throws DBException {
        getSchemaImpl().setDriverName(driverName);
    }

    /** Getter for property driverVersion.
     * @return Value of property driverVersion.
     */
    public String getDriverVersion() {
        return getSchemaImpl().getDriverVersion();
    }

    /** Setter for property driverVersion.
     * @param driverVersion New value of property driverVersion.
     */
    public void setDriverVersion(String driverVersion) throws DBException {
        getSchemaImpl().setDriverVersion(driverVersion);
    }

    /** Saves the current schema to an XML file.
     * @param filename the system-dependent filename
     */
    public void save(String filename) {
        setVersionNo(CURRENT_VERSION_NO);
        
        try {
            OutputStream s = new FileOutputStream(filename);
            ObjectOutput o = new XMLOutputStream(s);
            o.writeObject(this);
            o.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Saves the current schema to an XML file.
     * @param s the output stream
     */
    public void save(OutputStream s) {
        setVersionNo(CURRENT_VERSION_NO);
        
        try {
            ObjectOutput o = new XMLOutputStream(s);
            o.writeObject(this);
            o.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Pluggable behaviour for schema elements.
     * @see SchemaElement
     */
    public static interface Impl extends DBElement.Impl {
        /** Gets the parsing status of the element.
        * This is a non-blocking operation.
        * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR},
        * {@link #STATUS_PARTIAL}, or {@link #STATUS_OK}
        */
        public int getStatus();

        /** Sets the schema name of this schema snapshot.
         * @param id the schema name, or <code>null</code>
         * @exception DBException if the operation cannot proceed
         */
        public void setSchema(DBIdentifier id) throws DBException;

        /** Get the schema name of this schema snapshot.
         * @return the schema name, or <code>null</code> if this snapshot does
         * not have a schema name
         */
        public DBIdentifier getSchema();

        /** Sets the catalog name of this schema snapshot.
         * @param id the catalog name, or <code>null</code>
         * @exception DBException if the operation cannot proceed
         */
        public void setCatalog(DBIdentifier id) throws DBException;

        /** Gets the catalog name of this schema snapshot.
         * @return the catalog name, or <code>null</code> if this snapshot does
         * not have a catalog name
         */
        public DBIdentifier getCatalog();

        /** Change the set of tables.
        * @param elems the tables to change
        * @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
        * @exception DBException if the action cannot be handled
        */
        public void changeTables(TableElement[] elems, int action) throws DBException;

        /** Gets all tables.
        * @return the tables
        */
        public TableElement[] getTables();

        /** Finds a table by name.
        * @param name the name for which to look
        * @return the table, or <code>null</code> if it does not exist
        */
        public TableElement getTable(DBIdentifier name);

        /** Getter for property url.
         * @return Value of property url.
         */
        public String getUrl();

        /** Setter for property url.
         * @param url New value of property url.
         */
        public void setUrl(String url) throws DBException;

        /** Getter for property username.
         * @return Value of property username.
         */
        public String getUsername();

        /** Setter for property username.
         * @param username New value of property username.
         */
        public void setUsername(String username) throws DBException;

        /** Getter for property driver.
         * @return Value of property driver.
         */
        public String getDriver();

        /** Setter for property driver.
         * @param driver New value of property driver.
         */
        public void setDriver(String driver);

        /** Getter for property databaseProductName.
         * @return Value of property databaseProductName.
         */
        public String getDatabaseProductName();

        /** Setter for property databaseProductName.
         * @param databaseProductName New value of property databaseProductName.
         */
        public void setDatabaseProductName(String databaseProductName) throws DBException;

        /** Getter for property databaseProductVersion.
         * @return Value of property databaseProductVersion.
         */
        public String getDatabaseProductVersion();

        /** Setter for property databaseProductVersion.
         * @param databaseProductVersion New value of property databaseProductVersion.
         */
        public void setDatabaseProductVersion(String databaseProductVersion) throws DBException;

        /** Getter for property driverName.
         * @return Value of property driverName.
         */
        public String getDriverName();

        /** Setter for property driverName.
         * @param driverName New value of property driverName.
         */
        public void setDriverName(String driverName) throws DBException;

        /** Getter for property driverVersion.
         * @return Value of property driverVersion.
         */
        public String getDriverVersion();

        /** Setter for property driverVersion.
         * @param driverVersion New value of property driverVersion.
         */
        public void setDriverVersion(String driverVersion) throws DBException;
    }

	/** Memory based implementation of the element factory.
	 */
	static final class Memory extends DBElement.Memory implements Impl {
		/** collection of tables */
		private DBMemoryCollection.Table tables;

        private DBIdentifier _catalog;
        private DBIdentifier _schema;
        private int _status;
        private String _url;
        private String _username;
        private String _driver;
        private String _databaseProductName;
        private String _databaseProductVersion;
        private String _driverName;
        private String _driverVersion;

        /** Default constructor
         */
		public Memory() {
            super();
		}

		/** Copy constructor.
		 * @param el element to copy from
		 */
		public Memory(SchemaElement el) {
			super(el);
            _catalog = el.getCatalog();
            _schema = el.getSchema();
            _status = el.getStatus();
            _url = el.getUrl();
            _username = el.getUsername();
            _driver = el.getDriver();
            _databaseProductName = el.getDatabaseProductName();
            _databaseProductVersion = el.getDatabaseProductVersion();
            _driverName = el.getDriverName();
            _driverVersion = el.getDriverVersion();
		}

		/** Late initialization of initialization of copy elements.
		 */
		public void copyFrom(SchemaElement copyFrom) throws DBException {
			changeTables(copyFrom.getTables(), SET);
		}

		/** Changes set of elements.
		 * @param elems elements to change
		 * @exception SourceException if the action cannot be handled
		 */
		public synchronized void changeTables(TableElement[] elems, int action) throws DBException {
			initTables();
			tables.change(elems, action);
		}

        /** Gets all tables.
         * @return the tables
         */
   	    public synchronized TableElement[] getTables() {
			initTables();
			return (TableElement[]) tables.getElements();
        }

		/** Finds a table with given name.
		 * @param name the name of table for which to look
		 * @return the element or null if table with such name does not exist
		 */
        public synchronized TableElement getTable(DBIdentifier name) {
			initTables();
			return (TableElement) tables.getElement(name);
        }

        /** Initializes the collection of tables.
         */
		void initTables() {
			if (tables == null)
				tables = new DBMemoryCollection.Table(this);
		}

		/** Getter for the associated schema
		 * @return the schema element for this impl
		 */
		final SchemaElement getSchemaElement() {
			return (SchemaElement) _element;
		}

        /** Gets the parsing status of the element.
         * This is a non-blocking operation.
         * @return one of {@link #STATUS_NOT}, {@link #STATUS_ERROR}, {@link
         * #STATUS_PARTIAL}, or {@link #STATUS_OK}
         */
        public int getStatus() {
            return _status;
        }

        public void setSchema(DBIdentifier id) throws DBException {
			DBIdentifier old = _schema;

			_schema = id;
			firePropertyChange (PROP_SCHEMA, old, id);
        }

        public DBIdentifier getSchema() {
            if (_schema == null)        // lazy initialization !?
                _schema = DBIdentifier.create(""); //NOI18N

            return _schema;
        }

        public void setCatalog(DBIdentifier id) throws DBException {
			DBIdentifier old = _catalog;

			_catalog = id;
			firePropertyChange (PROP_CATALOG, old, id);
        }

        public DBIdentifier getCatalog() {
            if (_catalog == null)        // lazy initialization !?
                _catalog = DBIdentifier.create(""); //NOI18N

            return _catalog;
        }

        public String getUrl() {
            return _url;
        }

        public void setUrl(String url) throws DBException{
            _url = url;
        }

        public String getUsername() {
            return _username;
        }

        public void setUsername(String username) throws DBException{
            _username = username;
        }

        public String getDriver() {
            return _driverName;
        }

        public void setDriver(String driver){
            _driver = driver;
        }

        public String getDatabaseProductName() {
            return _databaseProductName;
        }

        public void setDatabaseProductName(String databaseProductName) throws DBException {
            _databaseProductName = databaseProductName;
        }

        public String getDatabaseProductVersion() {
            return _databaseProductVersion;
        }

        public void setDatabaseProductVersion(String databaseProductVersion) throws DBException{
            _databaseProductVersion = databaseProductVersion;
        }

        public String getDriverName() {
            return _driverName;
        }

        public void setDriverName(String driverName) throws DBException {
            _driverName = driverName;
        }

        public String getDriverVersion() {
            return _driverVersion;
        }

        public void setDriverVersion(String driverVersion) throws DBException {
            _driverVersion = driverVersion;
        }
	}
}
