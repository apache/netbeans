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

package org.netbeans.modules.db.metadata.model;

import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Function;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Value;
import org.netbeans.modules.db.metadata.model.api.View;
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
 *
 * @author Andrei Badea
 */
public abstract class MetadataAccessor {

    private static volatile MetadataAccessor accessor;

    public static void setDefault(MetadataAccessor accessor) {
        if (MetadataAccessor.accessor != null) {
            throw new IllegalStateException();
        }
        MetadataAccessor.accessor = accessor;
    }

    public static MetadataAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        Class<Metadata> c = Metadata.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return accessor;
    }

    public abstract ForeignKeyColumn createForeignKeyColumn(ForeignKeyColumnImplementation impl);

    public abstract ForeignKey createForeignKey(ForeignKeyImplementation impl);

    public abstract Index createIndex(IndexImplementation impl);

    public abstract IndexColumn createIndexColumn(IndexColumnImplementation impl);

    public abstract MetadataModel createMetadataModel(MetadataModelImplementation impl);

    public abstract Metadata createMetadata(MetadataImplementation impl);

    public abstract Catalog createCatalog(CatalogImplementation impl);

    public abstract Parameter createParameter(ParameterImplementation impl);

    public abstract PrimaryKey createPrimaryKey(PrimaryKeyImplementation impl);

    public abstract Procedure createProcedure(ProcedureImplementation impl);

    public abstract Function createFunction(FunctionImplementation impl);

    public abstract Schema createSchema(SchemaImplementation impl);

    public abstract Table createTable(TableImplementation impl);

    public abstract Column createColumn(ColumnImplementation impl);

    public abstract Value createValue(ValueImplementation impl);

    public abstract View createView(ViewImplementation impl);

    public abstract CatalogImplementation getCatalogImpl(Catalog catalog);
}
