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

import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Script;

/**
 *
 * @author Martin Entlicher
 */
public final class Scripts {
    
    private Scripts() {}
    
    public static V8Request createRequest(long sequence) {
        return new V8Request(sequence, V8Command.Scripts, null);
    }
    
    public static V8Request createRequest(long sequence, V8Script.Types types, Boolean includeSource) {
        return new V8Request(sequence, V8Command.Scripts, new Arguments(types, null, includeSource, null));
    }
    
    public static V8Request createRequest(long sequence, V8Script.Types types,
                                          long[] ids, Boolean includeSource,
                                          String nameFilter) {
        return new V8Request(sequence, V8Command.Scripts, new Arguments(types, ids, includeSource, nameFilter));
    }
    
    public static V8Request createRequest(long sequence, V8Script.Types types,
                                          long[] ids, Boolean includeSource,
                                          long idFilter) {
        return new V8Request(sequence, V8Command.Scripts, new Arguments(types, ids, includeSource, idFilter));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final V8Script.Types types;
        private final long[] ids;
        private final PropertyBoolean includeSource;
        private final String nameFilter;
        private final PropertyLong idFilter;
        
        public Arguments(V8Script.Types types, long[] ids, Boolean includeSource,
                         String nameFilter) {
            this.types = types;
            this.ids = ids;
            this.includeSource = new PropertyBoolean(includeSource);
            this.nameFilter = nameFilter;
            this.idFilter = new PropertyLong(null);
        }
        
        public Arguments(V8Script.Types types, long[] ids, Boolean includeSource,
                         long idFilter) {
            this.types = types;
            this.ids = ids;
            this.includeSource = new PropertyBoolean(includeSource);
            this.nameFilter = null;
            this.idFilter = new PropertyLong(idFilter);
        }

        public V8Script.Types getTypes() {
            return types;
        }

        public long[] getIds() {
            return ids;
        }

        public PropertyBoolean isIncludeSource() {
            return includeSource;
        }

        public String getNameFilter() {
            return nameFilter;
        }

        public PropertyLong getIdFilter() {
            return idFilter;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Script[] scripts;
        
        public ResponseBody(V8Script[] scripts) {
            this.scripts = scripts;
        }

        public V8Script[] getScripts() {
            return scripts;
        }
    }
    
}
