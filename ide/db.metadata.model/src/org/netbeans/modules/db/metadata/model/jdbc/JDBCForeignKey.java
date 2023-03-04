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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCForeignKey extends ForeignKeyImplementation {
    private final Table parent;
    private final String name;
    private final Map<String,ForeignKeyColumn> columns = new LinkedHashMap<String,ForeignKeyColumn>();
    private final String internalName;
    private static AtomicLong fkeyCounter = new AtomicLong(0);

    public JDBCForeignKey(Table parent, String name) {
        this.parent = parent;
        this.name = name;
        internalName = parent.getName() + "_FKEY_" + fkeyCounter.incrementAndGet();
    }

    public void addColumn(ForeignKeyColumn col) {
        columns.put(col.getName(), col);
    }

    public final Table getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "JDBCForeignKey[name='" + name + "']"; // NOI18N
    }

    @Override
    public Collection<ForeignKeyColumn> getColumns() {
        return columns.values();
    }

    @Override
    public ForeignKeyColumn getColumn(String name) {
        return columns.get(name);
    }

    @Override
    public String getInternalName() {
        String result = getName();
        return (result != null) ? result : internalName;
    }
}
