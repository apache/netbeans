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

import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Value;

/**
 *
 * @author David Van Couvering
 */
public abstract class ValueImplementation {
    private Value value;

    public final Value getValue() {
        if (value == null) {
            value = MetadataAccessor.getDefault().createValue(this);
        }
        return value;
    }

    public abstract MetadataElement getParent();

    public abstract int getLength();

    public abstract String getName();

    public abstract Nullable getNullable();

    public abstract int getPrecision();

    public abstract short getRadix();

    public abstract short getScale();

    public abstract SQLType getType();

    /**
     * This should be overriden by the implementation - this is a fallback!
     *
     * @return Database specific type name
     */
    public String getTypeName() {
        return getType().toString();
    }
}
