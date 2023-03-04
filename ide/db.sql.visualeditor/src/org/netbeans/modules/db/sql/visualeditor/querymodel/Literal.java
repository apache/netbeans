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

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL literal valus
 */
public class Literal implements Value {

    // Fields

    private Object _value;


    // Constructors

    public Literal() {
    }

    public Literal(Object value) {
        _value = value;
    }


    // Methods

    public String genText(SQLIdentifiers.Quoter quoter) {
        return _value.toString();
    }

    public String toString() {
        return _value.toString();
    }


    // Accessors/Mutators

    public Object getValue(SQLIdentifiers.Quoter quoter) {
        return _value;
    }

    //REVIEW: this should probably go away, and change Literal to not be a QueryItem?
    public void getReferencedColumns(Collection columns) {}
    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    public boolean isParameterized() {
        // Original version
        // return _value.equals("?");

        // Expand prev version, to try to catch literals like "id = ?" or "id IN (?,?,?).
        // return (_value.toString().indexOf("?") != -1 );

        // Prev version was also catching "id = '?'", which is a string, not a parameter.  Exclude it.
	// return (_value.toString().matches(".*[^']?[^']"));

        // Prev regexp had problems because ? was not escaped
        // Return true if (a) string contains ? (b) string does not contain '?'
        // This still gets other cases wrong, like ? in the middle of a string.
        return (((_value.toString().indexOf("?")) != -1) &&
                ((_value.toString().indexOf("'?'")) == -1));
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {}

}


