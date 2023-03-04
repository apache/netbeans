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

import java.util.ArrayList;
import java.util.List;

/** Default implementation of NbPerformanceTest with added methods to collect
 * measured performance data.
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class NbPerformanceTestCase extends NbTestCase implements NbPerformanceTest {


    /** Creates a new instance of NbPerformanceTestCase
     * @param name String test name
     */
    public NbPerformanceTestCase(String name) {
        super(name);
    }

    private List<NbPerformanceTest.PerformanceData> data = new ArrayList<NbPerformanceTest.PerformanceData>();
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        return data.toArray(new NbPerformanceTest.PerformanceData[0]);
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name and unit is not specified
     * @param value measured perofrmance value
     */    
    public void reportPerformance(long value) {
        reportPerformance(null, value, null, NbPerformanceTest.PerformanceData.NO_ORDER);
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     */    
    public void reportPerformance(long value, String unit) {
        reportPerformance(null, value, unit, NbPerformanceTest.PerformanceData.NO_ORDER);
    }

    /** method for storing and reporting measured performance value,
     * unit is not specified
     * @param name measured value name
     * @param value measured perofrmance value
     */    
    public void reportPerformance(String name, long value) {
        reportPerformance(name, value, null, NbPerformanceTest.PerformanceData.NO_ORDER);
    }
    
    /** method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
    */
    public void reportPerformance(String name, long value, String unit, int runOrder) {
        reportPerformance(name, value, unit, runOrder, PerformanceData.NO_THRESHOLD);
    }
    
    /** method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     * @param threshold - measure threshold
     */    
    public void reportPerformance(String name, long value, String unit, int runOrder, long threshold) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name==null? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = runOrder;
        d.threshold = threshold;
        data.add(d);
    }
    
    
    
}
