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

package org.netbeans.modules.parsing.lucene.support;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class LMListenerTest extends NbTestCase {

    private static final int _10K = 10 * 1024;


    private List<byte []> refs = new LinkedList<>();

    public LMListenerTest (final String name) {
        super (name);
    }


    /**
     * Checks if the LMListener detects low memory
     * and if it is not expensive to intensively call it.
     */
    public void testListnener () {
        final LowMemoryWatcher l = LowMemoryWatcher.getInstance();
        long ct = 0;
        for (int i=0; i<100000; i++) {
            long st = System.currentTimeMillis();
            boolean isLM = l.isLowMemory();
            long et = System.currentTimeMillis();
            ct+=et-st;
            if (isLM) {
                refs.clear();
            }
            byte[] data = new byte[_10K];
            refs.add(data);
        }
        assertTrue(ct<1000);
        
    }

}
