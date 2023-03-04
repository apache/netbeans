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

package org.netbeans.modules.db.metadata.model.jdbc;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.PrimaryKeyImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCPrimaryKey extends PrimaryKeyImplementation {

    private final String name;
    private final Collection<Column> columns;
    private final Table parent;
    
    public JDBCPrimaryKey(Table parent, String name, Collection<Column> columns) {
        this.parent = parent;
        this.name = name;
        this.columns = Collections.unmodifiableCollection(columns);
    }
    
    @Override
    public Collection<Column> getColumns() {
        return columns;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Table getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "JDBCPrimaryKey[name='" + getName() + "']";
    }

}
