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

/** Describes a reference key in a table.
 */
public interface ReferenceKey extends ColumnPairElementHolder {
	//================== Naming ===============================

    /** Get the name of this element.
     * @return the name
     */
    public String getKeyName();
	
    /** Set the name of this element.
    * @param name the name
    * @throws Exception if impossible
    */
    public void setKeyName (String name) throws Exception;


	//================== Tables ===============================

	/** Get the declaring table. 
	 * @return the table that owns this reference key element, or 
	 * <code>null</code> if the element is not attached to any table
	 */
	public TableElement getDeclaringTable ();

	/** Set the declaring table. 
    * @param te the table to set
	 */
	public void setDeclaringTable (TableElement te);

	/** Get the referenced table of the reference key.
	 * @return the referenced table
	 */
	public TableElement getReferencedTable();


	//================== Columns ===============================

	// column convenience methods

	/** Get all referenced columns in this reference key.
	 * @return the columns
	 */
	public ColumnElement[] getReferencedColumns ();
    
	/** Get all local columns in this reference key.
	 * @return the columns
	 */
	public ColumnElement[] getLocalColumns();

	// end column convenience methods
}
