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

/**
 * A tuple is something that is represented by a set of rows, with each
 * row being represented by one or more columns
 *
 * @author David
 */
public abstract class Tuple extends MetadataElement {
    /**
     * Returns the columns in this tuple.
     *
     * @return the columns.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public abstract Collection<Column> getColumns();

    /**
     * Returns the column with the given name.
     *
     * @param name a column name.
     * @return a column named {@code name} or {@code null} if there is no such column.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public abstract Column getColumn(String name);

    /**
     * Returns the schema containing this tuple.
     *
     * @return the parent schema.
     * @since 0.8
     */
    public abstract Schema getParent();
}
