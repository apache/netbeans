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
package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class GC {
    
    private GC() {}
    
    public static V8Request createRequest(long sequence) {
        return new V8Request(sequence, V8Command.Gc, new Arguments("all"));
    }
    
    public static V8Request createRequest(long sequence, String type) {
        return new V8Request(sequence, V8Command.Gc, new Arguments(type));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final String type;
        
        public Arguments(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final long before;
        private final long after;
        
        public ResponseBody(long before, long after) {
            this.before = before;
            this.after = after;
        }

        public long getBefore() {
            return before;
        }

        public long getAfter() {
            return after;
        }
    }
}
