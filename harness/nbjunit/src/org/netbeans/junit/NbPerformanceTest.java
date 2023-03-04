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
package org.netbeans.junit;

import junit.framework.Test;

/** Interface extending JUnit test to store measured performance data
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public interface NbPerformanceTest extends NbTest {

    /** Helper class holding one measured performance value */
    public static class PerformanceData extends Object {
        // no measue order defined
        public static final int NO_ORDER = 0;
        // no threshold defined
        public static final long NO_THRESHOLD = 0;
        /** performance value name */        
        public String name;
        /** easured performance value */        
        public long value;
        /** performance value unit */        
        public String unit;
        /** run order - for same performance name, which run of the test is it **/
        public int runOrder;
        /** threshold for measured data **/
        public long threshold;
    }
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public PerformanceData[] getPerformanceData();
    
}
