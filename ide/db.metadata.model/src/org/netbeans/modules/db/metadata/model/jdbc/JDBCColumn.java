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

package org.netbeans.modules.db.metadata.model.jdbc;

import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.spi.ColumnImplementation;

/**
 *
 * @author Andrei Badea, David Van Couvering
 */
public class JDBCColumn extends ColumnImplementation {

    private final Tuple parent;
    private final JDBCValue value;
    private final int position;

    public JDBCColumn(Tuple parent, int position, JDBCValue value) {
        this.parent = parent;
        this.value = value;
        this.position = position;
    }

    public final Tuple getParent() {
        return parent;
    }

    public final String getName() {
        return value.getName();
    }

    @Override
    public String toString() {
        return "JDBCColumn[" + value + ", ordinal_position=" + position + "]"; // NOI18N
    }

    @Override
    public int getPrecision() {
        return value.getPrecision();
    }

    @Override
    public short getRadix() {
        return value.getRadix();
    }

    @Override
    public short getScale() {
        return value.getScale();
    }

    @Override
    public SQLType getType() {
        return value.getType();
    }

    @Override
    public String getTypeName() {
        return value.getTypeName();
    }

    @Override
    public int getLength() {
        return value.getLength();
    }

    @Override
    public Nullable getNullable() {
        return value.getNullable();
    }

    @Override
    public int getPosition() {
        return position;
    }
}
