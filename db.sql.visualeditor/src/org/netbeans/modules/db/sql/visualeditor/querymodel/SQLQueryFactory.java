/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
