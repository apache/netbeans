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

package org.netbeans.modules.db.metadata.model.spi;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Function;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.View;

/**
 *
 * @author Andrei Badea
 */
public abstract class SchemaImplementation {

    private Schema schema;

    public final Schema getSchema() {
        if (schema == null) {
            schema = MetadataAccessor.getDefault().createSchema(this);
        }
        return schema;
    }

    public abstract Catalog getParent();

    public abstract String getName();

    public abstract View getView(String name);

    public abstract Collection<View> getViews();

    public abstract Procedure getProcedure(String name);

    public abstract Collection<Procedure> getProcedures();

    /**
     * @since db.metadata.model/1.0
     */
    public Function getFunction(String name) {
        return null;
    }

    /**
     * @since db.metadata.model/1.0
     */
    public Collection<Function> getFunctions() {
        return Collections.emptyList();
    }

    public abstract boolean isDefault();

    public abstract boolean isSynthetic();

    public abstract Collection<Table> getTables();

    public abstract Table getTable(String name);

    public abstract void refresh();
}
