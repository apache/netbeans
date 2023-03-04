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
package org.netbeans.modules.profiler.heapwalk.details.jdk.image;

import java.util.List;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.image.FieldAccessor.InvalidFieldException;

// Convertors of instance values --------------------------------------------
/**
 * Reconstruct object from the heap instance.
 * @author Jan Taus
 */
abstract class InstanceBuilder<T> {

    public static final InstanceBuilder<String> STRING_BUILDER = new InstanceBuilder<String>(String.class) {
        @Override
        public String convert(FieldAccessor accessor, Instance instance) throws InvalidFieldException {
            return accessor.toString(instance);
        }
    };
    /**
     * Builds
     * <code>int[]</code> from {@link PrimitiveArrayInstance}
     */
    public static final InstanceBuilder<int[]> INT_ARRAY_BUILDER = new InstanceBuilder<int[]>(int[].class) {
        @Override
        public int[] convert(FieldAccessor accessor, Instance instance) throws InvalidFieldException {
            if (instance == null) {
                return null;
            }
            PrimitiveArrayInstance array = FieldAccessor.castValue(instance, PrimitiveArrayInstance.class);
            List<?> list = array.getValues();
            int[] result = new int[list.size()];
            for (int i = 0; i < result.length; i++) {
                try {
                    result[i] = Integer.parseInt((String) list.get(i));
                } catch (NumberFormatException e) {
                    throw new InvalidFieldException("invalid format of int at index %d: %s", i, list.get(i));
                }
            }
            return result;
        }
    };
    /**
     * Builds
     * <code>byte[]</code> from {@link PrimitiveArrayInstance}
     */
    public static final InstanceBuilder<byte[]> BYTE_ARRAY_BUILDER = new InstanceBuilder<byte[]>(byte[].class) {
        @Override
        public byte[] convert(FieldAccessor accessor, Instance instance) throws InvalidFieldException {
            if (instance == null) {
                return null;
            }
            PrimitiveArrayInstance array = FieldAccessor.castValue(instance, PrimitiveArrayInstance.class);
            List<?> list = array.getValues();
            byte[] result = new byte[list.size()];
            for (int i = 0; i < result.length; i++) {
                try {
                    result[i] = Byte.parseByte((String) list.get(i));
                } catch (NumberFormatException e) {
                    throw new InvalidFieldException("invalid format of byte at index %d: %s", i, list.get(i));
                }
            }
            return result;
        }
    };
    /**
     * Builds
     * <code>short[]</code> from {@link PrimitiveArrayInstance}
     */
    public static final InstanceBuilder<short[]> SHORT_ARRAY_BUILDER = new InstanceBuilder<short[]>(short[].class) {
        @Override
        public short[] convert(FieldAccessor accessor, Instance instance) throws InvalidFieldException {
            if (instance == null) {
                return null;
            }
            PrimitiveArrayInstance array = FieldAccessor.castValue(instance, PrimitiveArrayInstance.class);
            List<?> list = array.getValues();
            short[] result = new short[list.size()];
            for (int i = 0; i < result.length; i++) {
                try {
                    result[i] = Short.parseShort((String) list.get(i));
                } catch (NumberFormatException e) {
                    throw new InvalidFieldException("invalid format of short at index %d: %s", i, list.get(i));
                }
            }
            return result;
        }
    };
    /**
     * Builder which returns original instance.
     */
    public static final InstanceBuilder<Instance> IDENTITY_BUILDER = new InstanceBuilder<Instance>(Instance.class) {
        @Override
        public Instance convert(FieldAccessor accessor, Instance instance) throws InvalidFieldException {
            return instance;
        }
    };

    /**
     * Builds object in the field of the instance.
     */
    public static class ReferringInstanceBuilder<T> extends InstanceBuilder<T> {

        private final String[] path;

        public ReferringInstanceBuilder(Class<T> type, String... path) {
            super(type);
            this.path = path;
        }

        @Override
        public T convert(FieldAccessor fa, Instance instance) throws InvalidFieldException {
            for (int i = 0; i < path.length - 1 && instance != null; i++) {
                instance = fa.getInstance(instance, path[i], false);
            }
            if (instance == null) {
                return null;
            }
            return fa.build(instance, path[path.length - 1], getType(), false);
        }
    }
    private final Class<T> type;

    public InstanceBuilder(Class<T> type) {
        this.type = type;
    }

    /**
     * Return type of the created objects. Function used to access class from generic context (e.g. allocating arrays).
     */
    Class<T> getType() {
        return type;
    }

    /**
     * Reconstruct object from the instance.
     *
     * @throws InvalidFieldException if the reconstructions failed
     */
    public abstract T convert(FieldAccessor accessor, Instance instance) throws FieldAccessor.InvalidFieldException;
}
