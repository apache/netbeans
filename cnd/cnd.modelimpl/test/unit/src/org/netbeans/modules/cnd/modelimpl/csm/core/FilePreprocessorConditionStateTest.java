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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import static junit.framework.TestCase.assertEquals;
import org.junit.Test;

/**
 *
 */
public class FilePreprocessorConditionStateTest {
    @Test
    public void testCoverage() {
        int blocks[] = new int[] {
            10, 20,
            35, 40,
            100, 127,
            200, 201,
            250, 300
        };
        assertEquals(1000, FilePreprocessorConditionState.getCoverage(blocks, 0, 1000));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 10, 30));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 20, 30));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 5, 100));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 5, 127));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 15, 17));
        assertEquals(-1, FilePreprocessorConditionState.getCoverage(blocks, 30, 120));
        assertEquals(10, FilePreprocessorConditionState.getCoverage(blocks, 5, 25));
        assertEquals(93, FilePreprocessorConditionState.getCoverage(blocks, 25, 150));
        assertEquals(50, FilePreprocessorConditionState.getCoverage(blocks, 220, 320));
        assertEquals(100, FilePreprocessorConditionState.getCoverage(blocks, 310, 410));
        assertEquals(5, FilePreprocessorConditionState.getCoverage(blocks, 3, 8));
    }
}
