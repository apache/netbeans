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
 * This class represents IntType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 * MaxInclusive is 2147483647
 * MinInclusive is -2147483648
 *
 * @author Ayub Khan
 */
public class IntType extends LongType {
    
    /**
     * Creates a new instance of IntType
     */
    public IntType() {
        super(Datatype.Kind.INT);
    }
    
    /**
     * Creates a new instance of derived type of IntType
     */
    public IntType(Datatype.Kind kind) {
        super(kind);
    }
}
