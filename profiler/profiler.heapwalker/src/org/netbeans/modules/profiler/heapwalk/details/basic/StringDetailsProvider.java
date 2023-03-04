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
package org.netbeans.modules.profiler.heapwalk.details.basic;

import org.netbeans.modules.profiler.heapwalk.details.api.StringDecoder;
import java.util.List;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public final class StringDetailsProvider extends DetailsProvider.Basic {
    
    static final String STRING_MASK = "java.lang.String";                           // NOI18N
    static final String BUILDERS_MASK = "java.lang.AbstractStringBuilder+";         // NOI18N
    
    public StringDetailsProvider() {
        super(STRING_MASK, BUILDERS_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (STRING_MASK.equals(className)) {                                        // String
            byte coder = DetailsUtils.getByteFieldValue(instance, "coder", (byte) -1);     // NOI18N
            if (coder == -1) {
                int offset = DetailsUtils.getIntFieldValue(instance, "offset", 0);      // NOI18N
                int count = DetailsUtils.getIntFieldValue(instance, "count", -1);       // NOI18N
                return DetailsUtils.getPrimitiveArrayFieldString(instance, "value",     // NOI18N
                        offset, count, null,
                        "...");                // NOI18N
            } else {
                return getJDK9String(heap, instance, "value", coder, null, "...");          // NOI18N                
            }
        } else if (BUILDERS_MASK.equals(className)) {                               // AbstractStringBuilder+
            byte coder = DetailsUtils.getByteFieldValue(instance, "coder", (byte) -1);  // NOI18N
            if (coder == -1) {
                int count = DetailsUtils.getIntFieldValue(instance, "count", -1);       // NOI18N
                return DetailsUtils.getPrimitiveArrayFieldString(instance, "value",     // NOI18N
                        0, count, null,
                        "...");                // NOI18N
            } else {
                return getJDK9String(heap, instance, "value", coder, null, "...");          // NOI18N                
            }
        }
        return null;
    }
    
    public View getDetailsView(String className, Instance instance, Heap heap) {
        return new ArrayValueView(className, instance, heap);
    }
    
    private String getJDK9String(Heap heap, Instance instance, String field, byte coder, String separator, String trailer) {
        Object byteArray = instance.getValueOfField(field);
        if (byteArray instanceof PrimitiveArrayInstance) {
            List<String> values = ((PrimitiveArrayInstance) byteArray).getValues();
            if (values != null) {
                StringDecoder decoder = new StringDecoder(heap, coder, values);
                int valuesCount = decoder.getStringLength();
                int separatorLength = separator == null ? 0 : separator.length();
                int trailerLength = trailer == null ? 0 : trailer.length();
                int estimatedSize = Math.min(valuesCount * (1 + separatorLength), DetailsUtils.MAX_ARRAY_LENGTH + trailerLength);
                StringBuilder value = new StringBuilder(estimatedSize);
                int lastValue = valuesCount - 1;
                for (int i = 0; i <= lastValue; i++) {
                    value.append(decoder.getValueAt(i));
                    if (value.length() >= DetailsUtils.MAX_ARRAY_LENGTH) {
                        if (trailerLength > 0) {
                            value.append(trailer);
                        }
                        break;
                    }
                    if (separator != null && i < lastValue) {
                        value.append(separator);
                    }
                }
                return value.toString();
            }
        }
        return null;
    }
}
