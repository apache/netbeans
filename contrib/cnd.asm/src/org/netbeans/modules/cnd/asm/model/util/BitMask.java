/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.asm.model.util;

// "Type Safe" bit mask
abstract public class BitMask<T extends BitMask> {
                    
    protected int mask;
    
    protected BitMask(int mask) {
       this.mask = mask;
    }

    public T apply(T u) {                        
        return create(mask | u.mask);
    }

    public boolean contains(T u) {
       return ((~mask) & u.mask) == 0;
    }
    
    public boolean intersects(T u) {
       return (mask & u.mask) != 0;
    }
    
    protected abstract T create(int param);
}
