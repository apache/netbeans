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

package org.netbeans.upgrade.systemoptions;

import java.util.Iterator;
import java.util.List;
import org.netbeans.upgrade.systemoptions.SerParser.ArrayWrapper;
import org.netbeans.upgrade.systemoptions.SerParser.NameValue;
import org.netbeans.upgrade.systemoptions.SerParser.ObjectWrapper;

/**
 *
 * @author rmatous
 */
final class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {}

    
    static String valueFromObjectWrapper(final Object value) {
        String stringvalue = null;
        if (value instanceof ObjectWrapper) {
            List l = ((SerParser.ObjectWrapper)value).data;
            if (l.size() == 1) {
                Object o = l.get(0);
                if (o instanceof NameValue) {
                    stringvalue = ((NameValue) o).value.toString();
                }
            }
            if (stringvalue == null) {
                stringvalue = ((ObjectWrapper) value).classdesc.name;
            }
        }  else if (value instanceof String && !"null".equals(value)) {
            stringvalue = value.toString();
            
        } else if (value instanceof SerParser.ArrayWrapper && "[Ljava.lang.String;".equals(((SerParser.ArrayWrapper)value).classdesc.name)) {
            StringBuilder sb = new StringBuilder();
            List es = ((SerParser.ArrayWrapper)value).values;
            for (Iterator it = es.iterator(); it.hasNext();) {
                sb.append((String)it.next());
                if (it.hasNext()) {
                    sb.append(" , ");
                }                
            }
            stringvalue = sb.toString();            
        } else if (value instanceof SerParser.ArrayWrapper && "[[Ljava.lang.String;".equals(((SerParser.ArrayWrapper)value).classdesc.name)) {
            StringBuilder sb = new StringBuilder();
            List awl = ((SerParser.ArrayWrapper)value).values;
            for (Iterator it = awl.iterator(); it.hasNext();) {
                SerParser.ArrayWrapper aw = (SerParser.ArrayWrapper)it.next();
                sb.append(valueFromObjectWrapper(aw));
                if (it.hasNext()) {
                    sb.append(" | ");
                }
            }
            stringvalue = sb.toString();            
        } else {
            stringvalue = "unknown";//value.toString();
        }
        return stringvalue;
    }
    
    static String getClassNameFromObject(final Object value) {
        String clsName;
        if (value instanceof ObjectWrapper) {
            clsName = prettify(((ObjectWrapper) value).classdesc.name);
        }  else if (value instanceof ArrayWrapper) {
            clsName = prettify(((ArrayWrapper) value).classdesc.name);
        }  else {
            clsName = prettify(value.getClass().getName());
        }
        return clsName;
    }
    
    static String prettify(String type) {
        if (type.equals("B")) { // NOI18N
            return "byte"; // NOI18N
        } else if (type.equals("S")) { // NOI18N
            return "short"; // NOI18N
        } else if (type.equals("I")) { // NOI18N
            return "int"; // NOI18N
        } else if (type.equals("J")) { // NOI18N
            return "long"; // NOI18N
        } else if (type.equals("F")) { // NOI18N
            return "float"; // NOI18N
        } else if (type.equals("D")) { // NOI18N
            return "double"; // NOI18N
        } else if (type.equals("C")) { // NOI18N
            return "char"; // NOI18N
        } else if (type.equals("Z")) { // NOI18N
            return "boolean"; // NOI18N
        } else if (type.startsWith("L") && type.endsWith(";")) { // NOI18N
            String fqn = type.substring(1, type.length() - 1).replace('/', '.').replace('$', '.'); // NOI18N
            return fqn;
        }
        if (!type.startsWith("[")) {
            if (type.startsWith("L")) {
                return type.substring(1);
            }
            if (type.endsWith(";")) {
                return type.substring(0,type.length()-1);
            }
        }
        return type;
    }
}
