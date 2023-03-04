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

// A QUERY clause
// This is the main interface representing the query statement
public interface Query extends QueryItem {
    public Select getSelect();
    public void setSelect(Select select);
    public From getFrom();
    public void setFrom(From from);
    public Where getWhere();
    public void setWhere(Where where);
    public GroupBy getGroupBy();
    public void setGroupBy(GroupBy groupBy);
    public OrderBy getOrderBy();
    public void setOrderBy(OrderBy orderBy);
    public Having getHaving();
    public void setHaving(Having having);

    public void removeTable(String tableSpec);
    public void renameTableSpec(String oldTableSpec, String corrName);

    public void replaceStar(ColumnProvider tableReader);

    public void addColumn(String tableSpec, String columnName);
    public void removeColumn(String tableSpec, String columnName);
}
