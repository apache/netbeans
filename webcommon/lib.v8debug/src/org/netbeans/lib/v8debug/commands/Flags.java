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

import java.util.Collections;
import java.util.Map;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class Flags {
    
    public static final String FLAG_BREAK_POINTS_ACTIVE = "breakPointsActive";
    public static final String FLAG_BREAK_ON_CAUGHT_EXCEPTION = "breakOnCaughtException";
    public static final String FLAG_BREAK_ON_UNCAUGHT_EXCEPTION = "breakOnUncaughtException";
    
    private Flags() {}
    
    public static V8Request createRequest(long sequence) {
        return new V8Request(sequence, V8Command.Flags, new Arguments());
    }
    
    public static V8Request createRequest(long sequence, String name, boolean value) {
        return new V8Request(sequence, V8Command.Flags, new Arguments(name, value));
    }
    
    public static V8Request createRequest(long sequence, Map<String, Boolean> flags) {
        return new V8Request(sequence, V8Command.Flags, new Arguments(flags));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final Map<String, Boolean> flags;
        
        public Arguments() {
            this(null);
        }
        
        public Arguments(String name, boolean value) {
            this(Collections.singletonMap(name, value));
        }
        
        public Arguments(Map<String, Boolean> flags) {
            this.flags = flags;
        }

        public Map<String, Boolean> getFlags() {
            return flags;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final Map<String, Boolean> flags;
        
        public ResponseBody() {
            this(Collections.<String, Boolean>emptyMap());
        }
        
        public ResponseBody(Map<String, Boolean> flags) {
            this.flags = flags;
        }

        public Map<String, Boolean> getFlags() {
            return flags;
        }
    }
    
}
