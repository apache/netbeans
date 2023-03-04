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
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Table;

/**
 *
 * @author David Van Couvering
 */
public abstract class ForeignKeyImplementation {
    private ForeignKey key;

    public final ForeignKey getForeignKey() {
        if (key == null) {
            key = MetadataAccessor.getDefault().createForeignKey(this);
        }
        return key;

    }

    /**
     * This is used internally only, to help resolve foreign key handles
     * in case the real foreign key name is null
     */
    public abstract String getInternalName();

    public abstract Collection<ForeignKeyColumn> getColumns();

    public abstract ForeignKeyColumn getColumn(String name);

    public abstract String getName();

    public abstract Table getParent();
}
