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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

// The comon base type of every item present in a query

public interface  QueryItem {

    // generate text that represent the item
    public String genText(SQLIdentifiers.Quoter quoter);

    // walks recursively the specific item to find all teh columns that are referenced from this item.
    // For instance, called on a WHERE cluase will return all teh columns used in the expression of the WHERE clause
    // could be used by the editor to obtain info on all teh column used in a particular clause
    public void getReferencedColumns(Collection columns);

}

