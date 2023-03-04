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

package org.netbeans.modules.dbschema.nodes;

/** Orders and filters members in a table element node.
* Can be used for columns, indexes, etc.
*/
public class TableElementFilter extends SchemaElementFilter {
    /** Specifies a child representing a column. */
    public static final int       COLUMN = 4;
    /** Specifies a child representing an index. */
    public static final int     INDEX = 8;
    /** Specifies a child representing a foreign key. */
    public static final int     FK = 16;
    /** Specifies a child representing a column pair. */
    public static final int     COLUMN_PAIR = 32;
    /** Does not specify a child type. */
    public static final int     ALL = SchemaElementFilter.ALL | COLUMN | COLUMN_PAIR | INDEX | FK;

    /** Default order and filtering.
    * Places all columns, indexes, and foreign keys together in one block.
    */
    public static final int[] DEFAULT_ORDER = {COLUMN | COLUMN_PAIR | INDEX | FK };
    
    /** stores property value */
    private boolean sorted = true;
  
    /** Test whether the elements in one element type group are sorted.
    * @return <code>true</code> if groups in getOrder () field are sorted, <code>false</code> 
    * to default order of elements
    */
    public boolean isSorted () {
        return sorted;
    }

    /** Set whether groups of elements returned by getOrder () should be sorted.
    * @param sorted <code>true</code> if so
    */
    public void setSorted (boolean sorted) {
        this.sorted = sorted;
    }
}
