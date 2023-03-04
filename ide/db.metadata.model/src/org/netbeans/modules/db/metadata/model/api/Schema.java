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
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;

/**
 * Encapsulates a database schema.
 *
 * @author Andrei Badea
 */
public class Schema extends MetadataElement {

    final SchemaImplementation impl;

    Schema(SchemaImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the catalog containing this schema.
     *
     * @return the parent catalog.
     */
    public Catalog getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of this schema or {@code null} if the name is not known.
     *
     * @return the name or {@code null}.
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Returns {@code true} if this schema is the default one in the parent catalog.
     *
     * @return {@code true} if this is the default schema, {@false} otherwise.
     */
    public boolean isDefault() {
        return impl.isDefault();
    }

    /**
     * Returns {@code true} if this schema is synthetic.
     *
     * @return {@code true} if this is a synthetic schema, {@false} otherwise.
     */
    public boolean isSynthetic() {
        return impl.isSynthetic();
    }

    /**
     * Returns the tables in this schema.
     *
     * @return the tables.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Table> getTables() {
        return impl.getTables();
    }

    /**
     * Returns the table with the given name.
     *
     * @param name a table name.
     * @return a table named {@code name} or {@code null} if there is no such table.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Table getTable(String name) {
        return impl.getTable(name);
    }

    /**
     * Returns the views in this schema.
     *
     * @return the views.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<View> getViews() {
        return impl.getViews();
    }

    /**
     * Returns the view with the given name.
     *
     * @param name a view name.
     * @return a view named {@code view} or {@code null} if there is no such view.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public View getView(String name) {
        return impl.getView(name);
    }

    /**
     * Get the list of procedures for this schema
     *
     * @return the procedures
     * @throws MetadataException if an error occurs while retrieving the metadata
     */
    public Collection<Procedure> getProcedures() {
        return impl.getProcedures();
    }

    /**
     * Return a procedure with the given name
     *
     * @param name a procedure name
     * @return a procedure named {@code name} or {@code null} if there is no such procedure.
     * @throws MetadataException if an error occurs while retrieving the metadata
     */
    public Procedure getProcedure(String name) {
        return impl.getProcedure(name);
    }

    /**
     * Get the list of functions for this schema
     *
     * @return the functions
     * @throws MetadataException if an error occurs while retrieving the
     * metadata
     * @since db.metadata.model/1.0
     */
    public Collection<Function> getFunctions() {
        return impl.getFunctions();
    }

    /**
     * Return a function with the given name
     *
     * @param name a function name
     * @return a function named {@code name} or {@code null} if there is no such
     * function.
     * @throws MetadataException if an error occurs while retrieving the
     * metadata
     * @since db.metadata.model/1.0
     */
    public Function getFunction(String name) {
        return impl.getFunction(name);
    }

    /**
     * Refresh the metadata for this schema
     */
    public void refresh() {
        impl.refresh();
    }

    @Override
    public String toString() {
        return "Schema[name='" + impl.getName() + "',default=" + isDefault() + ",synthetic=" + isSynthetic() + "]"; // NOI18N
    }
}
