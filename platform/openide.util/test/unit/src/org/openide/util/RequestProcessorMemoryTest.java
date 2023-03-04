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
package org.openide.util;

import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

public class RequestProcessorMemoryTest extends NbTestCase {
    public RequestProcessorMemoryTest(String name) {
        super(name);
    }
    
    public void testRunOutOfMemory() throws Exception {
        RequestProcessor rp = new RequestProcessor("testRunOutOfMemory", 1, false, false);
        final int[] cnt = { 0 };
        RequestProcessor.Task task = rp.create(new Runnable() {
            @Override
            public void run() {
                cnt[0]++;
            }
        });
        for (int i = 0; i < 10000000; i++) {
            task.schedule(2000000);
        }
        assertEquals("Still zero", 0, cnt[0]);
    }
    
}
