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
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;

/**
 *
 * @author Andrei Badea
 */
public class Catalog extends MetadataElement {

    final CatalogImplementation impl;

    Catalog(CatalogImplementation impl) {
        this.impl = impl;
    }

    public MetadataElement getParent() {
        return null;
    }

    /**
     * Returns the name of this catalog or {@code null} if the name is not known.
     *
     * @return the name or {@code null}.
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Returns {@code true} if this catalog is the default one in this metadata model.
     *
     * @return {@code true} if this is the default catalog, {@false} otherwise.
     */
    public boolean isDefault() {
        return impl.isDefault();
    }

    /**
     * Returns the synthetic schema in this catalog. The catalog has
     * a synthetic schema if the database doesn't support schemas. Synthetic
     * schemas have {@code null} names.
     *
     * <p>If this method returns {@code null}, then the database supports schemas,
     * and they can be retrieved by the {@link #getSchemas} method.</p>
     *
     * @return the synthetic schema or {@code null}.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Schema getSyntheticSchema() {
        return impl.getSyntheticSchema();
    }

    /**
     * Returns the schemas in this catalog.
     *
     * <p>If the database does not supports schemas, this method always returns
     * an empty collection. In that case, a synthetic schema available
     * through {@link #getSyntheticSchema} provides access to the tables and
     * other metadata elements in the catalog.</p>
     *
     * @return the schemas.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Schema> getSchemas() {
        return impl.getSchemas();
    }

    /**
     * Returns the schema with the given name.
     *
     * @param name a schema name.
     * @return a schema named {@code name} or {@code null} if there is no such schema.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Schema getSchema(String name) {
        return impl.getSchema(name);
    }

    /**
     * Refresh the entire catalog
     *
     */
    public void refresh() {
        impl.refresh();
    }

    @Override
    public String toString() {
        return "Catalog[name='" + impl.getName() + "',default=" + isDefault() + "]"; // NOI18N
    }
}
