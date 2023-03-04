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

import org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation;

/**
 * This class represents a column in an index.  It provides more information
 * about the column, such as whether it's used in the index ascending or
 * descending
 *
 * @author David Van Couvering
 */
public class IndexColumn extends MetadataElement {
    private IndexColumnImplementation impl;

    IndexColumn(IndexColumnImplementation impl) {
        this.impl = impl;
    }

    /**
     * Return the ordering for this column in the index
     *
     * @return the ordering for this column
     */
    public Ordering getOrdering() {
        return impl.getOrdering();
    }

    /**
     * Get the ordinal position for the column in this index
     *
     * @return the ordinal position, starting at 1
     */
    public int getPosition() {
        return impl.getPosition();
    }

    @Override
    public Index getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of the column
     *
     * @return the column name
     */
    @Override
    public String getName() {
        return impl.getName();
    }

    /**
     * Get the underlying column for this index column
     *
     * @return the column for this index column.
     */
    public Column getColumn() {
        return impl.getColumn();
    }

    @Override
    public String toString() {
        return impl.toString();
    }


}
