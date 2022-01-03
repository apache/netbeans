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

package org.netbeans.modules.cnd.repository.testbench;

import java.io.PrintStream;
import java.util.*;

/**
 * Base class for collecting simple statistics
 */
public class BaseStatistics<K extends Comparable<?>> {

    protected int min = 0;
    protected int max = 0;
    protected int cnt = 0;
    
    protected int sum;
    
    protected String text;
    
    protected final Map<K, Integer> values = new TreeMap<K, Integer>();
    
    protected int level;
    
    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_MINUMUN = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_MAXIMUM = 3;
    
    public BaseStatistics(String text, int level) {
	this.text = text;
	this.level = level;
    }    
    
    public void consume(K key, int value) {
	if( value > max ) {
	    max = value;
	}
	if( value < min ) {
	    min = value;
	}
	cnt++;
	sum += value;
	if( values != null ) {
	    Integer count = values.get(key);
	    values.put(key, Integer.valueOf((count == null) ? 1 : count.intValue() + 1));
	}
    }    

    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	ps.printf("%s %8d min    %8d max    %8d avg    %8d cnt    %8d sum\n", text, min, max, avg, cnt, sum);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    protected void printDistributionDetailed(PrintStream ps) {
        int maxKeyLen = 0;
        String alignment = "-"; // NOI18N
	for( Map.Entry<K, Integer> entry : values.entrySet() ) {
            K key = entry.getKey();
            int currKeyLen;
            if( key == null ) {
                currKeyLen = 4; // "null"
            } else if( key instanceof Number ) { // can't use K.class  :(
                maxKeyLen = 8;
                alignment = "";
                break;
            } else {
                currKeyLen = key.toString().length();
            }
            if( currKeyLen > maxKeyLen ) {
                maxKeyLen = currKeyLen;
            }
	}
        String format = "\t%" + alignment + maxKeyLen + "s %8d\n"; // NOI18N
	for( Map.Entry<K, Integer> entry : values.entrySet() ) {
	    ps.printf(format, entry.getKey(), entry.getValue());	// NOI18N
	}
    }
    
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	printDistributionDetailed(ps);
    }

    public void clear() {
        min = max = cnt = sum = 0;
        values.clear();
    }
    
}
