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

/** Describes a foreign key in a table.
 */
public final class ForeignKeyElement extends KeyElement implements ReferenceKey, ColumnPairElementHolder {
	/** Creates a new foreign key element represented in memory.
	 */
	public ForeignKeyElement() {
		this(new Memory(), null);
	}

	/** Creates a new foreign key element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this foreign key, or
	 * <code>null</code>
	 */
	public ForeignKeyElement(Impl impl, TableElement declaringTable) {
		super(impl, declaringTable);
	}

    /** Returns the implementation for the foreign key.
     * @return implementation for the foreign key
     */
	final Impl getForeignKeyImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the referenced table of the foreign key.
	 * @return the referenced table
	 */
	public TableElement getReferencedTable() {
		ColumnPairElement[] columnPairs = getColumnPairs();

		if ((columnPairs != null) && (columnPairs.length > 0))
        	return columnPairs[0].getReferencedColumn().getDeclaringTable();

		return null;
	}

	/** Gets all referenced columns in this foreign key.
	 * @return the referenced columns
	 */
	public ColumnElement[] getReferencedColumns() {
        ColumnPairElement[] columnPairs = getColumnPairs();
        int count = ((columnPairs != null) ? columnPairs.length : 0);
        ColumnElement[] ce = new ColumnElement[count];
        
        for (int i = 0; i < count; i++)
            ce[i] = columnPairs[i].getReferencedColumn();
        
        return ce;
	}
    
    /** Gets the name of this element.
     * @return the name
     */
    public String getKeyName() {
        return getName().getFullName();
    }
    
    /** Sets the name of this element.
     * @param name the name
     * @throws DBException if impossible
     */
    public void setKeyName(String name) throws DBException {        
        setName(DBIdentifier.create(name));
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
        getForeignKeyImpl().changeColumnPairs(pairs, Impl.ADD);
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
        getForeignKeyImpl().changeColumnPairs(pairs, Impl.REMOVE);
    }
    
    /** Sets the column pairs for this holder. Previous column pairs are removed.
     * @param pairs the new column pairs
     * @throws DBException if impossible
     */
    public void setColumnPairs(ColumnPairElement[] pairs) throws DBException {
        getForeignKeyImpl().changeColumnPairs(pairs, Impl.SET);
    }
    
    /** Gets all column pairs in this holder.
     * @return the column pairs
     */
    public ColumnPairElement[] getColumnPairs() {
        return getForeignKeyImpl().getColumnPairs();
    }
    
    /** Finds a column pair by name.
     * @param name the name of the column pair for which to look
     * @return the column pair or <code>null</code> if not found
     */
    public ColumnPairElement getColumnPair(DBIdentifier name) {
        return getForeignKeyImpl().getColumnPair(name);
    }

    /** Gets all local columns in this reference key.
     * @return the local columns
     */
    public ColumnElement[] getLocalColumns() {
        return getForeignKeyImpl().getColumns();
    }
    
//-- Unsupported methods ---------------------------------------------------
    
    /** Adds a column to the holder. It is unsupported operation.
     * @param el the column to add
     * @throws UnsupportedOperationException is always thrown
     */
    public void addColumn (ColumnElement el) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /** Adds a collection of columns to the holder. It is unsupported operation.
     * @param els the columns to add
     * @throws UnsupportedOperationException is always thrown
     */
    public void addColumns (ColumnElement[] els) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /** Removes a column from the holder. It is unsupported operation.
     * @param el the column to remove
     * @throws UnsupportedOperationException is always thrown
     */
    public void removeColumn (ColumnElement el) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /** Removes a collection of columns from the holder. It is unsupported operation.
     * @param els the columns to remove
     * @throws UnsupportedOperationException is always thrown
     */
    public void removeColumns (ColumnElement[] els) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /** Sets a collection of columns to the holder. It is unsupported operation.
	 * Previous columns are removed.
     * @param els the column to set
     * @throws UnsupportedOperationException is always thrown
     */
    public void setColumns (ColumnElement[] els) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
//-------------------------------------------------------------------------
    
    /** Gets all columns in this key.
     * @return the columns
     */
    public ColumnElement[] getColumns () {
        return getForeignKeyImpl().getColumns();
    }

    /** Gets a column by name.
     * @param name the name
     * @return the column
     */
    public ColumnElement getColumn (DBIdentifier name) {
        return getForeignKeyImpl().getColumn(name);
    }

    
	/** Implementation of a foreign key element.
	 * @see KeyElement
	 */
	public interface Impl extends KeyElement.Impl {
        /** Changes the content of this object.
         * @param arr array of objects to change
         * @param action the action to do
         */
		public void changeColumnPairs(ColumnPairElement[] pairs, int action) throws DBException;
        
        /** Gets all column pairs.
         * @return the column pairs
         */
        public ColumnPairElement[] getColumnPairs();

        /** Finds a column pair by name.
         * @param name the name of the column pair for which to look
         * @return the column pair or <code>null</code> if not found
         */
        public ColumnPairElement getColumnPair(DBIdentifier name);
	}

	static class Memory extends KeyElement.Memory implements Impl {
		/** collection of column pairs */
		private DBMemoryCollection.ColumnPair pairs;
        
        /** Default constructor
         */
		Memory() {
			super();
		}

		/** Copy constructor.
		* @param fk the object from which to read values
		*/
		Memory(ForeignKeyElement fk) {
			super(fk);
		}

        /** Gets all column pairs.
         * @return the column pairs
         */
        public synchronized ColumnPairElement[] getColumnPairs() {
			initColumnPairs();
			return (ColumnPairElement[]) pairs.getElements();
        }
        
        /** Finds a column pair by name.
         * @param name the name of the column pair for which to look
         * @return the column pair or <code>null</code> if not found
         */
        public synchronized ColumnPairElement getColumnPair(DBIdentifier name) {
			initColumnPairs();
			return (ColumnPairElement) pairs.getElement(name);
        }
        
        /** Changes the content of this object.
         * @param arr array of objects to change
         * @param action the action to do
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
