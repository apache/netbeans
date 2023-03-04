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
package org.netbeans.lib.profiler.filters;

import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Jiri Sedlacek
 */
public class GenericFilter {
    
    public static final int TYPE_NONE = 0;
    public static final int TYPE_INCLUSIVE = 10;
    public static final int TYPE_EXCLUSIVE = 20;
    
    protected static final int MODE_EQUALS = 1000;
    protected static final int MODE_CONTAINS = 1010;
    protected static final int MODE_STARTS_WITH = 1020;
    protected static final int MODE_ENDS_WITH = 1030;
    
    private static final String PROP_NAME = "NAME"; // NOI18N
    private static final String PROP_VALUE = "VALUE"; // NOI18N
    private static final String PROP_TYPE = "TYPE"; // NOI18N
    
    
    private String name;
    private String value;
    private transient String[] values;
    
    private int type;
    private transient int[] modes;
    
    private Boolean isEmpty;
    private Boolean isAll;
    
    
    public GenericFilter() {
        this(null, "", TYPE_NONE); // NOI18N
    }
    
    public GenericFilter(GenericFilter other) {
        this(other.name, other.value, other.values, other.type, other.modes);
    }
    
    public GenericFilter(String name, String value, int type) {
        this(name, value, null, type, null);
    }
    
    public GenericFilter(Properties properties, String id) {
        this(loadName(properties, id), loadValue(properties, id), loadType(properties, id));
    }
    
    private GenericFilter(String name, String value, String[] values, int type, int[] modes) {
        this.name = name;
        this.value = value;
        this.values = values; // arrays are shared as long as the instances don't change, array of the changed instance is nulled
        this.type = type;
        this.modes = modes; // arrays are shared as long as the instances don't change, array of the changed instance is nulled
    }
    
    
    public void copyFrom(GenericFilter other) {
        name = other.name;
        value = other.value;
        values = other.values; // arrays are shared as long as the instances don't change, array of the changed instance is nulled
        type = other.type;
        modes = other.modes; // arrays are shared as long as the instances don't change, array of the changed instance is nulled
        isEmpty = other.isEmpty;
        isAll = other.isAll;
    }
    
    
    public final void setName(String name) {
        this.name = name;
    }
    
    public final String getName() {
        return name;
    }
    
    public final void setValue(String value) {
        this.value = value;
        valueChanged();
    }
    
    protected void valueChanged() {
        values = null;
        modes = null;
        isEmpty = null;
        isAll = null;
    }
    
    public final String getValue() {
        return value;
    }

    public final String[] getValues() {
        if (values == null) values = computeValues(value);
        return values;
    }
    
    public final void setType(int type) {
        this.type = type;
    }
    
    public final int getType() {
        return type;
    }
    
    public final int[] getModes() {
        if (modes == null) modes = computeModes(getValues());
        return modes;
    }
    
    
    public final boolean isEmpty() {
        if (isEmpty == null) isEmpty = value.isEmpty();
        return isEmpty;
    }
    
    public boolean isAll() {
        if (isAll == null) isAll = isEmpty() || "*".equals(value) || "**".equals(value); // NOI18N
        return isAll;
    }

    
//    protected String computeValue(String[] values) { return value(values); }
//    
//    public static String value(String[] values) {
//        int length = values.length;
//
//        if (length == 0) return ""; // NOI18N
//        if (length == 1) return values[0];
//
//        StringBuilder b = new StringBuilder();
//        for (int i = 0; i < length - 1; i++)
//            b.append(values[i]).append(", "); // NOI18N
//        b.append(values[values.length - 1]);
//
//        return b.toString().trim();
//    }
    
    protected String[] computeValues(String _value) {
        return values(_value);
    }
    
    public static String[] values(String _value) {
        return _value.replace(',', ' ').split(" +"); // NOI18N
    }
    
    protected int[] computeModes(String[] _values) {
        int length = _values.length;
        int[] _modes = new int[length];
        
        for (int i = 0; i < length; i++) {
            String _value = _values[i];
            int vlength = _value == null ? 0 : _value.length();
            
            if (vlength == 0) {
                _modes[i] = MODE_CONTAINS;
                continue;
            }
            
            boolean startsWith = _value.charAt(0) == '*'; // NOI18N
            boolean endsWith = _value.charAt(vlength - 1) == '*'; // NOI18N
            if (startsWith) _value = _value.substring(1);
            if (endsWith) _value = _value.substring(0, vlength - (startsWith ? 2 : 1));
            _values[i] = _value;
            
            if (_value.isEmpty()) {
                _modes[i] = MODE_CONTAINS;
                continue;
            }
            
            if (startsWith) {
                if (endsWith) _modes[i] = MODE_CONTAINS;
                else _modes[i] = MODE_ENDS_WITH;
            } else if (endsWith) {
                _modes[i] = MODE_STARTS_WITH;
            } else {
                _modes[i] = MODE_EQUALS;
            }
        }
        return _modes;
    }
    
    
    public boolean passes(String string) {
        if (simplePasses(string)) return true;
        
        boolean inclusive = type == TYPE_INCLUSIVE;
        
        String[] _values = getValues();
        int[] _modes = getModes();
        for (int i = 0; i < _values.length; i++)
            if (matches(string, _values[i], _modes[i]))
                return inclusive;
        
        return !inclusive;
    }
    
    protected boolean simplePasses(String string) {
        if (type == TYPE_NONE) return true;
        
        boolean inclusive = type == TYPE_INCLUSIVE;
        
        if (isAll()) return inclusive;
        if (getValues().length == 0) return inclusive;
        
        return false;
    }
        
    protected boolean matches(String string, String filter, int mode) {
        if (filter.isEmpty()) return true;
        
        switch (mode) {
            case MODE_STARTS_WITH:
                return string.startsWith(filter);
            case MODE_EQUALS:
                return string.equals(filter);
            case MODE_ENDS_WITH:
                return string.endsWith(filter);
            case MODE_CONTAINS:
                return string.contains(filter);
        }
        return false;
    }
    
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!obj.getClass().isAssignableFrom(getClass()) &&
            !getClass().isAssignableFrom(obj.getClass())) return false;

        return valuesEquals(obj);
    }
    
    protected boolean valuesEquals(Object obj) {
        GenericFilter other = (GenericFilter)obj;
        
        if (!Objects.equals(name, other.name)) return false;
        if (!value.equals(other.value)) return false;
        if (type != other.type) return false;
        
        return true;
    }
    
    public int hashCode() {
        return valuesHashCode(3);
    }
    
    protected int valuesHashCode(int hashBase) {
        if (name != null) hashBase = 67 * hashBase + name.hashCode();
        hashBase = 67 * hashBase + value.hashCode();
        hashBase = 67 * hashBase + type;
        
        return hashBase;
    }
    
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append(getClass().getName());
        b.append("["); // NOI18N
        b.append("name: ").append(getName()); // NOI18N
        b.append(", value: ").append(getValue()); // NOI18N
        b.append(", type: ").append(typeString(getType())); // NOI18N
        b.append("]"); // NOI18N
        
        return b.toString();
    }
    
    private static String typeString(int type) {
        switch (type) {
            case TYPE_NONE: return "TYPE_NONE"; // NOI18N
            case TYPE_INCLUSIVE: return "TYPE_INCLUSIVE"; // NOI18N
            case TYPE_EXCLUSIVE: return "TYPE_EXCLUSIVE"; // NOI18N
            default: return "unknown"; // NOI18N
        }
    }
    
    
    public void store(Properties properties, String id) {
        if (name == null) properties.remove(id + PROP_NAME); else properties.put(id + PROP_NAME, name);
        properties.put(id + PROP_VALUE, value);
        properties.put(id + PROP_TYPE, Integer.toString(type));
    }
    
    
    private static String loadName(Properties properties, String id) {
        return properties.getProperty(id + PROP_NAME);
    }
    
    private static String loadValue(Properties properties, String id) {
        String _value = properties.getProperty(id + PROP_VALUE);
        if (_value == null) throw new InvalidFilterIdException("No filter value found", id); // NOI18N
        return _value;
    }
    
    private static int loadType(Properties properties, String id) {
        String _type = properties.getProperty(id + PROP_TYPE);
        if (_type == null) throw new InvalidFilterIdException("No filter type found", id); // NOI18N
        try { return Integer.parseInt(_type); } catch (NumberFormatException e)
            { throw new InvalidFilterIdException("Bad filter type specified", id); } // NOI18N
    }
    
    
    public static final class InvalidFilterIdException extends IllegalArgumentException {
        
        public InvalidFilterIdException(String message, String filterId) {
            super(message + " for filter id " + filterId); // NOI18N
        }
        
    }
    
}
