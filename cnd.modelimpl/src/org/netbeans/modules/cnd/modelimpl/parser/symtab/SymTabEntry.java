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
package org.netbeans.modules.cnd.modelimpl.parser.symtab;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public final class SymTabEntry {
    private final Map<SymTabEntryKey, Object> attributes = new TreeMap<>();
    private final CharSequence name;
    private final SymTab container;

    SymTabEntry(CharSequence name, SymTab container) {
        this.name = name;
        this.container = container;
    }
    
    public Object getAttribute(SymTabEntryKey key) {
        return attributes.get(key);
    }
    
    public void setAttribute(SymTabEntryKey key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * access to full scope of this entry
     * @return 
     */
    public SymTab getSymTab() {
        return container;
    }

    @Override
    public String toString() {
        return "Entry{name=" + name + ", level=" + container.getNestingLevel() + "\n  attributes=" + attributes + '}'; // NOI18N
    }
}
