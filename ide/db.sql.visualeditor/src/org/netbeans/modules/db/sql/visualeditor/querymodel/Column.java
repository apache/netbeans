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

import java.util.List;

// Identify a Column.
// It derives from Value which derives from Expression.
// REVIEW: not clear we need all these methods, need a clean up
public interface Column extends Value {

    //
    // this first 3 methods are used for equality. Equality is based on string equality and not object identity
    //
    public boolean matches(String table, String column);
    public boolean matches(String table);
    public boolean equals(Column column);

    // getters
    public String getColumnName();
    public String getTableSpec();
    public String getFullTableName();
    public String getDerivedColName();

    // modifiers
    public void setDerivedColName(String derivedColName);
    public void setTableSpec(String oldTableSpec, String newTableSpec);
    public void setColumnName(String oldColumnName, String newColumnName);
    public void setColumnTableName(String tableName);
    public void setColumnCorrName(String corrName);
}

