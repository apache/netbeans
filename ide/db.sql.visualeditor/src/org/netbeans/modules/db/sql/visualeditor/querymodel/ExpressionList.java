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

/*
 * ExpressionList.java
 *
 * Created on March 23, 2005, 2:11 PM
 */

// AND and OR operators are defined as list of expressions.
// The reason is mostly due to the need of the editor to see expressions in a "linear" form and changing this to be a binary expression (which it should be)
// is too much of a change
// The api here is very similar to that of a List though it is properly typed.
public interface ExpressionList extends Expression {
    public int size();
    public Expression getExpression(int i);
    public void addExpression(int index, Expression expression);
    public void addExpression(Expression expression);
    public void replaceExpression(int index, Expression expression);
    public void removeExpression(int index);
    public void removeTable(String tableSpec);
}
