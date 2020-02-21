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

/**
 * Collects a simple statistics
 */
public class SimpleStatistics extends BaseStatistics<Integer> {

    public SimpleStatistics(String text, int level) {
	super(text, level);
    }
    
    @Override
    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	//ps.printf("%s %8d min    %8d max    %8d avg\n", text, min, max, avg);	// NOI18N
        ps.printf("%s %8d min    %8d max    %8d avg    %8d cnt    %8d sum\n", text, min, max, avg, cnt, sum);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    @Override
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	printDistributionDetailed(ps);
    }
}
