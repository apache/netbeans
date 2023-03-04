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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.Arrays;

import org.netbeans.modules.dbschema.*;

public class ForeignKeyElementImpl  extends KeyElementImpl implements ForeignKeyElement.Impl {

    private TableElementImpl tei;

    public ForeignKeyElementImpl() {
		this(null, null);
    }

    public ForeignKeyElementImpl(TableElementImpl tei, String name) {
		super(name);

        this.tei = tei;
    }

    protected DBElementsCollection initializeCollection() {
        return new DBElementsCollection(this, new ColumnPairElement[0]);
    }
  
    public ColumnPairElement[] getColumnPairs() {
        DBElement[] dbe = getColumnCollection().getElements();
        return Arrays.asList(dbe).toArray(new ColumnPairElement[dbe.length]);
    }
    
    public ColumnPairElement getColumnPair(DBIdentifier name) {
		return (ColumnPairElement) getColumnCollection().find(name);
    }
    
    public void changeColumnPairs(ColumnPairElement[] pairs,int action) throws DBException {
        getColumnCollection().changeElements(pairs, action);
    }
    
    public ColumnElement[] getColumns() {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        ColumnElement[] ce = new ColumnElement[cpe.length];
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            ce[i] = ((ForeignKeyElement) element).getDeclaringTable().getColumn(DBIdentifier.create(localColumn));
        }
        
        return ce;
    }
    
    public ColumnElement getColumn(DBIdentifier name) {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            if (name.getName().equals(DBIdentifier.create(localColumn).getName())) //need to check
                return ((ForeignKeyElement) element).getDeclaringTable().getColumn(name);
        }
        
        return null;
    }
    
}
