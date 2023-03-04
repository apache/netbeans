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
 * This class represents LongType. This is one of those atomic types that can
 * be used to type an Attribute or leaf Elements in AXI Model
 *
 * MaxInclusive is  9223372036854775807
 * MinInclusive is -9223372036854775808
 *
 * @author Ayub Khan
 */
public class LongType extends IntegerType {
    
    /**
     * Creates a new instance of LongType
     */
    public LongType() {
        super(Datatype.Kind.LONG);
    }
    
    /**
     * Creates a new instance of derived type of LongType
     */
    public LongType(Datatype.Kind kind) {
        super(kind);
    }
}
