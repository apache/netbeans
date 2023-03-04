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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

import org.netbeans.modules.db.sql.visualeditor.Log;

import org.netbeans.modules.db.sql.visualeditor.querymodel.Query;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Select;
import org.netbeans.modules.db.sql.visualeditor.querymodel.From;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Where;
import org.netbeans.modules.db.sql.visualeditor.querymodel.GroupBy;
import org.netbeans.modules.db.sql.visualeditor.querymodel.OrderBy;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Having;
import org.netbeans.modules.db.sql.visualeditor.querymodel.JoinTable;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Table;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Expression;
import org.netbeans.modules.db.sql.visualeditor.querymodel.ColumnProvider;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;
import org.netbeans.modules.db.sql.visualeditor.querymodel.SQLQueryFactory;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Value;
import org.netbeans.modules.db.sql.visualeditor.querymodel.And;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Or;
import org.netbeans.modules.db.sql.visualeditor.querymodel.ExpressionList;

class QueryModel {

    // Fields

    private static boolean              DEBUG = false;
    private Query                       _query = null; // The parsed query produced by the parser
    private QueryBuilderMetaData        qbMetaData;
    private SQLIdentifiers.Quoter       quoter;

    // Constructors

    QueryModel(SQLIdentifiers.Quoter quoter) {
        this.quoter = quoter;
    }

    // Methods

    // Parse a query
    void parse (String cmd)
        throws org.netbeans.modules.db.sql.visualeditor.parser.ParseException
    {
        _query = SQLQueryFactory.parse(cmd);
    }


    // Generate the SQL string corresponding to this model

    String genText() {

        Log.getLogger().entering("QueryModel", "genText"); // NOI18N
        if (QueryModel.DEBUG)
            (new Throwable()).printStackTrace();
        if ( _query == null )
            return null;
        else
            return (_query.genText(quoter));
    }


    // Accessors/Mutators

    // Getters for the main query clauses

    Select getSelect() {
        return _query.getSelect();
    }

    From getFrom() {
        return _query.getFrom();
    }

    Where getWhere() {
        // This is the only case (so far) where we might use an accessor with a null query
        // Should put this check on every accessor/mutator
        return (_query == null) ? null :  _query.getWhere();
    }

    GroupBy getGroupBy() {
        return  _query.getGroupBy();
    }

    OrderBy getOrderBy() {
        return _query.getOrderBy();
    }

    Having getHaving() {
        return _query.getHaving();
    }

    void setSelect(Select select) {
        _query.setSelect(select);
    }

    void setFrom(From from) {
        _query.setFrom(from);
    }

    void setWhere(Where where) {
        _query.setWhere(where);
    }

    void setGroupBy(GroupBy groupBy) {
        _query.setGroupBy(groupBy);
    }

    void setOrderBy(OrderBy orderBy) {
        _query.setOrderBy(orderBy);
    }

    void setHaving(Having having) {
        _query.setHaving(having);
    }

    // Model manipulation methods

    // Remove any GroupBy clause in this query
    void removeGroupBy()
    {
        this.setGroupBy(null);
    }

    // returns true if the query has any GroupBy clause, false otherwise.
    boolean hasGroupBy()
    {
        return (_query.getGroupBy() != null);
    }

    // Insert a table into the model
    void insertTable(JoinTable joinTable) {
        _query.getFrom().addTable(joinTable);
    }


    // Update a JoinTable object by adding the associated condition to it

    /**
     * Remove a table from the model, along with all references to it
     */
    void removeTable (String tableSpec) {

        Log.getLogger().entering("QueryModel", "removeTable", tableSpec); // NOI18N
        _query.removeTable(tableSpec);
    }
    

    // If "*" appears in the SELECT list, replace it with an explicit
    // list of all columns from all tables, to make manipulation easier

    void replaceStar(ColumnProvider tableReader) {

        Log.getLogger().entering("QueryModel", "replaceStar"); // NOI18N
        _query.replaceStar(tableReader);
    }

    // Add an appropriate entry to the SELECT list
    // Tablespec may be either a corrName or fullTableName
    void addColumn(String tableSpec, String columnName) {
        _query.addColumn(tableSpec, columnName);
    }

    void removeColumn(String tableSpec, String columnName) {
        _query.removeColumn(tableSpec, columnName);
    }

    // Return a condition in a WHERE clause that references the specified tables and columns
    Expression findCond(String table1, String column1, String table2, String column2) {
        return _query.getWhere().findExpression(table1, column1, table2, column2);
    }
    

    // Find the Table object with this tablespec
    Table findTable(String tableSpec) {
        if ((_query != null) && (_query.getFrom() != null) )
            return _query.getFrom().findTable(tableSpec);
        else
            return null;
    }
    

    Column findSelectColumn(String tableSpec, String columnName) {
        List<Column> columns = new ArrayList<>();
        _query.getSelect().getReferencedColumns(columns);
        for (int i=0; i<columns.size(); i++) {
            Column col = columns.get(i);
            if (col.matches(tableSpec, columnName))
                return col;
        }
        return null;
    }
    
    // Return the full name of the table associated with this tableSpec 
    // (corrName or tableName)
    // If necessary, we could maintain a separate HashMap with this info
    String getFullTableName(String corrName) {
        if ((_query != null) && (_query.getFrom() != null) && (corrName != null))
            return _query.getFrom().getFullTableName(corrName);
        else
            return null;
    }
    

    // Return null, indicating this tableName is unique, or a new correlation name

    String genUniqueName(String fullTableName) {

        if (findTable(fullTableName)==null)
            // tableName is unused
            return null;
        else
        {
            // If we have <schame>.<table>, just use the tablename
            String[] table = fullTableName.split("\\.");
            String tableName=(table.length>1) ? table[1] : table[0];
            String candidate; 
            for (int i=1;;i++) {

                candidate = tableName+"_"+i;    // NOI18N
                /*
                if (tableName.indexOf(' ') == -1 ) {
                    candidate = tableName+"_"+i;    // NOI18N
                }
                else {
                    candidate = "\"" + tableName.replaceAll ("\"", "")+"_"+i + "\"" ;    // NOI18N
                }
                */

                if (findTable(candidate)==null)
                    // Found an unused correlation name
                    return candidate;
            }
        }
    }
    
    // Add or modify a SortSpecification entry in the OrderBy
    void addSortSpecification(String tableSpec, String columnName, String direction, int order) {
        OrderBy orderBy = this.getOrderBy();
        if (orderBy == null) {
            orderBy = SQLQueryFactory.createOrderBy();
            this.setOrderBy(orderBy);
        }

        // Remove the sort spec for this column if there was one
        orderBy.removeSortSpecification(tableSpec, columnName);

        // Insert the new one in an appropriate place
        orderBy.addSortSpecification(tableSpec, columnName, direction, order);
    }

    // Remove a SortSpecification entry in the OrderBy
    void removeSortSpecification(String tableSpec, String columnName) {
        OrderBy ob = this.getOrderBy();
        if (ob != null) 
            ob.removeSortSpecification(tableSpec, columnName);
    }


    /*
     * Returns the number of sort specifications current in place
     */
    int getSortCount() {
        OrderBy orderBy = this.getOrderBy();
        if (orderBy==null)
            return 0;
        else
            return orderBy.getSortSpecificationCount();
    }


    // Set the column alias (derived name) associated with the given column in SELECT
    void setDerivedColName(String tableSpec, String columnName, String derivedColName) {
        Column col = findSelectColumn(tableSpec, columnName);
        if (col!=null)
            col.setDerivedColName(derivedColName);
    }
    
    // Remove the column alias (derived name) associated with the given column in SELECT
    void removeDerivedColName(String tableSpec, String columnName) {
        setDerivedColName(tableSpec, columnName, null);
    }
    
    /**
     * Return true if the query contains any parameters ("?"), false otherwise
     */
    boolean isParameterized() {
        // search through the where clause
        // examine every predicate
        // if it contains a literal "?", the answer is true
        Where where = this.getWhere();
        if (where==null)
            return false;
        else
            return where.isParameterized();
        
    }
    
    /**
     * Fill the collection with the parameterized expressions
     * e.g. if the expression is like "a.x=? AND b.x <= 10 OR c.x <> ?", 
     * collection will be filled with "a.x and c.x"
     */
    void getParameterizedPredicates(Collection predicates) {
        // search through the where clause
        Where where = this.getWhere();
        if (where==null)
            return;
        else {
            Expression expr = where.getExpression();
            getParameterized (expr, predicates); 
        }
        return; 
    } 
    
    private void getParameterized (Expression expr, Collection predicates) {
        if ( expr instanceof ExpressionList ) {
            for (int i=0; i<((ExpressionList)expr).size();i++) {
                Expression e = ((ExpressionList)expr).getExpression(i); 
                getParameterized (e, predicates);
            }
        }
        else if ( expr.isParameterized() && (expr instanceof Predicate) ) {
                Predicate pred = (Predicate) expr;
                Value val1 = pred.getVal1();
                Value val2 = pred.getVal2();
//              System.out.println("Entering QueryModel.getParameterized() val1 = " + val1.genText() + " val2 = " + val2.genText()); // NOI18N
                // we will support only a.x = ?  OR
                // a.x IN ( ?, ?, ? ) for thresher.
                if ( val1.isParameterized() ) {
                    String val1String = val1.genText(quoter);
                    for (int i=0; i<val1String.length(); i++) {
                        if (val1String.charAt(i) == '?' )
                            predicates.add(val2.genText(quoter));
                    }
                }
                else if ( val2.isParameterized() ) {
                    String val2String = val2.genText(quoter);
                    for (int i=0; i<val2String.length(); i++) {
                        if (val2String.charAt(i) == '?' )
                            predicates.add(val1.genText(quoter));
                    }
                }
        }

    }

    /**
     * Replace every instance of the given tableSpec with the new one
     * The old one may be either a tableName or previous corrName
     * The new one will be set as a corrName
     */
    void renameTableSpec(String oldTableSpec, String corrName) {
        // Find all the places where a tableSpec can occur, and replace
        // This seems to be most of the query
        Log.getLogger().entering("QueryModel", "renameTableSpec", new Object[] { oldTableSpec, corrName } ) ; // NOI18N
        _query.renameTableSpec(oldTableSpec, corrName);
    }


    // Return a jointable that references the specified tables and columns
    // ToDo: decide some canonical order or other simplification
    // ToDo: come up with a better predicate here
    JoinTable findJoinTable(String table1, String column1, String table2, String column2) {
        return _query.getFrom().findJoinTable(table1, column1, table2, column2);
    }
    
    public void getColumnNames (String tableSpec, Collection columnNames) {
        ArrayList columns = new ArrayList();
        this.getSelect().getReferencedColumns(columns);
        for (int i=0; i<columns.size(); i++) {
            Object element = columns.get(i);
            if (element instanceof Column) {
                Column col = (Column) element;
                if (col != null) {
                    String colName = col.getColumnName();
                    String tabSpec =  col.getTableSpec();
                    if ( (colName != null) && (tabSpec != null) )
                        if (colName.equals("*") || tabSpec.equals(tableSpec))  // NOI18N
                            columnNames.add(colName);
                }
            }
        }
    }
    
    // Add a GroupBy clause
    // Include all columns that are mentioned in the select clause
    void addGroupBy()
    {
        ArrayList columns = new ArrayList();
        this.getSelect().getReferencedColumns(columns);
        this.setGroupBy(SQLQueryFactory.createGroupBy(columns));
    }

    // Insert the relationships that go with this table
    // This depends on modifying the joinTable object that we previously inserted
    // into the model.
    void addRelationships(JoinTable joinTable, List relationships) {

        if (relationships.isEmpty()) 
            return;
        else {
            // There are multiple foreign keys between the new table and current ones 
            // Add the first one as a JOIN condition if possible, and the rest to the WHERE clause
            String[] rel = (String[]) relationships.get(0);

            // get the corr name 

            String corrName1 = _query.getFrom().getTableSpec(rel[0]);
            String corrName2 = _query.getFrom().getTableSpec(rel[2]);
            
            String lastTableFullName = _query.getFrom().getPreviousTableFullName();
            if ((rel[0].equals(lastTableFullName)) || rel[2].equals(lastTableFullName))  {
                joinTable.addJoinCondition(rel);
                /*
                if ( !corrName1.equals(rel[0]) )
                    joinTable.getExpression().renameTableSpec(rel[0], corrName1);

                if ( !corrName2.equals(rel[2]) )
                    joinTable.getExpression().renameTableSpec(rel[2], corrName2);
                */
            }
            else
                // Add to the WHERE clause instead
                addOrCreateAndExpression(SQLQueryFactory.createPredicate(rel));

            for (int i=1; i<relationships.size(); i++) {
                
                // We need to construct a predicate from the edge info
                rel = (String[]) relationships.get(i);
                addOrCreateAndExpression(SQLQueryFactory.createPredicate(rel));
            }
        }
    }

    // Remove a predicate from the WHERE clause
    // This method is used only to remove the joins
    // So all the 'getVal' methods on predicate should
    // return an instance of Column
    void removeCondition (Predicate removePred) {

        Where where = this.getWhere();

        if ( where == null || where.getExpression() == null )
            return;
        else {
            Expression expr = this.getWhere().getExpression();
            expr = this.removeConditionFromExpression(removePred, expr);
            if ( expr == null ) {
                where.resetExpression();
                _query.setWhere(null);
            }
            else {
                where.replaceExpression(expr);
            }
        }
    }
    
    // return the expression the caller should insert in its list if any.
    // The caller should chek for null and if null is returned it should delete the entry
    // otherwise it should replace the entry with whatever is coming back
    private Expression removeConditionFromExpression (Predicate removePred, Expression expr) {
        Value removeVal1 = removePred.getVal1();
        Value removeVal2 = removePred.getVal2();

        if (expr instanceof Predicate) {
            Predicate pred = (Predicate) expr;
            Value val1 = pred.getVal1();
            Value val2 = pred.getVal2();

            if ( (val1 instanceof Column) && 
                 (val2 instanceof Column) &&
                 (removeVal1 instanceof Column) && 
                 (removeVal2 instanceof Column) ) {
                Column col1 = (Column)val1;
                Column col2 = (Column)val2;
                Column removeCol1 = (Column)removeVal1;
                Column removeCol2 = (Column)removeVal2;
                if ( col1.equals(removeCol1) && col2.equals(removeCol2) ) {
                    return null;
                }
            }
        }
        else if ( expr instanceof ExpressionList ) {

            ExpressionList exprList = (ExpressionList) expr;
            for (int i=exprList.size()-1; i>=0 ; i--) {
                Expression element = this.removeConditionFromExpression(removePred, exprList.getExpression(i));
                if ( element == null ) {
                    exprList.removeExpression(i);
                }
                else {
                    exprList.replaceExpression(i, element);
                }
            }
            
            // make return value proper for the caller to take proper action
            int size = exprList.size();
            if ( size == 0 ) {
                return null;
            }
            else if ( size == 1 ) {
                return exprList.getExpression(0);
            }
        }
        return expr;
    }
    
    /*
     * Returns the number of criteria specifications current in place
     */
    int getCriteriaCount() {
        Where where = this.getWhere();
        if (where != null) {
            Expression expr = where.getExpression();
            if (expr != null) {
                if (expr instanceof And)
                    return ((ExpressionList)expr).size();
                else
                    return 1;
            } 
        }
        return 0;
    }

    // Remove join node by setting the join type and expression to null.
    void removeJoinNode ( String table1, String column1, String table2, String column2) {
        if ( this.getFrom() != null ) {
            JoinTable jt = this.getFrom().findJoinTable ( table1, column1, table2, column2 );
            if ( jt != null ) {
                // remove join type
                jt.setJoinType (null);
                // remove join expression
                jt.setExpression (null);
            }
        }

    }

    // Remove a criteria associated with tableSpec, columnName and order 
    void removeCriteria (String tableSpec, String columnName, int recurseLevel) {
        Where where = this.getWhere();
        if (where != null) {
            
            Expression expr = where.getExpression();
            if ( expr == null )
                return;

            expr = removeCriteriaFromExpression(tableSpec, columnName, expr, recurseLevel);
            if (expr == null) {
                where.resetExpression();
                _query.setWhere(null);
            }
            else 
                where.replaceExpression(expr);
        }
    }
    
    // return the expression the caller should insert in its list if any.
    // The caller should chek for null and if null is returned it should delete the entry
    // otherwise it should replace the entry with whatever is coming back
    private Expression removeCriteriaFromExpression(String tableSpec, String columnName, Expression expr, int recurseLevel) {
        if (expr instanceof Predicate) {

            Predicate pred = (Predicate) expr;
            Value val1 = pred.getVal1();
            Value val2 = pred.getVal2();

            if (pred.isCriterion()) { // Don't remove relationships

                if (((val1 instanceof Column) && ((Column)val1).matches(tableSpec, columnName)) 
                    || ((val2 instanceof Column) && ((Column)val2).matches(tableSpec, columnName)))
                {
                    // has to erase element in caller
                    return null;
                }
            }
        }
        else if ( recurseLevel > 0 && expr instanceof ExpressionList ) {

            recurseLevel--;
            ExpressionList exprList = (ExpressionList) expr;
            for (int i=exprList.size()-1; i>=0; i--) {
                Expression item = removeCriteriaFromExpression(tableSpec, columnName, exprList.getExpression(i), recurseLevel);
                if (item == null) {
                    exprList.removeExpression(i);
                }
                else {
                    exprList.replaceExpression(i, item);
                }
            }
            
            int size = exprList.size();
            if ( size == 0 ) {
                // has to erase element in caller
                return null;
            }
            else if ( size == 1 ) {
                return exprList.getExpression(0);
            }
        }
        
        return expr;
    }

    // Add or modify a Criteria entry in the Where clause, including the order 

    void addCriteria(String tableSpec, String columnName, Predicate pred) {

        if (QueryModel.DEBUG)
            System.out.println("Entering QueryModel.addCriteriaOrder () tableSpec = " + tableSpec // NOI18N
                               + " ColumnName = " + columnName + " Predicate = " + pred // NOI18N
                               + "\n");  // NOI18N

        Where where = this.getWhere();
        if (where==null) {
            this.setWhere(SQLQueryFactory.createWhere(pred));
        }
        else 
        {
            Expression expr = where.getExpression();
            if (expr == null) {
                // if there is no predicate , add one
                where.replaceExpression(pred);
            }
            else {
                addOrCreateAndExpression(pred);
            }
        }
    }

    void replaceCriteria(String tableSpec, String columnName, Predicate pred, int order) {

        order--; // it's not 0 based
        if (QueryModel.DEBUG)
            System.out.println("Entering QueryModel.replaceCriteria () tableSpec = " + tableSpec // NOI18N
                               + " ColumnName = " + columnName + " Predicate = " + pred // NOI18N
                               + " Order = " + order + "\n");  // NOI18N

        Where where = this.getWhere();
        Expression expr = where.getExpression();
        if (expr instanceof ExpressionList)
            ((ExpressionList)expr).replaceExpression(order, pred);
        else {
            // order must be 1
            where.replaceExpression(pred);
        }
    }

    void addCriteria(String tableSpec, String columnName, Predicate pred, int order) {

        order--; // it's not 0 based
        if (QueryModel.DEBUG)
            System.out.println("Entering QueryModel.addCriteriaOrder () tableSpec = " + tableSpec // NOI18N
                               + " ColumnName = " + columnName + " Predicate = " + pred // NOI18N
                               + " Order = " + order + "\n");  // NOI18N

        Where where = this.getWhere();
        Expression expr = where.getExpression();
        if (expr instanceof And) {
            ((ExpressionList)expr).addExpression(order, pred);
        }
        else {
            if (expr == null)
                where.replaceExpression(pred);
            else {
                if (order == 0)
                    expr = SQLQueryFactory.createAnd(pred, expr);
                else
                    expr = SQLQueryFactory.createAnd(expr, pred);
                this.getWhere().replaceExpression(expr);
            }
        }
    }

    // Add a predicate to the WHERE clause
    // As of now, it must be the only predicate; eventually we'll support AND
    void addOrCreateAndExpression (Predicate pred) {
        if (this.getWhere()!=null) {
            Expression expr = this.getWhere().getExpression();
            if (expr != null) {
                if (expr instanceof And) {
                    And andExpr = (And)expr;
                    andExpr.addExpression(pred);
                }
                else {
                    expr = SQLQueryFactory.createAnd(expr, pred);
                    this.getWhere().replaceExpression(expr);
                }
                return;
            }
        }
        this.setWhere(SQLQueryFactory.createWhere(pred));
    }

    // Add a predicate to the WHERE clause
    // As of now, it must be the only predicate; eventually we'll support AND
    void addOrCreateOrExpression (Predicate pred) {
        if (this.getWhere()!=null) {
            Expression expr = this.getWhere().getExpression();
            if (expr != null) {
                if (expr instanceof Or) {
                    Or orExpr = (Or)expr;
                    orExpr.addExpression(pred);
                    return;
                }
                else {
                    expr = SQLQueryFactory.createOr(expr, pred);
                    this.getWhere().replaceExpression(expr);
                    return;
                }
            }
        }
        this.setWhere(SQLQueryFactory.createWhere(pred));
    }

}



