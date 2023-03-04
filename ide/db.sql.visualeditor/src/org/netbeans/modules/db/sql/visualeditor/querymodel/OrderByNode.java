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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL ORDER BY clause
 */
public class OrderByNode implements OrderBy {

    // Fields

    // A vector of generalized column objects (JoinTables)

    List<SortSpecification> _sortSpecificationList;


    // Constructors

    public OrderByNode() {
    }

    public OrderByNode(List sortSpecificationList) {
        _sortSpecificationList = sortSpecificationList;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = "";    // NOI18N
        if (_sortSpecificationList != null && _sortSpecificationList.size() > 0) {

            res = " ORDER BY " + _sortSpecificationList.get(0).genText(quoter);  // NOI18N

            for (int i=1; i<_sortSpecificationList.size(); i++) {
                res += ", " + "                    " +    // NOI18N
                  _sortSpecificationList.get(i).genText(quoter);
            }
        }

        return res;
    }



    // Methods

    // Accessors/Mutators

    void renameTableSpec(String oldTableSpec, String corrName) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++)
                _sortSpecificationList.get(i).renameTableSpec(oldTableSpec, corrName);
        }
    }

    public void removeSortSpecification(String tableSpec) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++) {
                ColumnNode col = (ColumnNode)_sortSpecificationList.get(i).getColumn();
                if (col.getTableSpec().equals(tableSpec))
                {
                    _sortSpecificationList.remove(i);
                    // item from arraylist is removed, reset index value
                    // as remove shifts any subsequent elements to the left
                    // (subtracts one from their indices).
                    i=i-1;
                }
            }
        }
    }

    public void removeSortSpecification(String tableSpec, String columnName) {
        if (_sortSpecificationList != null) {
            for (int i=0; i<_sortSpecificationList.size(); i++) {
                ColumnNode col = (ColumnNode)_sortSpecificationList.get(i).getColumn();
                if (col.matches(tableSpec, columnName))
                {
                    _sortSpecificationList.remove(i);
                    // item from arraylist is removed, reset index value
                    // as remove shifts any subsequent elements to the left
                    // (subtracts one from their indices).
                    i=i-1;
                }
            }
        }
    }

    public void addSortSpecification(String tableSpec, String columnName, String direction, int order) {
        SortSpecification sortSpec = new SortSpecification(new ColumnNode(tableSpec, columnName), direction);
        // Insert the new one in an appropriate place
        if (_sortSpecificationList == null)
            _sortSpecificationList = new ArrayList<>();
        _sortSpecificationList.add(order-1, sortSpec);
    }

    public int getSortSpecificationCount() {
        return (_sortSpecificationList != null) ? _sortSpecificationList.size() : 0;
    }

    public SortSpecification getSortSpecification(int i) {
        return (_sortSpecificationList != null) ? _sortSpecificationList.get(i) : null;
    }

    public void  getReferencedColumns (Collection columns) {
        if (_sortSpecificationList != null) {
            for (int i = 0; i < _sortSpecificationList.size(); i++)
                _sortSpecificationList.get(i).getReferencedColumns(columns);
        }
    }

}
