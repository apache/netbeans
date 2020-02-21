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
package org.netbeans.modules.cnd.discovery.wizard.support.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MacroMap {
    private final Map<String, Value> storage = new HashMap<>();
    
    public MacroMap() {
    }

    public MacroMap(MacroMap parent) {
        storage.putAll(parent.storage);
    }

    public void addAll(List<String> list) {
        for(String macro : list){
            int i = macro.indexOf('=');
            String key;
            String value;
            if ( i > 0){
                key = macro.substring(0,i).trim();
                value = macro.substring(i+1).trim();
            } else {
                key = macro;
                value = null;
            }
            Value v = storage.get(key);
            if (v == null) {
                v = new Value();
                storage.put(key, v);
            }
            v.addValue(value);
        }
    }
    
    public int size() {
        return storage.size();
    }

    public void retainAll(List<String> list) {
        Map<String,String> keys = new HashMap<>();
        for(String macro : list){
            int i = macro.indexOf('=');
            String key;
            String value;
            if ( i > 0){
                key = macro.substring(0,i).trim();
                value = macro.substring(i+1).trim();
            } else {
                key = macro;
                value = null;
            }
            keys.put(key, value);
        }
        for(String s : new ArrayList<>(storage.keySet())) {
            if (!keys.containsKey(s)) {
                storage.remove(s);
            } else {
                String v = keys.get(s);
                Value value = storage.get(s);
                value.addValue(v);
            }
        }
    }
    
    public List<String> convertToList() {
        List<String> external = new ArrayList<>();
        for(Map.Entry<String,Value> e : storage.entrySet()) {
            String key = e.getKey();
            String value = e.getValue().getDominante();
            if (value == null) {
                external.add(key);
            } else {
                external.add(key+"="+value); //NOI18N
            }
        }
        return external;
    }
    
    public List<String> removeCommon(List<String> list) {
        List<String> diff = new ArrayList<>();
        for(String macro : list){
            int i = macro.indexOf('=');
            String key;
            String value;
            if ( i > 0){
                key = macro.substring(0,i).trim();
                value = macro.substring(i+1).trim();
            } else {
                key = macro;
                value = null;
            }
            Value v = storage.get(key);
            if (v == null) {
                diff.add(macro);
                continue;
            }
            String dominante = v.getDominante();
            if (value == null && dominante == null) {
                continue; 
            } else if (value != null && dominante != null && value.equals(dominante)) {
                continue; 
            }
            diff.add(macro);
        }
        return diff;
    }
    
    private static class Value {
        private final List<String> values = new ArrayList<>();
        private final List<Integer> counts = new ArrayList<>();
        private void addValue(String value) {
            int i = values.indexOf(value);
            if (i < 0) {
                values.add(value);
                counts.add(Integer.valueOf(1));
            } else {
                int c = counts.get(i);
                counts.set(i, Integer.valueOf(c+1));
            }
        }
        private String getDominante() {
            int res = -1;
            int count = 0;
            for(int i = 0; i < counts.size(); i++) {
                int v = counts.get(i);
                if (v > count) {
                    count = v;
                    res = i;
                }
            }
            return values.get(res);
        }
    }
}
