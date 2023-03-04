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

import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.spi.ParameterImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCParameter extends ParameterImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCParameter.class.getName());
    private final MetadataElement parent;
    private final Direction direction;
    private final int ordinalPosition;
    private final JDBCValue value;

    public JDBCParameter(MetadataElement parent, JDBCValue value, Direction direction, int ordinalPosition) {
        this.parent = parent;
        this.direction = direction;
        this.value = value;
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public String toString() {
        return "JDBCParameter[" + value + ", direction=" + getDirection() + ", position=" + getOrdinalPosition() + "]"; // NOI18N
    }

    @Override
    public MetadataElement getParent() {
        return parent;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String getName() {
        return value.getName();
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
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

}
