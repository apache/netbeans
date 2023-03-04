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

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public class JavacDetailsProvider extends DetailsProvider.Basic {
    
    private static final String SHAREDNAMETABLE_NAMEIMPL_MASK =
            "com.sun.tools.javac.util.SharedNameTable$NameImpl";                // NOI18N
    private static final String NAME_MASK =
            "com.sun.tools.javac.util.Name";                                    // NOI18N
    
    public JavacDetailsProvider() {
        super(SHAREDNAMETABLE_NAMEIMPL_MASK, NAME_MASK);
    }

    @Override
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (SHAREDNAMETABLE_NAMEIMPL_MASK.equals(className)) {
            return getName(instance, "length", "index", "table", "bytes");      // NOI18N
        } else if (NAME_MASK.equals(className)) {
            return getName(instance, "len", "index", "table", "names");         // NOI18N
        }
        return null;
    }

    private String getName(Instance instance, String lenField, String indexField, String tableField, String bytesField) {
        Integer length = (Integer) instance.getValueOfField(lenField);
        Integer index = (Integer) instance.getValueOfField(indexField);
        Instance table = (Instance) instance.getValueOfField(tableField);
        if (length != null && index != null && table != null) {
            PrimitiveArrayInstance bytes = (PrimitiveArrayInstance) table.getValueOfField(bytesField);
            List elements = bytes.getValues();
            byte[] data = new byte[length];
            for (int i = 0; i < length; i++) {
                String el = (String) elements.get(index+i);
                data[i] = Byte.valueOf(el).byteValue();
            }
            return new String(data, StandardCharsets.UTF_8);
        }
        return null;
    }
    
}
