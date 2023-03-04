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
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.IndexImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCIndex extends IndexImplementation {

    private final Table parent;
    private final String name;
    private final Map<String,IndexColumn> columns = new LinkedHashMap<String,IndexColumn>();
    private final IndexType indexType;
    private final boolean isUnique;

    public JDBCIndex(Table parent, String name, IndexType indexType, boolean isUnique) {
        this.parent = parent;
        this.name = name;
        this.indexType = indexType;
        this.isUnique = isUnique;
    }

    public void addColumn(IndexColumn col) {
        columns.put(col.getName(), col);
    }

    @Override
    public IndexColumn getColumn(String name) {
        return columns.get(name);
    }

    public final Table getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "JDBCIndex[name='" + name + "', type=" +indexType + ", unique=" + isUnique +"]"; // NOI18N
    }

    @Override
    public Collection<IndexColumn> getColumns() {
        return columns.values();
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public boolean isUnique() {
        return isUnique;
    }
}
