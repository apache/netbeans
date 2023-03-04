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

/** Describes a unique key in a table.
 */
public final class UniqueKeyElement extends KeyElement {
	/** the index to which this element is associated */
	private IndexElement _associatedIndex;

	/** Creates a new unique key element represented in memory.
	 */
	public UniqueKeyElement() {
		this(new Memory(), null, null);
	}

	/** Creates a new unique key element.
	 * @param impl the pluggable implementation
	 * @param declaringTable declaring table of this unique key, or <code>null</code>
     * @param associatedIndex the associated index
	 */
	public UniqueKeyElement(Impl impl, TableElement declaringTable, IndexElement associatedIndex) {
		super(impl, declaringTable);

        _associatedIndex = associatedIndex;
	}

    /** Returns the implementation for the unique key.
	 * @return implementation for the unique key
	 */
	final Impl getUniqueKeyImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the associated index of the unique key.
	 * @return the associated index for this unique key, <code>null</code>
	 * if unattached
	 */
	public IndexElement getAssociatedIndex() {
        return _associatedIndex;
    }

	/** Sets the associated index of the unique key.
	 * @param the associated index for this unique key
	 * @throws DBException if impossible
	 */
	public void setAssociatedIndex(IndexElement index) throws DBException {
		_associatedIndex = index;
	}

	/** Gets the primary key flag of the unique key.
	 * @return true if this unique key is a primary key, false otherwise
	 */
	public boolean isPrimaryKey() {
        return getUniqueKeyImpl().isPrimaryKey();
    }

	/** Sets the primary key flag of the unique key.
	 * @param flag the flag
	 * @throws DBException if impossible
	 */
	public void setPrimaryKey (boolean flag) throws DBException {
		getUniqueKeyImpl().setPrimaryKey(flag);
	}

	/** Implementation of a unique key element.
	 * @see KeyElement
	 */
	public interface Impl extends KeyElement.Impl {
		/** Gets the primary key flag of the unique key.
		 * @return true if this unique key is a primary key, false otherwise
		 */
		public boolean isPrimaryKey ();

		/** Sets the primary key flag of the unique key.
		 * @param flag the flag
		 * @throws DBException if impossible
		 */
		public void setPrimaryKey (boolean flag) throws DBException;
	}

	static class Memory extends KeyElement.Memory implements Impl {
		/** Primary key flag of key */
		private boolean _pk;

        /** Default constructor
         */
		Memory () {
			_pk = false;
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory (UniqueKeyElement key) {
			super(key);
			_pk = key.isPrimaryKey();
		}

		/** Gets the primary key flag of the unique key.
		 * @return true if this unique key is a primary key, false otherwise
		 */
		public boolean isPrimaryKey() {
            return _pk;
        }

		/** Sets the primary key flag of the unique key.
		 * @param flag the flag
		 * @throws DBException if impossible
		 */
		public void setPrimaryKey (boolean flag) throws DBException {
			boolean old = _pk;

			_pk = flag;
			firePropertyChange(PROP_PK, Boolean.valueOf(old), Boolean.valueOf(flag));
		}
	}
}
