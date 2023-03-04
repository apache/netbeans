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
import org.netbeans.modules.db.metadata.model.spi.IndexImplementation;

/**
 *
 * @author David Van Couvering
 */
public class Index extends MetadataElement {
    public enum IndexType { CLUSTERED, HASHED, OTHER };

    final IndexImplementation impl;

    Index(IndexImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the schema containing this table.
     *
     * @return the parent schema.
     */
    public Table getParent() {
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

    /**
     * Return the columns for this index
     *
     * @return the list of columns for this index
     */
    public Collection<IndexColumn> getColumns() {
        return impl.getColumns();
    }

    /**
     * Return a given index column
     * @param name the name of the column to retrieve
     * @return the column for the given name or null if it doesn't exist
     */
    public IndexColumn getColumn(String name) {
        return impl.getColumn(name);
    }

    /**
     * Return the type of index
     *
     * @return the index type
     */
    public IndexType getIndexType() {
        return impl.getIndexType();
    }

    /**
     * Return whether the index must have unique values
     *
     * @return true if unique, false otherwise
     */
    public boolean isUnique() {
        return impl.isUnique();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
