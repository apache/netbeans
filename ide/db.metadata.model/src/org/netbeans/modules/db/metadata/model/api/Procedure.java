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
import org.netbeans.modules.db.metadata.model.spi.ProcedureImplementation;

/**
 *
 * @author Andrei Badea
 */
public class Procedure extends Tuple {

    final ProcedureImplementation impl;

    Procedure(ProcedureImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the schema containing this procedure.
     *
     * @return the parent schema.
     */
    public Schema getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of this procedure; never {@code null}.
     *
     * @return the name.
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Returns the return value of this procedure
     * 
     * @return the return value for this procedure
     */
    public Value getReturnValue() {
        return impl.getReturnValue();
    }

    /**
     * Returns the columns in the result set for this procedure.
     *
     * @return the columns.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Column> getColumns() {
        return impl.getColumns();
    }

    /**
     * Returns the column with the given name.
     *
     * @param name a column name.
     * @return a column named {@code name} or {@code null} if there is no such column.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Column getColumn(String name) {
        return impl.getColumn(name);
    }

    /**
     * Returns the list of parameters for this procedure
     *
     * @return the list of parameters for this procedure
     * @throws MetadataException if an error occurs while retrieving the metadata
     */
    public Collection<Parameter> getParameters() {
        return impl.getParameters();
    }

    /**
     * Returns the parameter with the given name
     * @throws MetadataException if an error occurs while retrieving the metadata
     */
    public Parameter getParameter(String name) {
        return impl.getParameter(name);
    }

    /**
     * Refresh the table metadata from the database
     */
    public void refresh() {
        impl.refresh();
    }

    @Override
    public String toString() {
        return "Procedure[name='" + getName() + "']";                  // NOI18N
    }
}
