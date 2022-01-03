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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public final class SymTab {
    private final Map<CharSequence, SymTabEntry> entries = new TreeMap<>();
    
//    private final Map<CharSequence, SymTabEntry> imported = new TreeMap<CharSequence, SymTabEntry>();
    private final List<SymTab> imported = new ArrayList<>();  
    private boolean lookupMark;                                     // to avoid loops during lookup
    
    private final int nestingLevel;
    private final CharSequence name;

    SymTab(int nestingLevel, CharSequence name) {
        this.nestingLevel = nestingLevel;
        this.name = name;
    }

    public CharSequence getName() {
        return name;
    }
    
    SymTabEntry lookup(CharSequence entry) {
        SymTabEntry out = entries.get(entry);
        if (out == null && !lookupMark) {
            // out = imported.get(entry);
            lookupMark = true;
            
            try {            
                ListIterator<SymTab> iter = imported.listIterator(imported.size());
                while (out == null && iter.hasPrevious()) {
                    out = iter.previous().lookup(entry);
                }            
            } finally {
                lookupMark = false;
            }
        }
        return out;
    }

    SymTabEntry enter(CharSequence entry) {
        SymTabEntry newEntry = new SymTabEntry(entry, this);
        entries.put(entry, newEntry);
        return newEntry;
    }

    void importSymTab(SymTab symTab) {
//        imported.putAll(symTab.entries);
        imported.add(symTab);
    }

    int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public String toString() {
        return "SymTab{name=" + name + ", nestingLevel=" + nestingLevel + ", entries=" + entries + ", imported=" + imported + '}'; // NOI18N
    }
    
    
}
