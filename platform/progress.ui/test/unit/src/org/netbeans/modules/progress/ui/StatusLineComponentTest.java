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

package org.netbeans.modules.progress.ui;

import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class StatusLineComponentTest extends TestCase {
    
    public StatusLineComponentTest(String testName) {
        super(testName);
    }
    
    public void testGetBarString() {
        long estimatedCompletion = -1;

        double percentage = 0.0;
        String result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("0%", result);
        
        percentage = 0.49;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("0%", result);
        
        percentage = 1.0;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("1%", result);
        
        percentage = 50.0;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("50%", result);
        
        percentage = 99.33;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("99%", result);

        percentage = 99.51;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("100%", result);

        percentage = 100.1;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("100%", result);
    }
}
