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

package org.netbeans.modules.xml.axi.datatype;

/**
 * This class represents PositiveIntegerType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 * FractionDigits is 0
 *
 * @author Ayub Khan
 */
public class PositiveIntegerType extends NonNegativeIntegerType {
    
    /**
     * Creates a new instance of PositiveIntegerType
     */
    public PositiveIntegerType() {
        super(Datatype.Kind.POSITIVE_INTEGER);
    }
    
    /**
     * Creates a new instance of derived type of PositiveIntegerType
     */
    public PositiveIntegerType(Datatype.Kind kind) {
        super(kind);
    }
}
