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

import org.netbeans.modules.db.metadata.model.spi.ValueImplementation;

/**
 * Defines a value used when working with the database.  It can be
 * a column in a table, a column in a result set, or a parameter in a procedure
 * or a function.
 *
 * @author David Van Couvering
 */
public class Value extends MetadataElement {
    private final ValueImplementation impl;

    Value(ValueImplementation impl) {
        this.impl = impl;
    }

    @Override
    public MetadataElement getParent() {
        return impl.getParent();
    }

    @Override
    public String getName() {
        return impl.getName();
    }
    
    /**
     * Return the SQL type of this value
     *
     * @return the SQL type of this value
     */
    public SQLType getType() {
        return impl.getType();
    }

    /**
     * Return the precision for this value.  Precision is defined as the total
     * number of possible digits for this value
     *
     * @return the precision for this value or 0 if precision does nt apply to this type
     */
    public int getPrecision() {
        return impl.getPrecision();
    }

    /**
     * Return the length of this value for variable length values such as
     * characters.  Length has no meaning for fixed-length types like numerics.
     *
     * @return the length of this value or 0 if length does not apply to this type.
     */
    public int getLength() {
        return impl.getLength();
    }

    /**
     * Return the scale for this value.  This is the number of digits to the
     * right of the decimal point.
     *
     * @return the scale for this value or 0 if scale does not apply to this type
     */
    public short getScale() {
        return impl.getScale();
    }

    /**
     * Return the radix for this value, where applicable.  This is defined number of digits
     * used in expressing a number.  For example, binary numbers have a radix
     * of 2, hex numbers have a radix of 16 and decimal numbers have a radix
     * of 10.  Non-numeric values have a radix of 0.
     *
     * @return the radix for this value, or 0 if a radix does not apply to this type
     */
    public short getRadix() {
        return impl.getRadix();
    }

    /**
     * Return whether this value is nullable or not
     *
     * @return whether this value is nullable
     */
    public Nullable getNullable() {
        return impl.getNullable();
    }

    /**
     * Return database specific name of data type
     *
     * @return
     */
    public String getTypeName() {
        return impl.getTypeName();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
