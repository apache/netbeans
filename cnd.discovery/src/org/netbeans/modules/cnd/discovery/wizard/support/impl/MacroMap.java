/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
