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

package org.netbeans.modules.cnd.asm.model.lang;

import org.netbeans.modules.cnd.asm.model.util.BitMask;

public interface OperandElement extends AsmElement {       
    Usage getUsage();
    
    // "type safe" bit mask for argument usage
    public final static class Usage extends BitMask<Usage> {
       public static final Usage OP_USE_NO_USE = new Usage(0);
       public static final Usage OP_USE_READ = new Usage(1);
       public static final Usage OP_USE_WRITE = new Usage(2);
       public static final Usage OP_USE_READ_WRITE = OP_USE_READ.apply(OP_USE_WRITE);
       
       private Usage(int mask) { 
           super(mask);
       }
       
       protected Usage create(int mask) {
           return new Usage(mask);
       }
    }        
}


