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

import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 *
 * @author Jiri Sedlacek
 */
public class TextFilter extends GenericFilter {
    
    public static final int TYPE_REGEXP = 30;
    
    
    private static final String[] NORMALIZED_NOT_READY = new String[0];
    
    private String[] normalizedValues = NORMALIZED_NOT_READY;
    private transient Pattern[] regexpPatterns;
    
    
    public TextFilter() {
        super(null, "", TYPE_INCLUSIVE); // NOI18N
    }
    
    public TextFilter(String value, int type, boolean caseSensitive) {
        super(null, value, type);
        setCaseSensitive(caseSensitive);
    }
    
    public TextFilter(Properties properties, String id) {
        super(properties, id);
    }
    
    
    public void copyFrom(TextFilter other) {
        super.copyFrom(other);
        
        normalizedValues = other.normalizedValues;
        regexpPatterns = other.regexpPatterns;
    }
    
    
    public boolean isAll() {
//        return getType() == TYPE_REGEXP ? isEmpty() : super.isAll();
        return isEmpty();
    }
    
    
    public final void setCaseSensitive(boolean caseSensitive) {
//        if (caseSensitive != isCaseSensitive()) setValue(getValue()); // resets precomputed values
        if (caseSensitive || getType() == TYPE_REGEXP) {
            normalizedValues = null;
        } else {
            normalizedValues = NORMALIZED_NOT_READY;
        }
    }
    
    public final boolean isCaseSensitive() {
        return normalizedValues == null;
    }
    
    
    protected void valueChanged() {
        super.valueChanged();
        
        if (!isCaseSensitive()) normalizedValues = NORMALIZED_NOT_READY;
        regexpPatterns = null;
    }
    
//    protected String[] computeValues(String value) {
//        return getType() == TYPE_REGEXP ? super.computeValues(value) :
//               super.computeValues(value.replace('*', ' ')); // NOI18N
//    }
    
    
    public boolean passes(String string) {
        if (getType() == TYPE_REGEXP) {
            String[] values = getValues();
            
            if (regexpPatterns == null) regexpPatterns = new Pattern[values.length];
            
            for (int i = 0; i < regexpPatterns.length; i++) {
                if (regexpPatterns[i] == null) 
                    try {
                        regexpPatterns[i] = Pattern.compile(values[i]);
                    } catch (RuntimeException e) {
                        handleInvalidFilter(values[i], e);
                        regexpPatterns[i] = Pattern.compile(".*"); // NOI18N
                    }
                if (regexpPatterns[i].matcher(string).matches()) return true;
            }
            
            return false;
        } else {
//            return super.passes(string);
            if (simplePasses(string)) return true;
            
            String[] values = getValues();
            
            boolean caseSensitive = isCaseSensitive();
            if (!caseSensitive) {
                string = normalizeString(string);
                if (normalizedValues == NORMALIZED_NOT_READY) normalizedValues = new String[values.length];
            }
            
            for (int i = 0; i < values.length; i++) {
                String value;
                if (!caseSensitive) {
                    if (normalizedValues[i] == null) normalizedValues[i] = normalizeString(values[i]);
                    value = normalizedValues[i];
                } else {
                    value = values[i];
                }
                if (string.contains(value)) return getType() == TYPE_INCLUSIVE;
            }

            return getType() != TYPE_INCLUSIVE;
        }
    }
    
    
    protected void handleInvalidFilter(String invalidValue, RuntimeException e) {}
    
    
    private static String normalizeString(String string) {
        // NOTE: comparing String.toLowerCase doesn't work correctly for all locales
        // but is much faster than using String.equalsIgnoreCase or an exact algorithm
        // for case-insensitive comparison
        return string.toLowerCase(Locale.ENGLISH);
    }
    
    
    protected boolean valuesEquals(Object obj) {
        if (!super.valuesEquals(obj)) return false;
        
        TextFilter other = (TextFilter)obj;
        if (normalizedValues == null) {
            if (other.normalizedValues != null) return false;
        } else {
            if (other.normalizedValues == null) return false;
        }
        
        return true;
    }
    
    protected int valuesHashCode(int hashBase) {
        hashBase = super.valuesHashCode(hashBase);
        
        if (normalizedValues == null) hashBase = 67 * hashBase;
        
        return hashBase;
    }
    
}
