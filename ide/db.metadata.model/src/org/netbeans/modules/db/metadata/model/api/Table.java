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

package org.netbeans.modules.db.metadata.model.api;

import java.util.Collection;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;

/**
 *
 * @author Andrei Badea
 */
public class Table extends Tuple {

    final TableImplementation impl;

    Table(TableImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the schema containing this table.
     *
     * @return the parent schema.
     */
    public Schema getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of this table; never {@code null}.
     *
     * @return the name.
     */
    public String getName() {
        return impl.getName();
    }

    @Override
    public Collection<Column> getColumns() {
        return impl.getColumns();
    }

    @Override
    public Column getColumn(String name) {
        return impl.getColumn(name);
    }

    /**
     * Get the primary key for this table
     *
     * @return the primary key for this table
     */
    public PrimaryKey getPrimaryKey() {
        return impl.getPrimaryKey();
    }

    /**
     * Get the indexes for this table
     *
     * @return the indexes for this table, or an empty collection if none exist
     */
    public Collection<Index> getIndexes() {
        return impl.getIndexes();
    }

    /**
     * Get an index of a given name
     * @param name the name of the index
     * @return the index of the given name, or null if it doesn't exist
     */
    public Index getIndex(String name) {
        return impl.getIndex(name);
    }

    /**
     * Get the foreign keys for this table
     *
     * @return the foreign keys for the table, or an empty collection if none exist
     */
    public Collection<ForeignKey> getForeignKeys() {
        return impl.getForeignKeys();
    }

    /**
     * Refresh the table metadata from the database
     */
    public void refresh() {
        impl.refresh();
    }


    @Override
    public String toString() {
        return "Table[name='" + getName() + "']"; // NOI18N
    }

    public boolean isSystem() {
        return impl.isSystem();
    }

    /**
     * Used to find a foreign key if the actual name is null
     */
    ForeignKey getForeignKeyByInternalName(String internalName) {
        return impl.getForeignKeyByInternalName(internalName);
    }
}
