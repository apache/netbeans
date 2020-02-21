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
import org.openide.util.CharSequences;

/**
 *
 */
public final class SymTabStack {
    private final ArrayList<SymTab> stack = new ArrayList<>();

    public static SymTabStack create() {
        return new SymTabStack();
    }
    
    private SymTabStack() {
       
    }
    
    public SymTab push() {        
        SymTab symTab = new SymTab(stack.size(), CharSequences.empty());
        stack.add(symTab);
        return symTab;
    }
    
    public SymTab push(CharSequence name) {        
        assert CharSequences.isCompact(name) : "only compact strings allowed";
        SymTab symTab = new SymTab(stack.size(), name);
        stack.add(symTab);
        return symTab;
    }    

    public SymTab push(SymTab symTab) {        
        stack.add(symTab);
        return symTab;
    }
    
    public SymTab pop() {
        if(!stack.isEmpty()) {
            return stack.remove(stack.size() - 1);
        }
        return null;
    }

    public SymTab pop(CharSequence name) {
        if(!stack.isEmpty()) {
            if(stack.get(stack.size() - 1).getName().equals(name)) {
                return stack.remove(stack.size() - 1);
            }
        }
        return null;
    }
    
    public SymTabEntry lookupLocal(CharSequence entry) {
        assert CharSequences.isCompact(entry) : "only compact strings allowed";
        return getLocal().lookup(entry);
    }
    
    public SymTabEntry lookup(CharSequence entry) {
        assert CharSequences.isCompact(entry) : "only compact strings allowed";
        assert stack.size() > 0;
        SymTabEntry out = null;
        for (int i = stack.size() - 1; i >= 0; i--) {
            out = stack.get(i).lookup(entry);
            if (out != null) {
                break;
            }
        }
        return out;
    }
    
    public SymTabEntry enterLocal(CharSequence entry) {
        return getLocal().enter(entry);
    }
    
    public void importToLocal(SymTab symTab) {
        getLocal().importSymTab(symTab);
    }
    
    public int getSize() {
        return stack.size();
    }

    private SymTab getLocal() {
        return stack.get(stack.size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SymTabStack, nestingLevel=").append(stack.size()); //NOI18N
        sb.append(", stack="); //NOI18N
        for (SymTab symTab : stack) {
            sb.append("\n").append(symTab); //NOI18N
        }
        return sb.toString();
    }
}
