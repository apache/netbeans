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

package org.netbeans.modules.javascript.v8debug;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * Holder of values referenced by a response.
 * 
 * @author Martin Entlicher
 */
public final class ReferencedValues {
    
    private final Map<Long, V8Value> valuesByReferences;
    
    public ReferencedValues(V8Response response) {
        this(response.getReferencedValues());
    }
    
    public ReferencedValues(ReferencedValue[] referencedValues) {
        valuesByReferences = new HashMap<>();
        for (int i = 0; i < referencedValues.length; i++) {
            valuesByReferences.put(referencedValues[i].getReference(), referencedValues[i].getValue());
        }
    }
    
    public V8Value getReferencedValue(long reference) {
        return valuesByReferences.get(reference);
    }
}
