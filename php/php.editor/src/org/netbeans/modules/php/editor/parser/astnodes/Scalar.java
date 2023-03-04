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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents a scalar
 * <pre>e.g.<pre> 'string',
 * 1,
 * 1.3,
 * __CLASS__
 */
public class Scalar extends Expression {

    public enum Type {
        INT, // 'int'
        REAL, // 'real'
        STRING, // 'string'
        UNKNOWN, // unknown scalar in quote expression
        SYSTEM // system scalars (__CLASS__ / ...)

    }
    // 'int'
    //public static final int TYPE_INT = 0;
    // 'real'
    //public static final int TYPE_REAL = 1;
    // 'string'
    //public static final int TYPE_STRING = 2;
    // unknown scalar in quote expression
    //public static final int TYPE_UNKNOWN = 3;
    // system scalars (__CLASS__ / ...)
    //public static final int TYPE_SYSTEM = 4;

    private String stringValue;
    private Type scalarType;

    public Scalar(int start, int end, String value, Scalar.Type type) {
        super(start, end);

        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.scalarType = type;
        this.stringValue = value;
    }

    /**
     * the scalar type
     * @return scalar type
     */
    public Scalar.Type getScalarType() {
        return scalarType;
    }

    /**
     * the scalar value
     * @return scalar value
     */
    public String getStringValue() {
        return this.stringValue;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getScalarType() + " " + getStringValue(); //NOI18N
    }

}
