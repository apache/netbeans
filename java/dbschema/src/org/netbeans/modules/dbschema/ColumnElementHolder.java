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

/** Describes an object which holds a list of columns.
 */
public interface ColumnElementHolder {
	/** Add a new column to the holder.
	 *  @param el the column to add
	 * @throws Exception if impossible
	 */
	public void addColumn (ColumnElement el) throws Exception;

	/** Add some new columns to the holder.
	 *  @param els the columns to add
	 * @throws Exception if impossible
	 */
	public void addColumns (ColumnElement[] els) throws Exception;

	/** Remove a column from the holder.
	 *  @param el the column to remove
	 * @throws Exception if impossible
	 */
	public void removeColumn (ColumnElement el) throws Exception;

	/** Remove some columns from the holder.
	 *  @param els the columns to remove
	 * @throws Exception if impossible
	 */
	public void removeColumns (ColumnElement[] els) throws Exception;

	/** Set the columns for this holder.
	 * Previous columns are removed.
	 * @param els the new columns
	 * @throws Exception if impossible
	 */
	public void setColumns (ColumnElement[] els) throws Exception;

	/** Get all columns in this holder.
	 * @return the columns
	 */
	public ColumnElement[] getColumns ();

	/** Find a column by name.
	 * @param name the name of the column for which to look
	 * @return the element or <code>null</code> if not found
	 */
	public ColumnElement getColumn (DBIdentifier name);
}
