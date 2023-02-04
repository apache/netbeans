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
package org.netbeans.modules.profiler.heapwalk.details.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;

/**
 *
 * @author Jiri Sedlacek
 */
public final class DetailsUtils {
    
    public static final int MAX_ARRAY_LENGTH = 160;
    
    
    // --- Check types ---------------------------------------------------------
    
    public static boolean isInstanceOf(Instance instance, String clsName) {
        if (instance == null) return false;
        return instance.getJavaClass().getName().equals(clsName);
    }
    
    public static boolean isSubclassOf(Instance instance, String clsName) {
        if (instance == null) return false;
        JavaClass cls = instance.getJavaClass();
        
        // NOTE: currently optimized for Builders, two-dimensional cache would be more general
        if (!cls.getName().equals(LAST_SUBCLASS_INSTANCE)) {
            SUBCLASS_CACHE.clear();
        } else {
            Boolean subclass = SUBCLASS_CACHE.get(clsName);
            if (subclass != null) return subclass.booleanValue();
        }
        
        LAST_SUBCLASS_INSTANCE = cls.getName();
        
        boolean result = false;
        while (cls != null) {
            if (cls.getName().equals(clsName)) {
                result = true;
                break;
            }
            cls = cls.getSuperClass();
        }
        
        SUBCLASS_CACHE.put(clsName, Boolean.valueOf(result));
        
        return result;
    }
    
    private static String LAST_SUBCLASS_INSTANCE;
    private static final LinkedHashMap<String, Boolean> SUBCLASS_CACHE =
            new LinkedHashMap<String, Boolean>(100) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 5000;
                }
            };
    
    
    // --- Primitive types -----------------------------------------------------
    
    public static boolean getBooleanFieldValue(Instance instance, String field, boolean def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Boolean ? ((Boolean)value).booleanValue() : def;
    }
    
    public static byte getByteFieldValue(Instance instance, String field, byte def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Byte ? ((Byte)value).byteValue() : def;
    }
    
    public static char getCharFieldValue(Instance instance, String field, char def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Character ? ((Character)value).charValue() : def;
    }
    
    public static double getDoubleFieldValue(Instance instance, String field, double def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Double ? ((Double)value).doubleValue() : def;
    }
    
    public static float getFloatFieldValue(Instance instance, String field, float def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Float ? ((Float)value).floatValue() : def;
    }
    
    public static int getIntFieldValue(Instance instance, String field, int def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Integer ? (Integer) value : def;
    }
    
    public static long getLongFieldValue(Instance instance, String field, long def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Long ? ((Long)value).longValue() : def;
    }
    
    public static short getShortFieldValue(Instance instance, String field, short def) {
        Object value = instance.getValueOfField(field);
        return value instanceof Short ? ((Short)value).shortValue() : def;
    }
    
    
    // --- Primitive arrays ----------------------------------------------------
    
    public static String getPrimitiveArrayFieldString(Instance instance, String field, int offset, int count, String separator, String trailer) {
        Object value = instance.getValueOfField(field);
        return value instanceof Instance ? getPrimitiveArrayString((Instance)value,
                                           offset, count, separator, trailer) : null;
    }
    
    public static String getPrimitiveArrayString(Instance instance, int offset, int count, String separator, String trailer) {
        List<String> values = getPrimitiveArrayValues(instance);
        if (values != null) {
            int valuesCount = count < 0 ? values.size() - offset :
                              Math.min(count, values.size() - offset);            
            int separatorLength = separator == null ? 0 : separator.length();
            int trailerLength = trailer == null ? 0 : trailer.length();
            int estimatedSize = Math.min(valuesCount * (1 + separatorLength), MAX_ARRAY_LENGTH + trailerLength);
            StringBuilder value = new StringBuilder(estimatedSize);
            int lastValue = offset + valuesCount - 1;
            for (int i = offset; i <= lastValue; i++) {
                value.append(values.get(i));
                if (value.length() >= MAX_ARRAY_LENGTH) {
                    if (trailerLength > 0) value.append(trailer);
                    break;
                }
                if (separator != null && i < lastValue) value.append(separator);
            }
            return value.toString();
        }
        return null;
    }
    
    public static List<String> getPrimitiveArrayFieldValues(Instance instance, String field) {
        Object value = instance.getValueOfField(field);
        if (value instanceof Instance) return getPrimitiveArrayValues((Instance)value);
        return null;
    }
    
    public static List<String> getPrimitiveArrayValues(Instance instance) {
        if (instance instanceof PrimitiveArrayInstance) {
            PrimitiveArrayInstance array = (PrimitiveArrayInstance)instance;
            return array.getValues();
        }
        return null;
    }
    
    
    // --- Object types --------------------------------------------------------
    
    public static String getInstanceFieldString(Instance instance, String field, Heap heap) {
        Object value = instance.getValueOfField(field);
        return value instanceof Instance ? getInstanceString((Instance)value, heap) : null;
    }
    
    public static String getInstanceString(Instance instance, Heap heap) {
        return DetailsSupport.getDetailsString(instance, heap);
    }
    
    
    // --- Create arrays -------------------------------------------------------
    
    public static boolean[] getBooleanArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            boolean[] values = new boolean[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Boolean.parseBoolean(valuesList.get(i));
            return values;
        } catch (OutOfMemoryError e) {
            return new boolean[0];
        }
    }
    
    public static byte[] getByteArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            byte[] values = new byte[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Byte.parseByte(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Byte.parseByte(String)
            return new byte[0];
        } catch (OutOfMemoryError e) {
            return new byte[0];
        }
    }
    
    public static char[] getCharArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            char[] values = new char[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = valuesList.get(i).charAt(0);
            return values;
        } catch (IndexOutOfBoundsException e) { // String.charAt(0)
            return new char[0];
        } catch (OutOfMemoryError e) {
            return new char[0];
        }
    }
    
    public static double[] getDoubleArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            double[] values = new double[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Double.parseDouble(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Double.parseDouble(String)
            return new double[0];
        } catch (OutOfMemoryError e) {
            return new double[0];
        }
    }
    
    public static float[] getFloatArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            float[] values = new float[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Float.parseFloat(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Float.parseFloat(String)
            return new float[0];
        } catch (OutOfMemoryError e) {
            return new float[0];
        }
    }
    
    public static int[] getIntArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            int[] values = new int[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Integer.parseInt(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Integer.parseInt(String)
            return new int[0];
        } catch (OutOfMemoryError e) {
            return new int[0];
        }
    }
    
    public static long[] getLongArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            long[] values = new long[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Long.parseLong(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Long.parseLong(String)
            return new long[0];
        } catch (OutOfMemoryError e) {
            return new long[0];
        }
    }
    
    public static short[] getShortArray(List<String> valuesList) {
        if (valuesList == null) return null;
        int valuesCount = valuesList.size();
        try {
            short[] values = new short[valuesCount];
            for (int i = 0; i < valuesCount; i++)
                values[i] = Short.parseShort(valuesList.get(i));
            return values;
        } catch (NumberFormatException e) { // Short.parseShort(String)
            return new short[0];
        } catch (OutOfMemoryError e) {
            return new short[0];
        }
    }
    
}
