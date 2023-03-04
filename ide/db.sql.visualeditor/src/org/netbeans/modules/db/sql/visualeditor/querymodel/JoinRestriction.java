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

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents the restriction in a JOIN clause
 * Must be of form a.x = b.y
 */
public class JoinRestriction {

    // Fields

    private ColumnNode  _col1;

    private String      _op;

    private ColumnNode  _col2;


    // Constructors

    public JoinRestriction (ColumnNode col1, String op, ColumnNode col2) {
        _col1 = col1;
        _op = op;
        _col2 = col2;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText(SQLIdentifiers.Quoter quoter) {
        return " " + _col1.genText(quoter) + " " + _op + " " + _col2.genText(quoter);    // NOI18N
    }

    // Methods

    // Accessors/Mutators

    public Column getCol1 () {
        return _col1;
    }

    public Column getCol2 () {
        return _col2;
    }
}


