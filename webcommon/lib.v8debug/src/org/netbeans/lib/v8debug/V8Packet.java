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
package org.netbeans.lib.v8debug;

/**
 * An abstract V8 debugging protocol packet.
 * 
 * @author Martin Entlicher
 */
public abstract class V8Packet {
    
    private static final String SEQ = "seq";
    private static final String TYPE = "type";
    
    private final long sequence;
    private final V8Type type;
    
    protected V8Packet(long sequence, V8Type type) {
        this.sequence = sequence;
        this.type = type;
    }

    public long getSequence() {
        return sequence;
    }

    public V8Type getType() {
        return type;
    }
    
}
