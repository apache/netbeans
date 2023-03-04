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
package org.netbeans.lib.v8debug.vars;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Number extends V8Value {
    
    private final Kind type;
    private final long lvalue;
    private final double dvalue;
    
    public static enum Kind {
        Long,
        Double
    }
    
    public V8Number(long handle, long value, String text) {
        super(handle, V8Value.Type.Number, text);
        this.type = Kind.Long;
        this.lvalue = value;
        this.dvalue = value;
    }

    public V8Number(long handle, double value, String text) {
        super(handle, V8Value.Type.Number, text);
        this.type = Kind.Double;
        this.lvalue = (long) value;
        this.dvalue = value;
    }
    
    public Kind getKind() {
        return type;
    }

    public long getLongValue() {
        return lvalue;
    }
    
    public double getDoubleValue() {
        return dvalue;
    }
}
