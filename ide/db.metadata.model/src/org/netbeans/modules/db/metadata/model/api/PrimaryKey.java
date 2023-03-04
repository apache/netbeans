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
import org.netbeans.modules.db.metadata.model.spi.PrimaryKeyImplementation;

/**
 *
 * @author David
 */
public class PrimaryKey extends MetadataElement {

    private final PrimaryKeyImplementation impl;

    PrimaryKey(PrimaryKeyImplementation impl) {
        this.impl = impl;
    }

    @Override
    public Table getParent() {
        return impl.getParent();
    }

    @Override
    /**
     * Get the name for this primary key.  May be null.
     *
     * @return the name for this primary key
     */
    public String getName() {
        return impl.getName();
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    /**
     * Get the list of columns for this primary key.  The collection is ordered
     * based on the sequencing of the primary key
     *
     * @return the list of columns for this primary key.  
     */
    public Collection<Column> getColumns() {
        return impl.getColumns();
    }

}
