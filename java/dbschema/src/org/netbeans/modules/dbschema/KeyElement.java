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

/** Describes a key in a table.
 */
public abstract class KeyElement extends DBMemberElement implements ColumnElementHolder {
	/** Creates a new key element represented in memory.
	 */
	public KeyElement() {
		this(new Memory(), null);
	}

	/** Creates a new key element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this key, or
	 * <code>null</code>
	 */
	public KeyElement(KeyElement.Impl impl, TableElement declaringTable) {
		super(impl, declaringTable);
	}

    /** Returns the implementation for the key element.
     * @return implementation for the key element
     */
	final Impl getKeyImpl() {
        return (Impl)getElementImpl();
    }

	//================== Columns ===============================

	/** Adds a new column to the key.
	 * @param el the column to add
	 * @throws DBException if impossible
	 */
	public void addColumn(ColumnElement el) throws DBException {
		addColumns(new ColumnElement[]{el});
	}

	/** Adds some new columns to the key.
	 * @param els the columns to add
	 * @throws DBException if impossible
	 */
	public void addColumns(final ColumnElement[] els) throws DBException {
		for (int i = 0; i < els.length; i++)
			if (getColumn(els[i].getName()) != null)
				throwAddException("FMT_EXC_AddColumn", els[i]); //NOI18N
	
        getKeyImpl().changeColumns(els, TableElement.Impl.ADD);
	}

	/** Removes a column from the key.
	 * @param el the column to remove
	 * @throws DBException if impossible
	 */
	public void removeColumn(ColumnElement el) throws DBException {
		removeColumns(new ColumnElement[]{el});
	}

	/** Removes some columns from the key.
	 * @param els the columns to remove
	 * @throws DBException if impossible
	 */
	public void removeColumns(final ColumnElement[] els) throws DBException {
        getKeyImpl().changeColumns(els, TableElement.Impl.REMOVE);
	}

	/** Sets the columns for this key.
	 * Previous columns are removed.
	 * @param els the new columns
	 * @throws DBException if impossible
	 */
	public void setColumns(ColumnElement[] els) throws DBException {
        getKeyImpl().changeColumns(els, TableElement.Impl.SET);
	}

	/** Gets all columns in this key.
	 * @return the columns
	 */
	public ColumnElement[] getColumns() {
        return getKeyImpl().getColumns();
    }

	/** Finds a column by name.
	 * @param name the name of the column for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public ColumnElement getColumn(DBIdentifier name) {
        return getKeyImpl().getColumn(name);
	}
	
    /** This method just throws localized exception. It is used during
     * adding class element, which already exists in source.
     * @param formatKey The message format key to localized bundle.
     * @param element The element which can't be added
     * @exception DBException is alway thrown from this method.
     */
    private void throwAddException(String formatKey, ColumnElement element) throws DBException {
        //MessageFormat format = new MessageFormat(ElementFormat.bundle.getString(formatKey));
        String msg = /*format.format(new Object[] { */element.getName().getName();// });
        throw new DBException(msg);
    }

	/** Implementation of an key element.
	 * @see KeyElement
	 */
	public interface Impl extends DBMemberElement.Impl {
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
	}

	static class Memory extends DBMemberElement.Memory implements Impl {
		/** collection of columns */
		private DBMemoryCollection.Column columns;

        /** Default constructor
         */
		Memory() {
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory(KeyElement key) {
			super(key);
		}

		/** Changes set of elements.
		 * @param elems elements to change
		 * @exception SourceException if the action cannot be handled
		 */
		public synchronized void changeColumns(ColumnElement[] elems, int action)  {
			initColumns();
			columns.change(elems, action);
		}

		/** Gets all columns.
		* @return the columns
		*/
        public synchronized ColumnElement[] getColumns() {
			initColumns();
			return (ColumnElement[])columns.getElements();
        }

		/** Finds a column with given name.
		 * @param name the name of column for which to look
		 * @return the element or null if column with such name does not exist
		 */
        public synchronized ColumnElement getColumn(DBIdentifier name) {
			initColumns();
			return (ColumnElement)columns.getElement(name);
        }

        /** Initializes the collection of columns.
         */
		void initColumns() {
			if (columns == null)
				columns = new DBMemoryCollection.Column(this);
		}
	}
}
