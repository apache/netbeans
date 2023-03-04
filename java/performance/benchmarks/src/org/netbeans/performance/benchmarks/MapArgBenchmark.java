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
package org.netbeans.performance.benchmarks;

import java.util.Map;
import java.util.HashMap;


/**
 * Benchmark which arguments are always Maps
 */
public class MapArgBenchmark extends Benchmark {

    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */
    public MapArgBenchmark(String name) {
        super(name);
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public MapArgBenchmark(String name, Object[] args) {
        super(name, args);
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        return new HashMap();
    }
    
    /** Sets argument of mab to this MapArgBenchmark */
    public final void setParent(MapArgBenchmark mab) {
        setArgument(mab.getArgument());
    }
    
    /** @return an int value bound to key */
    protected final int getIntValue(String key) {
        Map param = (Map) getArgument();
        if (param == null) {
            return 0;
        }
        Integer i = (Integer) param.get(key);
        if (i == null) {
            return 0;
        }
        return i.intValue();
    }
}
