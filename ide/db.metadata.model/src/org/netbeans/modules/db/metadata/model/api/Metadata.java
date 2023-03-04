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

import java.sql.DatabaseMetaData;
import java.util.Collection;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.MetadataModelImplementation;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.ColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyImplementation;
import org.netbeans.modules.db.metadata.model.spi.FunctionImplementation;
import org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.IndexImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;
import org.netbeans.modules.db.metadata.model.spi.ParameterImplementation;
import org.netbeans.modules.db.metadata.model.spi.PrimaryKeyImplementation;
import org.netbeans.modules.db.metadata.model.spi.ProcedureImplementation;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;
import org.netbeans.modules.db.metadata.model.spi.ValueImplementation;
import org.netbeans.modules.db.metadata.model.spi.ViewImplementation;

/**
 * Encapsulates information about the metadata in a database. The meaning
 * of "database" in this documentation is similar to that in {@link DatabaseMetaData}.
 *
 * @author Andrei Badea
 */
public class Metadata {

    private final MetadataImplementation impl;

    static {
        MetadataAccessor.setDefault(new MetadataAccessorImpl());
    }

    Metadata(MetadataImplementation impl) {
        this.impl = impl;
    }

    // XXX can this return null?
    /**
     * Returns the default catalog in this metadata instance.
     *
     * @return the default catalog.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Catalog getDefaultCatalog() {
        return impl.getDefaultCatalog();
    }

    /**
     * Returns the catalogs in this metadata instance.
     *
     * @return the catalogs.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Catalog> getCatalogs() {
        return impl.getCatalogs();
    }

    /**
     * Returns the catalog with the given name.
     *
     * @param name a catalog name.
     * @return a catalog named {@code name} or {@code null} if there is no such catalog.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Catalog getCatalog(String name) {
        return impl.getCatalog(name);
    }

    /**
     * @return the default schema or {@code null}.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Schema getDefaultSchema() {
        return impl.getDefaultSchema();
    }

    /**
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public void refresh() {
        impl.refresh();
    }

    private static final class MetadataAccessorImpl extends MetadataAccessor {

        @Override
        public MetadataModel createMetadataModel(MetadataModelImplementation impl) {
            return new MetadataModel(impl);
        }

        @Override
        public Metadata createMetadata(MetadataImplementation impl) {
            return new Metadata(impl);
        }

        @Override
        public Catalog createCatalog(CatalogImplementation impl) {
            return new Catalog(impl);
        }

        @Override
        public Schema createSchema(SchemaImplementation impl) {
            return new Schema(impl);
        }

        @Override
        public Table createTable(TableImplementation impl) {
            return new Table(impl);
        }


        @Override
        public View createView(ViewImplementation impl) {
            return new View(impl);
        }

        @Override
        public Column createColumn(ColumnImplementation impl) {
            return new Column(impl);
        }

        @Override
        public Procedure createProcedure(ProcedureImplementation impl) {
            return new Procedure(impl);
        }

        @Override
        public Function createFunction(FunctionImplementation impl) {
            return new Function(impl);
        }

        @Override
        public Parameter createParameter(ParameterImplementation impl) {
            return new Parameter(impl);
        }

        @Override
        public Value createValue(ValueImplementation impl) {
            return new Value(impl);
        }

        @Override
        public CatalogImplementation getCatalogImpl(Catalog catalog) {
            return catalog.impl;
        }

        @Override
        public PrimaryKey createPrimaryKey(PrimaryKeyImplementation impl) {
            return new PrimaryKey(impl);
        }

        @Override
        public Index createIndex(IndexImplementation impl) {
            return new Index(impl);
        }

        @Override
        public IndexColumn createIndexColumn(IndexColumnImplementation impl) {
            return new IndexColumn(impl);
        }

        @Override
        public ForeignKeyColumn createForeignKeyColumn(ForeignKeyColumnImplementation impl) {
            return new ForeignKeyColumn(impl);
        }

        @Override
        public ForeignKey createForeignKey(ForeignKeyImplementation impl) {
            return new ForeignKey(impl);
        }
    }

}
