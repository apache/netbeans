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

package org.netbeans.modules.profiler.heapwalk.details.netbeans;

import java.util.AbstractList;
import java.util.List;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public class EditorDetailsProvider  extends DetailsProvider.Basic {
    private static final String CHAR_CONTENT = "org.netbeans.modules.editor.lib2.document.CharContent"; // NOI18N

    public EditorDetailsProvider() {
        super(CHAR_CONTENT);
    }

    @Override
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (CHAR_CONTENT.equals(className)) {
            int gapStart = DetailsUtils.getIntFieldValue(instance, "gapStart", -1);     // NOI18N
            int gapLength = DetailsUtils.getIntFieldValue(instance, "gapLength", -1);       // NOI18N
            PrimitiveArrayInstance buffer = (PrimitiveArrayInstance)instance.getValueOfField("buffer"); // NOI18N

            if (gapLength >= 0 && gapLength >= 0 && buffer != null) {
                CharArrayWithGap array = new CharArrayWithGap(buffer, gapStart, gapLength);

                return DetailsUtils.getPrimitiveArrayString(array, 0, array.getLength(), "", "...");    // NOI18N
            }
        }
        return null;
    }


    private class CharArrayWithGap implements PrimitiveArrayInstance {

        PrimitiveArrayInstance buffer;
        int gapStart;
        int gapLength;

        private CharArrayWithGap(PrimitiveArrayInstance buf, int start, int length) {
            buffer = buf;
            gapStart = start;
            gapLength = length;
        }

        @Override
        public int getLength() {
            return buffer.getLength() - gapLength;
        }

        @Override
        public List getValues() {
            final List origValues = buffer.getValues();

            return new AbstractList() {

                @Override
                public Object get(int index) {
                    return origValues.get(rawOffset(index));
                }

                @Override
                public int size() {
                    return getLength();
                }
            };
        }

        private int rawOffset(int index) {
            return (index < gapStart) ? index : (index + gapLength);
        }

        @Override
        public List getFieldValues() {
            throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
        }

        @Override
        public boolean isGCRoot() {
                throw new UnsupportedOperationException("Not supported yet.");  // NOI18N
        }

        @Override
        public long getInstanceId() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getInstanceNumber() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public JavaClass getJavaClass() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Instance getNearestGCRootPointer() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public long getReachableSize() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public List getReferences() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public long getRetainedSize() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public long getSize() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public List getStaticFieldValues() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Object getValueOfField(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
}
