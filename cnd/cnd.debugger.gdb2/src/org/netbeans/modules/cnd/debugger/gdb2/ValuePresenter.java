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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ValuePresenter {
    private ValuePresenter() {
    }

    private static final Presenter[] presenters = new Presenter[]{new StdStringPresenter(), new StdVectorPresenter()};

    public static String getValue(String value) {
        return getValue(null, value);
    }

    public static String getValue(String type, String value) {
        for (Presenter vp : presenters) {
            if (vp.accepts(type, value)) {
                return vp.present(type, value);
            }
        }
        return value;
    }
    
    public static boolean acceptsType(String type) {
        for (Presenter vp : presenters) {
            if (vp.acceptsType(type)) {
                return true;
            }
        }
        return false;
    }
    
    private static interface Presenter {
        boolean acceptsType(String type);
        boolean accepts(String type, String value);
        String present(String type, String value);
    }
    
    private static class StdVectorPresenter implements Presenter {

        @Override
        public boolean acceptsType(String type) {
            return type != null && type.startsWith("std::vector");//NOI18N
        }

        @Override
        public boolean accepts(String type, String value) {
            return acceptsType(type);
        }

        @Override
        public String present(String type, String value) {
            return value;
        }
        
    }

    private static class StdStringPresenter implements Presenter {
        private static final String VALUE_PREFIX = "_M_p"; // NOI18N
        private static final Set<String> TYPES = new HashSet<String>();
        static {
            TYPES.add("string"); // NOI18N
            TYPES.add("string &"); // NOI18N
            TYPES.add("std::string"); // NOI18N
            TYPES.add("std::string &"); // NOI18N
            TYPES.add("std::locale::string"); // NOI18N
            TYPES.add("std::locale::string &"); // NOI18N
        }

        @Override
        public boolean acceptsType(String type) {
            return TYPES.contains(type);
        }
        
        @Override
        public boolean accepts(String type, String value) {
            // if type is not provided - try to check value
            if (type == null) {
                return checkValue(value);
            }
            return acceptsType(type) && value != null && value.contains(VALUE_PREFIX);
        }

        // checks if value is of string type
        private static boolean checkValue(String value) {
            if (value == null || value.length() < 10) {
                return false;
            }
            int start = value.indexOf('{');
            if (start == -1) {
                return false;
            }
            int end = value.lastIndexOf('}');
            if (end == -1 || end < start+1) {
                return false;
            }
            value = value.substring(start+1, end);
            if (value.indexOf("static npos") == -1) {// NOI18N
                return false;
            }
            return value.indexOf(VALUE_PREFIX) != -1;
        }

        @Override
        public String present(String type, String value) {
            int pos = value.indexOf(VALUE_PREFIX);
            assert pos > 0;
            pos = value.indexOf('"', pos);
            if (pos > 0) {
                // fix for \" quotes
                if (value.charAt(pos-1) == '\\') {
                    return present(type, value.replace("\\\"", "\"")); // NOI18N
                }
                int end = findEndOfString(value, pos+1);
                if (end != -1) {
                    return value.substring(pos, end+1);
                }
            }
            return value;
        }
    }
    
    /** Find the end of a string by looking for a non-escaped double quote */
    private static int findEndOfString(String s, int idx) {
        int len = s.length();

        for (;idx < len;idx++) {
            char ch = s.charAt(idx);
            if (ch == '"' && !isSlashBefore(s, idx)) {
                return idx;
            } 
        }
        throw new IllegalStateException("Failed to find end of string: " + s); // NOI18N
    }
    
    /**
     * Checks if the character at the specified pos has an odd number of slahses before
     * ///" - is ok, //" is not ok
     */
    private static boolean isSlashBefore(String source, int pos) {
        int count = 0;
        pos--;
        while (pos > 0 && source.charAt(pos) == '\\') {
            pos--;
            count++;
        }
        return count % 2 == 1;
    }
}
