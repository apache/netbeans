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

import org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation;

/**
 * This class defines a column that is a foreign key, referring to a
 * column in the primary key of another table.
 * 
 * @author David Van Couvering
 */
public class ForeignKeyColumn extends MetadataElement {
    ForeignKeyColumnImplementation impl;

    ForeignKeyColumn(ForeignKeyColumnImplementation impl) {
        this.impl = impl;
    }

    @Override
    public ForeignKey getParent() {
        return impl.getParent();
    }

    @Override
    public String getName() {
        return impl.getName();
    }

    /**
     * Get the column in the source table which is referring to the column
     * in the target table.
     *
     * @return the referring column definition
     */
    public Column getReferringColumn() {
        return impl.getReferringColumn();
    }

    /**
     * Get the primary key column in the target table which is being referred to
     *
     * @return the referred column
     */
    public Column getReferredColumn() {
        return impl.getReferredColumn();
    }

    /**
     * Get the position of this column in the foreign key
     *
     * @return the position of the column in the foreign key
     */
    public int getPosition() {
        return impl.getPosition();
    }
}
