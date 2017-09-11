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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

public class QueryNode implements Query {

    // Fields

    SelectNode  _select;
    FromNode    _from;
    WhereNode   _where;
    GroupByNode _groupBy;
    HavingNode  _having;
    OrderByNode _orderBy;


    // Constructors

    public QueryNode() {
    }

    public QueryNode(SelectNode select, FromNode from, WhereNode where,
                 GroupByNode groupBy, HavingNode having, OrderByNode orderBy) {
        _select = select;
        _from = from;
        _where = where;
        _groupBy = groupBy;
        _having = having;
        _orderBy = orderBy;
    }

    public QueryNode(SelectNode select, FromNode from) {
        this(select, from, null, null, null, null);
    }


    // Methods

    // Generate the SQL string corresponding to this model

    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = _select.genText(quoter) + " " + _from.genText(quoter);    // NOI18N

        if (_where!=null)
            res += _where.genText(quoter);

        if (_groupBy!=null)
            res += _groupBy.genText(quoter);

        if (_having!=null)
            res += _having.genText(quoter);

        if (_orderBy!=null)
            res += _orderBy.genText(quoter);

        return res;
    }


    // Dump out the model, for debugging purposes

    public String toString() {
        return (_select.toString() +
                _from.toString() +
                _where.toString() );
    }


    // Accessors/Mutators

    public Select getSelect() {
        return _select;
    }

    public void setSelect(Select select) {
        _select = (SelectNode)select;
    }

    public From getFrom() {
        return _from;
    }

    public void setFrom(From from) {
        _from = (FromNode)from;
    }

    public Where getWhere() {
        return _where;
    }

    public void setWhere(Where where) {
        _where = (WhereNode)where;
    }

    public GroupBy getGroupBy() {
        return _groupBy;
    }

    public void setGroupBy(GroupBy groupBy) {
        _groupBy = (GroupByNode)groupBy;
    }

    public OrderBy getOrderBy() {
        return _orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        _orderBy = (OrderByNode)orderBy;
    }

    public Having getHaving() {
        return _having;
    }

    public void setHaving(Having having) {
        _having = (HavingNode)having;
    }

    public void removeTable (String tableSpec) {
        // Find the FROM clause for this tableName, and remove it
        _from.removeTable(tableSpec);

        // ToDo: Remove any other joins that mention this table?

        // Find any SELECT targets for this tableName, and remove them
        _select.removeTable(tableSpec);

        // Find any WHERE clauses that mention this table, and remove them
        if (_where!=null) {
            _where.removeTable(tableSpec);
            if (_where.getExpression() == null)
                _where = null;
        }

        // Find any GROUPBY clauses that mention this table, and remove them
        if (_groupBy!=null)
        {
            _groupBy.removeTable(tableSpec);
            if (_from._tableList.size() == 0)
                _groupBy = null;
        }
        removeSortSpecification(tableSpec);
    }

    public void replaceStar(ColumnProvider tableReader) {
        if (_select.hasAsteriskQualifier()) {  // NOI18N

            // Hack - if there's a star, just replace the whole list
            ArrayList columns = new ArrayList();

            // Get the list of table objects from FROM
            ArrayList tables = _from.getTables();

            // Iterate through it
            for (int i=0; i<tables.size(); i++) {
                TableNode tbl = (TableNode) tables.get(i);
                String fullTableName = tbl.getFullTableName();
		List columnNames = new ArrayList();
		tableReader.getColumnNames(fullTableName, columnNames);
                String corrName=tbl.getCorrName();
                String tableName=tbl.getTableName();
                String schemaName=tbl.getSchemaName();
                for (int j=0; j<columnNames.size(); j++) {
                    String columnName = (String) columnNames.get(j);
                    columns.add(new ColumnNode(tableName, columnName, corrName, schemaName));
                }
            }
            _select.setColumnList(columns);
        }
    }

    public void addColumn(String tableSpec, String columnName) {
        // Get the corresponding Table object from the FROM, to resolve issues
        // of corrName/tableName
        Table table = _from.findTable(tableSpec);
        ColumnNode col = new ColumnNode(table, columnName);
        
        // Note that they will share the column.  Copy if this causes problem
        _select.addColumn(col);
        if (_groupBy != null)
            _groupBy.addColumn(col);
    }

    public void removeColumn(String tableSpec, String columnName) {
        _select.removeColumn(tableSpec, columnName);
        if (_groupBy != null)
            _groupBy.removeColumn(tableSpec, columnName);
        // Remove the sort spec for this column if there was one
        removeSortSpecification(tableSpec, columnName);
    }
    
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _from.renameTableSpec(oldTableSpec, corrName);
        _select.renameTableSpec(oldTableSpec, corrName);
        if (_where!=null)
            _where.renameTableSpec(oldTableSpec, corrName);
        if (_groupBy!=null)
            _groupBy.renameTableSpec(oldTableSpec, corrName);
        if (_having!=null)
            _having.renameTableSpec(oldTableSpec, corrName);
        if (_orderBy!=null)
            _orderBy.renameTableSpec(oldTableSpec, corrName);
    }

    public void getReferencedColumns(Collection columns) {
        _from.getReferencedColumns(columns);
        _select.getReferencedColumns(columns);
        if (_where!=null)
            _where.getReferencedColumns(columns);
        if (_groupBy!=null)
            _groupBy.getReferencedColumns(columns);
        if (_having!=null)
            _having.getReferencedColumns(columns);
        if (_orderBy!=null)
            _orderBy.getReferencedColumns(columns);
    }
    //
    // private implementation
    //
    
    private void removeSortSpecification(String tableSpec) {
        if (_orderBy!=null)
            _orderBy.removeSortSpecification(tableSpec);
    }

    private void removeSortSpecification(String tableSpec, String columnName) {
        if (_orderBy!=null)
            _orderBy.removeSortSpecification(tableSpec, columnName);
    }

}


