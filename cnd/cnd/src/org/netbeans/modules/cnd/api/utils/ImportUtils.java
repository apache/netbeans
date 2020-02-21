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

package org.netbeans.modules.cnd.api.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class ImportUtils {

    private ImportUtils() {
    }

    public static List<String> quoteList(List<String> list){
        List<String> res = new ArrayList<String>();
        for (String s : list){
            int i = s.indexOf('='); // NOI18N
            if (i > 0){
                String rest = s.substring(i+1);
                s = s.substring(0,i+1);
                if (rest.startsWith("\"")){ // NOI18N
                    rest = "'"+rest+"'"; // NOI18N
                } else if (rest.startsWith("'")){ // NOI18N
                    rest = "\""+rest+"\""; // NOI18N
                } else {
                    if (rest.indexOf('\\') >= 0) {
                        StringBuilder buf = new StringBuilder();
                        for(int j = 0; j < rest.length(); ) {
                            char c = rest.charAt(j);
                            switch (c) {
                                case '\\': //NOI18N
                                    j++;
                                    if (j < rest.length()) {
                                        char c2 = rest.charAt(j);
                                        if (c2 == ' ' || c == ':' || c == '*') {
                                            j++;
                                            buf.append(c2);
                                        }
                                    } else {
                                        buf.append(c);
                                    }
                                    continue;
                                default:
                                    j++;
                                    buf.append(c);
                                    continue;
                            }
                        }
                        rest = buf.toString();
                    }
                    if (rest.indexOf(' ')>0 || rest.indexOf('=')>0) { // NOI18N
                        rest = "\""+rest+"\""; // NOI18N
                    }
                }
                res.add(s+rest);
            }
        }
        return res;
    }

    public static List<String> normalizeParameters(List<String> list){
        List<String> res = new ArrayList<String>();
        for (String s : list){
            if (s.startsWith("'") && s.endsWith("'") || // NOI18N
                s.startsWith("\"") && s.endsWith("\"")){ // NOI18N
                s = s.substring(1,s.length()-1);
            }
            int i = s.indexOf('='); // NOI18N
            if (i > 0){
                String rest = s.substring(i+1);
                String var = s.substring(0,i+1);
                if (var.startsWith("-DCMAKE")) { //NOI18N
                    // cmake does not remove quotes of flags parameters
                    if (rest.startsWith("'") && rest.endsWith("'") || // NOI18N
                        rest.startsWith("\"") && rest.endsWith("\"")){ // NOI18N
                        rest = rest.substring(1,rest.length()-1);
                        s = var+rest;
                    }
                }
            }
            res.add(s);
        }
        return res;
    }

    public static List<String> parseEnvironment(String s) {
        return parse(s, true);
    }

    public static List<String> parseArgs(String s) {
        return parse(s, false);
    }

    private static List<String> parse(String s, boolean onlyEnv) {
        List<String> res = new ArrayList<String>();
        if (s == null) {
            return res;
        }
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        int inQuote = 0;
        int q;
        boolean inValue = false;
        for(int i = 0; i < s.length(); ) {
            char c = s.charAt(i);
            switch (c) {
                case '-': //NOI18N
                    if (inQuote != 0 || inValue) {
                        if (inValue) {
                            value.append(c);
                        }
                        i++;
                        continue;
                    }
                    if (!onlyEnv) {
                        key.append(c);
                    }
                    i++;
                    q = 0;
                    char prev = 0;
                    for(;i < s.length(); i++){
                        c = s.charAt(i);
                        if (c == '"' || c == '\''){ //NOI18N
                            if (q == 0) {
                                q = c;
                            } else if (q == c) {
                                q = 0;
                            }
                        } else if (c == ' ') {
                            if (q == 0) {
                                if (prev != '\\') { //NOI18N
                                    break;
                                } else {
                                    if (!onlyEnv) {
                                        key.setLength(key.length()-1);
                                    }
                                }
                            }
                        } else if (prev == '\\') {
                            if (!onlyEnv) {
                                key.setLength(key.length()-1);
                            }
                        }
                        if (!onlyEnv) {
                            key.append(c);
                        }
                        prev = c;
                    }
                    continue;
                case ' ': //NOI18N
                    if (inQuote != 0) {
                        if (inValue) {
                            value.append(c);
                        }
                        i++;
                        continue;
                    }
                    if (inValue) {
                        if (key.length() > 0) {
                            res.add(key+"="+value); //NOI18N
                        }
                        inValue = false;
                    } else if (!onlyEnv){
                        if (key.length() > 0) {
                            res.add(key.toString());
                        }
                    }

                    key.setLength(0);
                    value.setLength(0);
                    i++;
                    continue;
                case '\'': //NOI18N
                case '"': //NOI18N
                    if (inQuote == 0) {
                        if (!onlyEnv && !inValue){
                            q = 0;
                            for(;i < s.length(); i++){
                                c = s.charAt(i);
                                if (c == '"' || c == '\''){ //NOI18N
                                    if (q == 0) {
                                        q = c;
                                        continue;
                                    } else if (q == c) {
                                        q = 0;
                                        continue;
                                    }
                                }
                                if (q == 0 && c == ' '){ //NOI18N
                                    break;
                                }
                                key.append(c);
                            }
                            continue;
                        }
                        inQuote = c;
                    } else if (inQuote == c) {
                        inQuote = 0;
                    } else {
                        if (inValue) {
                            value.append(c);
                        }
                    }
                    i++;
                    continue;
                case '=': //NOI18N
                    if (inQuote == 0) {
                        value.setLength(0);
                        inValue = true;
                    } else {
                        if (inValue) {
                            value.append(c);
                        } else if (!onlyEnv){
                            key.append(c);
                        }
                    }
                    i++;
                    continue;
                case '\\': //NOI18N
                    i++;
                    if (i < s.length()) {
                        char c2 = s.charAt(i);
                        if (c2 == ' ') {
                            i++;
                            if (inValue) {
                                value.append(c2);
                            } else {
                                key.append(c2);
                            }
                            continue;
                        } 
                    }
                    if (inValue) {
                        value.append(c);
                    } else {
                        key.append(c);
                    }
                    continue;
                default:
                    if (inValue) {
                        value.append(c);
                    } else {
                        key.append(c);
                    }
                    i++;
                    continue;
            }
        }
        if (inValue) {
            if (key.length() > 0) {
                res.add(key+"="+value); //NOI18N
            }
        } else if (!onlyEnv){
            if (key.length() > 0) {
                res.add(key.toString());
            }
        }
        return res;
    }
}
