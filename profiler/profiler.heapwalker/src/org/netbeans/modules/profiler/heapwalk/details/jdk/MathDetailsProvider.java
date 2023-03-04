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
package org.netbeans.modules.profiler.heapwalk.details.jdk;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public final class MathDetailsProvider extends DetailsProvider.Basic {
    
    private static final String BIG_INTEGRER_MASK = "java.math.BigInteger"; // NOI18N
    private static final String BIG_DECIMAL_MASK = "java.math.BigDecimal";  // NOI18N
    
    public MathDetailsProvider() {
        super(BIG_INTEGRER_MASK,BIG_DECIMAL_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (BIG_INTEGRER_MASK.equals(className)) {
            BigInteger bint = getBigInteger(instance);
            
            if (bint != null) {
                return bint.toString();
            }
        } else if (BIG_DECIMAL_MASK.equals(className)) {
            String val = DetailsUtils.getInstanceFieldString(instance, "stringCache", heap);   // NOI18N
            if (val == null) {
                int scale = DetailsUtils.getIntFieldValue(instance, "scale", 0);    // NOI18N
                long intCompact = DetailsUtils.getLongFieldValue(instance, "intCompact", Long.MIN_VALUE);   // NOI18N
                
                if (intCompact != Long.MIN_VALUE) {
                    return BigDecimal.valueOf(intCompact, scale).toString();
                } else {
                    Object bintInstace = instance.getValueOfField("intVal");    // NOI18N
                    if (bintInstace instanceof Instance) {
                        BigInteger bint = getBigInteger((Instance)bintInstace);
                        
                        if (bint != null) {
                            return new BigDecimal(bint, scale).toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    private BigInteger getBigInteger(final Instance instance) {
        int sig = DetailsUtils.getIntFieldValue(instance, "signum", Integer.MAX_VALUE);     // NOI18N
        int[] mag = DetailsUtils.getIntArray(DetailsUtils.getPrimitiveArrayFieldValues(instance, "mag"));   // NOI18N
        if (mag != null && sig != Integer.MAX_VALUE) {
            ByteBuffer buffer = ByteBuffer.allocate(mag.length * 4);
            IntBuffer intBuffer = buffer.asIntBuffer();
            intBuffer.put(mag);
            
            return new BigInteger(sig, buffer.array());
        }
        return null;
    }
    
}
