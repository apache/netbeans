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
package org.netbeans.lib.v8debug.vars;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import org.netbeans.lib.v8debug.PropertyLong;

/**
 *
 * @author Martin Entlicher
 */
public class V8Object extends V8Value {
    
    private final String className;
    private final PropertyLong constructorFunctionHandle;
    private final PropertyLong protoObjectHandle;
    private final PropertyLong prototypeObjectHandle;
    private final Map<String, Property> properties;
    private final Array array;
    
    public V8Object(long handle, V8Value.Type type, String className,
                    PropertyLong constructorFunctionHandle,
                    PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                    Map<String, Property> properties, Array array, String text) {
        super(handle, type, text);
        this.className = className;
        this.constructorFunctionHandle = constructorFunctionHandle;
        this.protoObjectHandle = protoObjectHandle;
        this.prototypeObjectHandle = prototypeObjectHandle;
        this.properties = properties;
        this.array = array;
    }
    
    protected V8Object(long handle, V8Value.Type type, String className,
                       PropertyLong constructorFunctionHandle,
                       PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                       Map<String, Property> properties, String text) {
        this(handle, type, className, constructorFunctionHandle,
             protoObjectHandle, prototypeObjectHandle, properties, null, text);
    }
    
    public String getClassName() {
        return className;
    }

    public PropertyLong getConstructorFunctionHandle() {
        return constructorFunctionHandle;
    }

    public PropertyLong getProtoObjectHandle() {
        return protoObjectHandle;
    }

    public PropertyLong getPrototypeObjectHandle() {
        return prototypeObjectHandle;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }
    
    public Array getArray() {
        return array;
    }
    
    public static final class Property {
        
        public static enum Type {
            Normal,
            Field,
            Constant,
            Callbacks,
            Handler,
            Interceptor,
            Transition,
            Nonexistent
        }
        
        public static final int ATTR_NONE = 0;
        public static final int ATTR_READ_ONLY = 1;
        public static final int ATTR_DONT_ENUM = 2;
        public static final int ATTR_DONT_DELETE = 4;
        public static final int ATTR_SEALED = ATTR_DONT_DELETE;
        public static final int ATTR_FROZEN = ATTR_SEALED | ATTR_READ_ONLY;
        public static final int ATTR_STRING = 8;
        public static final int ATTR_SYMBOLIC = 16;
        public static final int ATTR_PRIVATE_SYMBOL = 32;
        public static final int ATTR_DONT_SHOW = ATTR_DONT_ENUM | ATTR_SYMBOLIC | ATTR_PRIVATE_SYMBOL;
        public static final int ATTR_ABSENT = 64;
        
        private final String name;
        private final Type type;
        private final int attributes;
        private final long reference;
        
        public Property(String name, Type type, int attributes, long reference) {
            this.name = name;
            this.type = type;
            this.attributes = attributes;
            this.reference = reference;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public int getAttributes() {
            return attributes;
        }

        public long getReference() {
            return reference;
        }
        
    }
    
    public static interface Array {
        
        long getReferenceAt(long index) throws NoSuchElementException;
        
        IndexIterator getIndexIterator();
        
        boolean isContiguous();
        
        long[] getContiguousReferences() throws UnsupportedOperationException;
        
        long getLength();
    }
    
    public static interface IndexIterator {
        
        boolean hasNextIndex();
        
        long nextIndex() throws NoSuchElementException;
    }
    
    public static final class DefaultArray implements Array {
        
        private long[] references;
        private long[] indexes;
        private int length;
        
        public DefaultArray(long[] references) {
            this.references = references;
            this.indexes = null;
            this.length = references.length;
        }
        
        public DefaultArray() {
            this.references = new long[0];
            this.indexes = null;
            this.length = 0;
        }
        
        public void putReferenceAt(long index, long reference) {
            if (indexes == null) {
                // we're contiguous so far
                if (index < length) {
                    references[(int) index] = reference;
                } else if (index == length) {
                    if (references.length <= index) {
                        int newLength = getNewLength();
                        references = Arrays.copyOf(references, newLength);
                    }
                    references[length++] = reference;
                } else {
                    // switch to non-contiguous
                    int newLength = references.length;
                    if (!(newLength > length)) {
                        newLength = getNewLength();
                        references = Arrays.copyOf(references, newLength);
                    }
                    indexes = new long[newLength];
                    for (int i = 0; i < length; i++) {
                        indexes[i] = i;
                    }
                    indexes[length] = index;
                    references[length] = reference;
                    length++;
                }
            } else {
                if (references.length <= length) {
                    int newLength = getNewLength();
                    references = Arrays.copyOf(references, newLength);
                    indexes = Arrays.copyOf(indexes, newLength);
                }
                indexes[length] = index;
                references[length] = reference;
                length++;
            }
        }
        
        private int getNewLength() {
            int newLength = length + (length >> 1);
            if (newLength < 10) {
                newLength = 10;
            }
            if (!(newLength > length)) {
                // overflow
                newLength = Integer.MAX_VALUE - 8;
                if (!(newLength > length)) {
                    if (length == Integer.MAX_VALUE) {
                        throw new OutOfMemoryError("Unable to allocate an array longer than "+Integer.MAX_VALUE+" bytes.");
                    }
                    newLength = Integer.MAX_VALUE;
                }
            }
            return newLength;
        }
        
        @Override
        public long getReferenceAt(long index) throws NoSuchElementException {
            if (indexes == null) {
                if (index >= length) {
                    throw new NoSuchElementException("No reference at "+index+", array length is "+length);
                }
                return references[(int) index];
            } else {
                int pos = getPositionOf(index);
                if (pos < 0) {
                    throw new NoSuchElementException("No reference at "+index);
                }
                return references[pos];
            }
        }
        
        private int getPositionOf(long index) {
            int p1 = 0;
            int p2 = length - 1;
            while (p1 <= p2) {
                int p = p1 + (p2 - p1)/2;
                long pi = indexes[p];
                if (pi == index) {
                    return p;
                }
                if (p1 == p2) {
                    return -1;
                }
                if (pi < index) {
                    if (p1 == p) {
                        p1++;
                    } else {
                        p1 = p;
                    }
                } else {
                    p2 = p;
                }
            }
            return -1;
        }

        @Override
        public IndexIterator getIndexIterator() {
            return new DefaultIndexIterator();
        }

        @Override
        public boolean isContiguous() {
            return indexes == null;
        }

        @Override
        public long[] getContiguousReferences() throws UnsupportedOperationException {
            if (indexes == null) {
                if (references.length > length) {
                    // trim
                    references = Arrays.copyOf(references, length);
                }
                return references;
            } else {
                throw new UnsupportedOperationException("The array is not contiguous.");
            }
        }

        @Override
        public long getLength() {
            if (indexes == null) {
                return length;
            } else {
                if (length == 0) {
                    return 0;
                } else {
                    long maxIndex = indexes[length-1];
                    return maxIndex + 1;
                }
            }
        }
        
        private final class DefaultIndexIterator implements IndexIterator {
            
            private int pos = 0;

            @Override
            public boolean hasNextIndex() {
                return pos < length;
            }

            @Override
            public long nextIndex() throws NoSuchElementException {
                if (pos >= length) {
                    throw new NoSuchElementException("Length = "+length);
                }
                if (indexes == null) {
                    return pos++;
                } else {
                    return indexes[pos++];
                }
            }
        
        }
    }
    
}
