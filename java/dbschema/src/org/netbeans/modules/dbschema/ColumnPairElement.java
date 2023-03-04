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

/** Describes a directed column pair.  Because of its direction, it has
 * the notion of belonging to a table.
 */
public final class ColumnPairElement extends DBMemberElement  {
	/** the local column to which this element is associated */
	private ColumnElement _localColumn;

	/** the referenced column to which this element is associated */
	private ColumnElement _referencedColumn;

	/** Create a new column pair element represented in memory.
	 */
	public ColumnPairElement() {
		this(new Memory(), null, null, null);
	}

	/** Creates a new column pair element.
	 * @param localColumn local column of this column pair
     * @param referencedColumn referenced column of this column pair
	 * @param declaringTable declaring table of this column pair, or <code>null</code>
	 */
	public ColumnPairElement (ColumnElement localColumn,  ColumnElement referencedColumn, TableElement declaringTable) {
		this(new Memory(), localColumn, referencedColumn, declaringTable);
	}

	/** Creates a new column pair element.
	 * @param impl the pluggable implementation
	 * @param localColumn local column of this column pair
     * @param referencedColumn referenced column of this column pair
	 * @param declaringTable declaring table of this column pair, or <code>null</code>
	 */
	public ColumnPairElement (ColumnPairElement.Impl impl, ColumnElement localColumn, ColumnElement referencedColumn, TableElement declaringTable) {
		super(impl, declaringTable);
		_localColumn = localColumn;
		_referencedColumn = referencedColumn;
	}

    /** Returns the implementation for the column pair.
     * @return implementation for the column pair
     */
	final Impl getColumnPairImpl() {
        return (Impl)getElementImpl();
    }

	/** Gets the local column. 
	 * @return the local column of this column pair
	 */
	public final ColumnElement getLocalColumn() {
		return _localColumn;
	}

	/** Sets the local column. 
	 * @param ce the local column
	 */
	public final void setLocalColumn (ColumnElement ce) {
		if (_localColumn == null)
            _localColumn = ce;
	}

	/** Gets the referenced column. 
	 * @return the referenced column of this column pair
	 */
	public final ColumnElement getReferencedColumn() {
		return _referencedColumn;
	}

	/** Sets the referenced column. 
	 * @param ce the referenced column
	 */
	public final void setReferencedColumn(ColumnElement ce) {
		if (_referencedColumn == null)
            _referencedColumn = ce;
	}

    /** Gets the name of this element.
     * @return the name
     */
    public DBIdentifier getName() {
        ColumnElement lce = getLocalColumn();
        ColumnElement fce = getReferencedColumn();
        
        DBIdentifier name = DBIdentifier.create(lce.getName().getFullName() + ";" + fce.getName().getFullName()); //NOI18N
        
		return name;
	}

	/** Implementation of a reference key column element.
	 * @see DBMemberElement
	 */
	public interface Impl extends DBMemberElement.Impl {
	}

	static class Memory extends DBMemberElement.Memory implements Impl {
        /** Default constructor.
         */
		Memory() {
		}

		/** Copy constructor.
		* @param column the object from which to read values
		*/
		Memory(ColumnPairElement column) {
			super(column);
		}
	}
}
