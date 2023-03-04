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
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyImplementation;

/**
 * This class represents a foreign key in a table - a key that refers to the
 * primary key of another table.
 * 
 * @author David Van Couvering
 */
public class ForeignKey extends MetadataElement {
    private final ForeignKeyImplementation impl;

    ForeignKey(ForeignKeyImplementation impl) {
        this.impl = impl;
    }

    @Override
    public Table getParent() {
        return impl.getParent();
    }

    @Override
    /**
     * Return the name of the foreign key.  The name of a foreign key may be null
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Get the foreign key columns that comprise this foreign key
     *
     * @return the collection of foreign key columns for this foreign key
     */
    public Collection<ForeignKeyColumn> getColumns() {
        return impl.getColumns();
    }

    /**
     * Get a specific foreign key column by name
     *
     * @param name the name of the foreign key column we are interested in
     *
     * @return the foreign key column for this name
     */
    public ForeignKeyColumn getColumn(String name) {
        return impl.getColumn(name);
    }
    
    /**
     * Get the internal name of the foreign key.  Used to resolve a foreign key
     * when its real name is null
     *
     * @return
     */
    @Override
    String getInternalName() {
       return impl.getInternalName();
    }
}
