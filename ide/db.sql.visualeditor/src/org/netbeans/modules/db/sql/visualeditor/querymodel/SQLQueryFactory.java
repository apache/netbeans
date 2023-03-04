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

import org.netbeans.modules.db.sql.visualeditor.parser.SQLParser;
import org.netbeans.modules.db.sql.visualeditor.parser.ParseException;
import java.util.ArrayList;

public class SQLQueryFactory {

    public static Query parse(String query) throws ParseException {
        SQLParser parser = new SQLParser(new java.io.StringReader(query));
        return (Query)parser.SQLQuery();
    }

    public static Where createWhere(Expression expr) {
        return new WhereNode(expr);
    }

    public static Predicate createPredicate(Value val1, Value val2, String op) {
        return new Predicate(val1, val2, op);
    }

    public static Predicate createPredicate(Value val1, Object literal, String op) {
        Literal val2 = new Literal(literal);
        return new Predicate(val1, val2, op);
    }

    public static Predicate createPredicate(String[] rel) {
        return new Predicate(rel);
    }

    public static GroupBy createGroupBy(ArrayList columnList) {
        return new GroupByNode(columnList);
    }

    public static OrderBy createOrderBy() {
        return new OrderByNode();
    }

    public static Column createColumn(String tableSpec, String columnName) {
        return new ColumnNode(tableSpec, columnName);
    }

    public static Literal createLiteral(Object value) {
        return new Literal(value);
    }

    public static Table createTable(String tableName, String corrName, String schemaName) {
        return new TableNode(tableName, corrName, schemaName);
    }

    public static JoinTable createJoinTable(Table table) {
        return new JoinTableNode((TableNode)table);
    }

    public static And createAnd(Expression expr1, Expression expr2) {
        ArrayList items = new ArrayList();
        items.add(expr1);
        items.add(expr2);
        return new AndNode(items);
    }

    public static Or createOr(Expression expr1, Expression expr2) {
        ArrayList items = new ArrayList();
        items.add(expr1);
        items.add(expr2);
        return new OrNode(items);
    }
}
