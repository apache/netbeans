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
package org.netbeans.modules.profiler.heapwalk.details.basic;

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
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public final class StringDetailsProvider extends DetailsProvider.Basic {
    // caching two fields
    private int hiShift = -1;
    private int loShift = -1;
 
    static final String STRING_MASK = "java.lang.String";                           // NOI18N
    static final String BUILDERS_MASK = "java.lang.AbstractStringBuilder+";         // NOI18N

    public StringDetailsProvider() {
        super(STRING_MASK, BUILDERS_MASK);
    }

    @Override
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
    
    @Override
    public View getDetailsView(String className, Instance instance, Heap heap) {
        return new ArrayValueView(className, instance, heap);
    }
    
    private String getJDK9String(Heap heap, Instance instance, String field, byte coder, String separator, String trailer) {
        Object byteArray = instance.getValueOfField(field);
        if (byteArray instanceof PrimitiveArrayInstance) {
            hiShift = -1;
            loShift = -1;
            List<String> values = ((PrimitiveArrayInstance) byteArray).getValues();
            if (values != null) {
                int valuesCount = switch (coder) {
                    case 1 -> values.size() / 2;
                    default -> values.size();
                };
                int separatorLength = separator == null ? 0 : separator.length();
                int trailerLength = trailer == null ? 0 : trailer.length();
                int estimatedSize = Math.min(valuesCount * (1 + separatorLength), DetailsUtils.MAX_ARRAY_LENGTH + trailerLength);
                StringBuilder value = new StringBuilder(estimatedSize);
                int lastValue = valuesCount - 1;
                for (int i = 0; i <= lastValue; i++) {
                    if (value.length() >= DetailsUtils.MAX_ARRAY_LENGTH) {
                        if (trailerLength > 0) {
                            value.append(trailer);
                        }
                        break;
                    }
                    value.append(switch (coder) {
                        case 0 -> {
                            byte ascii = Byte.parseByte(values.get(i));
                            yield Character.valueOf((char) (ascii & 0xff)).toString();
                        }
                        case 1 -> {
                            int hi = Byte.parseByte(values.get(i * 2)) & 0xff;
                            int lo = Byte.parseByte(values.get(i * 2 + 1)) & 0xff;
                            int both = hi << hiLoShift(true, heap) | lo << hiLoShift(false, heap);
                            yield Character.valueOf((char) both).toString();
                        }
                        default -> values.get(i);
                    });
                    if (separator != null && i < lastValue) {
                        value.append(separator);
                    }
                }
                return value.toString();
            }
        }
        return null;
    }
    
    private int hiLoShift(boolean hi, Heap heap) {
        if (hiShift == -1) {
            JavaClass utf16Class = heap.getJavaClassByName("java.lang.StringUTF16"); // NOI18N
            hiShift = (Integer) utf16Class.getValueOfStaticField("HI_BYTE_SHIFT"); // NOI18N
            loShift = (Integer) utf16Class.getValueOfStaticField("LO_BYTE_SHIFT"); // NOI18N
        }
        return hi ? hiShift : loShift;
    }
}
