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
package org.openide.util.enum;

import java.util.Enumeration;
import org.netbeans.performance.MultiInstanceIntArgBenchmark;

public abstract class EnumBenchmark extends MultiInstanceIntArgBenchmark {

    public EnumBenchmark( String name ) {
	super( name, new Integer[] { i(16), i(100), i(1000) } );
    }

    public void testEnumerationBlind() {
	int count = getIterationCount();
	int arg = getIntArg();
	
	while( count-- > 0 ) {
	    Enumeration e = (Enumeration)instances[count];
	    for( int i=0; i<arg; i++ ) e.nextElement();
	}
    }

    public void testEnumerationTesting() {
	int count = getIterationCount();
	
	while( count-- > 0 ) {
	    Enumeration e = (Enumeration)instances[count];
	    while( e.hasMoreElements() ) e.nextElement();
	}
    }    
}
