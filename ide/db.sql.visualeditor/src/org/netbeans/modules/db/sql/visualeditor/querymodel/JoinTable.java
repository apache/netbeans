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

public interface JoinTable extends QueryItem {
    public void addJoinCondition(String[] rel);


    // verify
    public String getFullTableName();

    public Table getTable();
    public String getTableName();
    public String getCorrName();
    public String getTableSpec();
    public String getJoinType();
    public void setJoinType(String joinType);
    public Expression getExpression();
    public void setExpression(Expression condition);
}






