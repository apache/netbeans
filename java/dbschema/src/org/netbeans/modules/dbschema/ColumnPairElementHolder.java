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

/** Describes an object which holds a list of column pairs.
 */
public interface ColumnPairElementHolder {
	/** Add a new column pair to the holder.
	 *  @param pair the pair to add
	 * @throws Exception if impossible
	 */
	public void addColumnPair (ColumnPairElement pair) throws Exception;

	/** Add some new column pairs to the holder.
	 *  @param pairs the column pairs to add
	 * @throws Exception if impossible
	 */
	public void addColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Remove a column pair from the holder.
	 *  @param pair the column pair to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPair (ColumnPairElement pair) throws Exception;

	/** Remove some column pairs from the holder.
	 *  @param pairs the column pairs to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Set the column pairs for this holder.
	 * Previous column pairs are removed.
	 * @param pairs the new column pairs
	 * @throws Exception if impossible
	 */
	public void setColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Get all column pairs in this holder.
	 * @return the column pairs
	 */
	public ColumnPairElement[] getColumnPairs ();

	/** Find a column pair by name.
	 * @param name the name of the column pair for which to look
	 * @return the column pair or <code>null</code> if not found
	 */
	public ColumnPairElement getColumnPair (DBIdentifier name);
}
