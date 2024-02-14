/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.dbschema;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.text.MessageFormat;

/** Describes an entire database table.
 */
public final class TableElement extends DBElement implements ColumnElementHolder, ColumnPairElementHolder {
	/** Constant indicating that the table is a real table.
	 * @see #isTableOrView
	 */
	public static final boolean TABLE = true;

	/** Constant indicating that the table is a view.
	 * @see #isTableOrView
	 */
	public static final boolean VIEW = false;

	/** the schema to which this table element belongs */
	private transient SchemaElement declaringSchema;

    /** Creates a new table element represented in memory.
     */
	public TableElement() {
        this(new Memory(), null);
	}

	/** Createa a table element.
	 * @param impl the pluggable implementation
	 * @param declaringSchema the schema to which this element belongs
	 */
	public TableElement(Impl impl, SchemaElement declaringSchema) {
		super(impl);
		this.declaringSchema = declaringSchema;
	}

    /** Returns the implementation for the table.
	 * @return implementation for the table
	 */
	final Impl getTableImpl() {
        return (Impl) getElementImpl();
    }

    /** Returns the TableElement object associated with the given name and with the given schema.
     * @param name the table name
     * @param schema the schema
     * @return the TableElement object for the given table name in the given schema
     */
	public static TableElement forName(String name, SchemaElement schema) {
        int pos = name.lastIndexOf(".");
        if (pos == -1)
            return null;
        else
            name = name.substring(pos + 1);
        
        TableElement[] tes = schema.getTables();
        for (int i = 0; i < tes.length; i++)
            if (tes[i].getName().getName().trim().equals(name))
                return tes[i];

        return null;
	}

    /** Returns the TableElement object associated with the given name.
     * @param name the table name
     * @return the TableElement object for the given table name
     */
	public static TableElement forName(String name) /* 4_ea throws DBException */ {
        int index = name.lastIndexOf("."); //NOI18N
        
        if (index == -1) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                System.out.println(ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString("FullyQualifiedName")); //NOI18N
            return null;
        }
        
        SchemaElement se = SchemaElement.forName(name.substring(0, index));
        
        if (se == null)
// 4_ea            throw new DBException(bundle.getString("SchemaNotFound"));
            return null;
        else
            return TableElement.forName(name, se);
	}

	/** Sets whether this is really a table, or a view.
	 * @param isTable one of {@link #TABLE} or {@link #VIEW}
	 * @throws DBException if impossible
	 */
	public void setTableOrView(boolean isTable) throws DBException {
		    getTableImpl().setTableOrView(isTable);
	}

	/** Tests whether this is a table, or a view.
	 * @return one of {@link #TABLE} or {@link #VIEW}
	 */
	public boolean isTableOrView() {
            return getTableImpl().isTableOrView();
  }

	/** Tests whether this is really a table.
	 * @return <code>true</code> if so
	 * @see #isTableOrView
	 */
	public boolean isTable() { 
            return getTableImpl().isTableOrView();
  }

	/** Test whether this is a view.
	 * @return <code>true</code> if so
	 * @see #isTableOrView
	 */
	public boolean isView() {
            return !getTableImpl().isTableOrView();
  }

	// ==================== schema section ==========================

	/* This should be automatically synchronized
	* when a TableElement is added to the schema. */
  
	/** Gets the declaring schema. 
	 * @return the schema that owns this table element
	 */
	public final SchemaElement getDeclaringSchema() {
        return declaringSchema;
    }

	/** Sets the declaring schema. 
	 * @param se the schema that owns this table element
	 */
	public final void setDeclaringSchema(SchemaElement se) {
        if (declaringSchema == null)
            declaringSchema = se;
    }

	//================== Columns ===============================

	/** Adds a new column to the table.
	 * @param el the column to add
	 * @throws DBException if impossible
	 */
	public void addColumn(ColumnElement el) throws DBException {
		addColumns(new ColumnElement[] {el});
	}

	/** Adds some new columns to the table.
	 * @param els the columns to add
	 * @throws DBException if impossible
	 */
	public void addColumns(final ColumnElement[] els) throws DBException {
		for (int i = 0; i < els.length; i++) {
			if (getColumn(els[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", els[i]); //NOI18N
            if (els[i].getDeclaringTable() == null)
                els[i].setDeclaringTable(this);
        }
        getTableImpl().changeColumns(els, Impl.ADD);
	}

	/** Removes a column from the table.
	 * @param el the column to remove
	 * @throws DBException if impossible
	 */
	public void removeColumn(ColumnElement el) throws DBException {
		removeColumns(new ColumnElement[] {el});
	}

	/** Removes some columns from the table.
	 * @param els the columns to remove
	 * @throws DBException if impossible
	 */
	public void removeColumns(final ColumnElement[] els) throws DBException {
    		getTableImpl().changeColumns(els, Impl.REMOVE);
	}

	/** Sets the columns for this table.
	 * Previous columns are removed.
	 * @param els the new columns
	 * @throws DBException if impossible
	 */
	public void setColumns(ColumnElement[] els) throws DBException {
            getTableImpl().changeColumns(els, Impl.SET);
	}

	/** Gets all columns in this table.
	 * @return the columns
	 */
	public ColumnElement[] getColumns() {
            return getTableImpl().getColumns();
    }

	/** Finds a column by name.
	 * @param name the name of the column for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public ColumnElement getColumn(DBIdentifier name) {
    		return getTableImpl().getColumn(name);
	}

	//================== Indexes ===============================

	/** Adds a new index to the table.
	 * @param el the index to add
	 * @throws DBException if impossible
	 */
	public void addIndex(IndexElement el) throws DBException {
		addIndexes(new IndexElement[] {el});
	}

	/** Adds some new indexes to the table.
	 * @param els the indexes to add
	 * @throws DBException if impossible
	 */
	public void addIndexes(final IndexElement[] els) throws DBException {
		for (int i = 0; i < els.length; i++) {
			if (getIndex(els[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", els[i]); //NOI18N
            if (els[i].getDeclaringTable() == null)
                els[i].setDeclaringTable(this);
        }

        getTableImpl().changeIndexes(els, Impl.ADD);
	}

	/** Removesan index from the table.
	 * @param el the index to remove
	 * @throws DBException if impossible
	 */
	public void removeIndex(IndexElement el) throws DBException {
		removeIndexes(new IndexElement[] {el});
	}

	/** Removes some indexes from the table.
	 * @param els the indexes to remove
	 * @throws DBException if impossible
	 */
	public void removeIndexes(final IndexElement[] els) throws DBException {
        getTableImpl().changeIndexes(els, Impl.REMOVE);
	}

	/** Sets the indexes for this table.
	 * Previous indexes are removed.
	 * @param els the new indexes
	 * @throws DBException if impossible
	 */
	public void setIndexes(IndexElement[] els) throws DBException {
        getTableImpl().changeIndexes(els, Impl.SET);
	}

	/** Gets all indexes in this table.
	 * @return the indexes
	 */
	public IndexElement[] getIndexes() {
        return getTableImpl().getIndexes();
    }

	/** Finds an index by name.
	 * @param name the name of the index for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public IndexElement getIndex(DBIdentifier name) {
        return getTableImpl().getIndex(name);
	}

	//================== Keys (Unique, Primary, and Foreign) =================

	/** Adds a new key to the table.
	 *  @param el the key to add
	 * @throws DBException if impossible
	 */
	public void addKey(KeyElement el) throws DBException {
		addKeys(new KeyElement[]{el});
	}

	/** Adds some new keys to the table.
	 *  @param els the keys to add
	 * @throws DBException if impossible
	 */
	public void addKeys(final KeyElement[] els) throws DBException {
		for (int i = 0; i < els.length; i++) {
			if (getKey(els[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", els[i]); //NOI18N
            if (els[i].getDeclaringTable() == null)
                els[i].setDeclaringTable(this);
            if (els[i] instanceof UniqueKeyElement)
                if (((UniqueKeyElement) els[i]).getAssociatedIndex() == null) {
                    IndexElement ie  = new IndexElement();
                    try {
                        ie.setName(els[i].getName());
                        ie.setColumns(els[i].getColumns());
                        this.addIndex(ie);
                    } catch (DBException exc) {
                        exc.printStackTrace();
                    }
                    ((UniqueKeyElement) els[i]).setAssociatedIndex(ie);
                }
        }
	
        getTableImpl().changeKeys(els, Impl.ADD);
	}

	/** Removes a key from the table.
	 *  @param el the key to remove
	 * @throws DBException if impossible
	 */
	public void removeKey(KeyElement el) throws DBException {
		removeKeys(new KeyElement[] {el});
	}

	/** Removes some keys from the table.
	 *  @param els the keys to remove
	 * @throws DBException if impossible
	 */
	public void removeKeys(final KeyElement[] els) throws DBException {
    		getTableImpl().changeKeys(els, Impl.REMOVE);
	}

	/** Sets the keys for this table.
	 * Previous keys are removed.
	 * @param els the new keys
	 * @throws DBException if impossible
	 */

	public void setKeys(KeyElement[] els) throws DBException {
            getTableImpl().changeKeys(els, Impl.SET);
	}

	/** Gets all keys in this table.
	 * @return the keys
	 */
	public KeyElement[] getKeys() {
    		return getTableImpl().getKeys();
	}

	/** Finds a key by name.
	 * @param name the name of the key for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public KeyElement getKey(DBIdentifier name) {
    		return getTableImpl().getKey(name);
	}

	// key convenience methods

	/** Gets all keys in this table of the given subtype.
     * @param subtype the type of the key classes
	 * @return the keys of the given subtype or <code>null</code> if not found
	 */
	private List<KeyElement> getKeys(Class subtype) {
		KeyElement[] keys = getKeys();

        if (keys == null)
            return null;

		int i, count = keys.length;
		List<KeyElement> subKeys = new ArrayList<>(count);

		for (i = 0; i < count; i++) {
			KeyElement key = keys[i];

			if (subtype.isInstance(key))
				subKeys.add(key);
		}

		return subKeys;
	}

	/** Gets all foreign keys in this table.
	 * @return the foreign keys or <code>null</code> if not found
	 */
	public ForeignKeyElement[] getForeignKeys() {
		List<KeyElement> keys = getKeys(ForeignKeyElement.class);
        
        if (keys == null)
            return null;

		int count = keys.size();

		return (keys.toArray(new ForeignKeyElement[count]));
	}

	/** Finds a foreign key by name.
	 * @param name the name of the foreign key for which to look
	 * @return the foreign key or <code>null</code> if not found
	 */
	public ForeignKeyElement getForeignKey(DBIdentifier name) {
		ForeignKeyElement[] fks = getForeignKeys();
		int i, count = fks.length;
		
		for (i = 0; i < count; i++) {
			ForeignKeyElement fk = fks[i];

			if (name.equals(fk.getName()))
				return fk;
		}

		return null;
	}
	
	/** Gets all unique keys in this table.
	 * @return the unique keys or <code>null</code> if not found
	 */
	public UniqueKeyElement[] getUniqueKeys() {
		List<KeyElement> keys = getKeys(UniqueKeyElement.class);

		if (keys == null)
            return null;

		return (keys.toArray(new UniqueKeyElement[0]));
	}

	/** Finds a unique key by name.
	 * @param name the name of the unique key for which to look
	 * @return the unique key or <code>null</code> if not found
	 */
	public UniqueKeyElement getUniqueKey(DBIdentifier name) {
		UniqueKeyElement[] uks = getUniqueKeys();
		int i, count = uks.length;
		
		for (i = 0; i < count; i++) {
			UniqueKeyElement uk = uks[i];

			if (name.equals(uk.getName()))
				return uk;
		}

		return null;
	}

	/** Finds the primary key.
	 * @return the primary key or <code>null</code> if not found
	 */
	public UniqueKeyElement getPrimaryKey() {
		UniqueKeyElement[] uks = getUniqueKeys();

        if (uks == null)
            return null;

        for (int i = 0; i < uks.length; i++) {
            UniqueKeyElement uk = uks[i];

            if (uk.isPrimaryKey())
                return uk;
        }

		return null;
	}

	// end key convenience methods

    /** This method just throws localized exception. It is used during
     * adding class element, which already exists in source.
     * @param formatKey The message format key to localized bundle.
     * @param element The element which can't be added
     * @exception DBException is alway thrown from this method.
     */
    private void throwAddException(String formatKey, DBMemberElement element) throws DBException {
        // MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = /*format.format(new Object[] { */element.getName().getName();// });
        throw new DBException(msg);
    }
    
    /** Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString() {
        if (getName() != null)
            return getName().toString();
        else
            return null;
    }

    /** Adds a new column pair to the holder.
     * @param pair the pair to add
     * @throws DBException if impossible
     */
    public void addColumnPair(ColumnPairElement pair) throws DBException {
		addColumnPairs(new ColumnPairElement[] {pair});
    }    

    /** Adds some new column pairs to the holder.
     * @param pairs the column pairs to add
     * @throws DBException if impossible
     */
    public void addColumnPairs(ColumnPairElement[] pairs) throws DBException {
		for (int i = 0; i < pairs.length; i++) {
			if (getColumnPair(pairs[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", pairs[i]); //NOI18N
            if (pairs[i].getDeclaringTable() == null)
                pairs[i].setDeclaringTable(this);
        }
        getTableImpl().changeColumnPairs(pairs, Impl.ADD);
    }
    
    /** Removes a column pair from the holder.
     * @param pair the column pair to remove
     * @throws DBException if impossible
     */
    public void removeColumnPair(ColumnPairElement pair) throws DBException {
		removeColumnPairs(new ColumnPairElement[] {pair});
    }
    
    /** Removes some column pairs from the holder.
     * @param pairs the column pairs to remove
     * @throws DBException if impossible
     */
    public void removeColumnPairs(ColumnPairElement[] pairs) throws DBException {
        getTableImpl().changeColumnPairs(pairs, Impl.REMOVE);
    }
    
    /** Sets the column pairs for this holder.
     * Previous column pairs are removed.
     * @param pairs the new column pairs
     * @throws DBException if impossible
     */
    public void setColumnPairs(ColumnPairElement[] pairs) throws DBException {
        getTableImpl().changeColumnPairs(pairs, Impl.SET);
    }
    
    /** Gets all column pairs in this holder.
     * @return the column pairs
     */
    public ColumnPairElement[] getColumnPairs() {
        return getTableImpl().getColumnPairs();
    }
    
    /** Finds a column pair by name.
     * @param name the name of the column pair for which to look
     * @return the column pair or <code>null</code> if not found
     */
    public ColumnPairElement getColumnPair(DBIdentifier name) {
        return getTableImpl().getColumnPair(name);
    }

    /** Finds a database member element by name.
     * @param name the name of the database member element for which to look
     * @return the database member element or <code>null</code> if not found
     */
    public DBMemberElement getMember(DBIdentifier name) {
        int index = name.getName().indexOf(";"); //NOI18N
        
        if (index == -1)
            //column
            return getColumn(name);
        else
            //column pair
            return getColumnPair(name);
    }
    
	/** Pluggable behaviour for table elements.
	 * @see TableElement
	 */
	public static interface Impl extends DBElement.Impl {
		/** Sets whether this is really a table, or a view.
		 * @param isTable one of {@link #TABLE} or {@link #VIEW}
		 * @throws DBException if impossible
		 */
		public void setTableOrView(boolean isTable) throws DBException;

		/** Tests whether this is a table, or a view.
		 * @return one of {@link #TABLE} or {@link #VIEW}
		 */
		public boolean isTableOrView();

		/** Changes the set of columns.
		* @param elems the columns to change
		* @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
		* @exception DBException if the action cannot be handled
		*/
		public void changeColumns(ColumnElement[] elems, int action) throws DBException;

		/** Gets all columns.
		* @return the columns
		*/
		public ColumnElement[] getColumns();

		/** Finds a column by name.
		* @param name the name for which to look
		* @return the column, or <code>null</code> if it does not exist
		*/
		public ColumnElement getColumn(DBIdentifier name);

		/** Changes the set of indexes.
		* @param elems the indexes to change
		* @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
		* @exception DBException if the action cannot be handled
		*/
		public void changeIndexes(IndexElement[] elems, int action) throws DBException;

		/** Gets all indexes.
		* @return the indexes
		*/
		public IndexElement[] getIndexes();

		/** Finds an index by name.
		* @param name the name for which to look
		* @return the index, or <code>null</code> if it does not exist
		*/
		public IndexElement getIndex(DBIdentifier name);

		/** Changes the set of keys.
		* @param elems the keys to change
		* @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
		* @exception DBException if the action cannot be handled
		*/
		public void changeKeys(KeyElement[] elems, int action) throws DBException;

		/** Gets all keys.
		* @return the keys
		*/
		public KeyElement[] getKeys();

		/** Finds a key by name.
		* @param name the name for which to look
		* @return the key, or <code>null</code> if it does not exist
		*/
		public KeyElement getKey(DBIdentifier name);
        
		/** Changes the set of column pairs.
		* @param pairs the column pairs to change
		* @param action one of {@link #ADD}, {@link #REMOVE}, or {@link #SET}
		* @exception DBException if the action cannot be handled
		*/
		public void changeColumnPairs(ColumnPairElement[] pairs, int action) throws DBException;
        
		/** Gets all column pairs.
		* @return the column pairs
		*/
        public ColumnPairElement[] getColumnPairs();

		/** Finds a column pair by name.
		* @param name the name for which to look
		* @return the column pair, or <code>null</code> if it does not exist
		*/
        public ColumnPairElement getColumnPair(DBIdentifier name);
	}

	/** Memory based implementation of the element factory.
	 */
	static final class Memory extends DBElement.Memory implements Impl {
		/** is table or view */
		private boolean _isTable;

		/** collection of columns */
		private DBMemoryCollection.Column columns;

		/** collection of indexes */
		private DBMemoryCollection.Index indexes;

		/** collection of keys */
		private DBMemoryCollection.Key keys;
        
		/** collection of column pairs */
		private DBMemoryCollection.ColumnPair pairs;

        /** Default constructor.
         */
		public Memory() {
            super();
			_isTable = true;
		}

		/** Copy constructor.
		 * @param el element to copy from
		 */
		public Memory(TableElement el) {
			super(el);
			_isTable = el.isTableOrView();
		}

		/** Sets whether this is really a table, or a view.
		 * @param isTable one of {@link #TABLE} or {@link #VIEW}
		 * @throws DBException if impossible
		 */
		public void setTableOrView(boolean isTable) {
			boolean old = _isTable;

			_isTable = isTable;
			firePropertyChange(PROP_TABLE_OR_VIEW, Boolean.valueOf(old), Boolean.valueOf(isTable));
		}

		/** Tests whether this is a table, or a view.
		 * @return one of {@link #TABLE} or {@link #VIEW}
		 */
        public boolean isTableOrView() {
            return _isTable;
        }

		/** Changes set of columns.
		 * @param elems elements to change
         * @param action the action to do
		 * @exception SourceException if the action cannot be handled
		 */
		public synchronized void changeColumns(ColumnElement[] elems, int action) throws DBException {
			initColumns();
			columns.change(elems, action);
		}

        /** Gets all columns
         * @return the columns
         */
        public synchronized ColumnElement[] getColumns() {
			initColumns();
			return (ColumnElement[]) columns.getElements();
        }

		/** Finds a column with given name.
		 * @param name the name of column for which to look
		 * @return the element or null if column with such name does not exist
		 */
        public synchronized ColumnElement getColumn(DBIdentifier name) {
			initColumns();
			return (ColumnElement) columns.getElement(name);
        }

        /** Initializes the collection of columns.
         */
		void initColumns() {
			if (columns == null)
				columns = new DBMemoryCollection.Column(this);
		}

		/** Changes set of indexes.
		 * @param elems elements to change
         * @param action the action to do
		 * @exception SourceException if the action cannot be handled
		 */
		public synchronized void changeIndexes(IndexElement[] elems, int action) throws DBException  {
			initIndexes();
			indexes.change(elems, action);
		}

        /** Gets all indexes.
         * @return the indexes
         */
        public synchronized IndexElement[] getIndexes() {
			initIndexes();
			return (IndexElement[]) indexes.getElements();
        }

		/** Finds an index with given name.
		 * @param name the name of index for which to look
		 * @return the element or null if index with such name does not exist
		 */
        public synchronized IndexElement getIndex(DBIdentifier name) {
			initIndexes();
			return (IndexElement) indexes.getElement(name);
        }

        /** Initializes the collection of indexes.
         */
		void initIndexes() {
			if (indexes == null)
				indexes = new DBMemoryCollection.Index(this);
		}
		
		/** Changes set of keys.
		 * @param elems elements to change
         * @param action the action to do
		 * @exception SourceException if the action cannot be handled
		 */
		public synchronized void changeKeys(KeyElement[] elems, int action) throws DBException {
			initKeys();
			keys.change(elems, action);
		}

        /** Gets all keys.
         * @return the keys
         */
        public synchronized KeyElement[] getKeys() {
			initKeys();
			return (KeyElement[]) keys.getElements();
        }

		/** Finds a key with given name.
		 * @param name the name of key for which to look
		 * @return the element or null if key with such name does not exist
		 */
        public synchronized KeyElement getKey(DBIdentifier name) {
			initKeys();
			return (KeyElement) keys.getElement(name);
        }

        /** Initializes the collection of keys.
         */
		void initKeys() {
			if (keys == null)
				keys = new DBMemoryCollection.Key(this);
		}

		/** Getter for the associated table
		 * @return the table element for this impl
		 */
		final TableElement getTableElement() {
			return (TableElement) _element;
		}
        
        /** Gets all column pairs.
         * @return the column pairs
         */
        public synchronized ColumnPairElement[] getColumnPairs() {
			initColumnPairs();
			return (ColumnPairElement[]) pairs.getElements();
        }
        
		/** Finds a column pair with given name.
		 * @param name the name of column pair for which to look
		 * @return the column pair or null if key with such name does not exist
		 */
        public synchronized ColumnPairElement getColumnPair(DBIdentifier name) {
			initColumnPairs();
			return (ColumnPairElement) pairs.getElement(name);
        }
        
		/** Changes set of column pairs.
		 * @param pairs elements to change
         * @param action the action to do
		 * @exception SourceException if the action cannot be handled
		 */
        public synchronized void changeColumnPairs(ColumnPairElement[] pairs, int action) throws DBException {
			initColumnPairs();
			this.pairs.change(pairs, action);
        }
        
        /** Initializes the collection of column pairs.
         */
		void initColumnPairs() {
			if (pairs == null)
				pairs = new DBMemoryCollection.ColumnPair(this);
		}
	}
}
