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
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.Schema;

/**
 *
 * @author Andrei Badea
 */
public abstract class MetadataImplementation {

    private Metadata metadata;

    public final Metadata getMetadata() {
        if (metadata == null) {
            metadata = MetadataAccessor.getDefault().createMetadata(this);
        }
        return metadata;
    }

    public abstract Catalog getDefaultCatalog();

    public abstract Collection<Catalog> getCatalogs();

    public abstract Catalog getCatalog(String name);

    public abstract Schema getDefaultSchema();

    public abstract void refresh();
}
