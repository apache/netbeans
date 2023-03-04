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

/**
 * Base class for benchmarks that need a separate instance for every
 * iteration and need to have them prepared before the actual test.
 *
 * The benchmark based on this class needs only to implement method
 * createInstance, and implement test methods that will use array
 * of created instances, named instances. It can also override
 * preSetUp method to do some per-run initialization. It is called
 * at the very beginning of setUp().
 *
 * Example:
 * <PRE>
 * class ListTest extends MultiInstanceBenchmark {
 *
 *     public ListTest( String name ) {
 *         super( name, new Object[] {
 *             new Integer(10), new Integer(100), new Integer(1000)
 *         }
 *     }
 *
 *     protected Object createInstance() {
 *         return new ArrayList();
 *     }
 *
 *     public void testAppend() {
 *         int count = getIterationsCount();
 *         int arg = ((Integer)getArgument()).intValue();
 *
 *         while( count-- > 0 ) {
 *             for( int i=0; i<arg; i++ ) {
 *                 ((List)instances[count]).add( null );
 *             }
 *         }
 *     }
 * }
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public abstract class MultiInstanceBenchmark extends Benchmark {
    
    public MultiInstanceBenchmark( String name ) {
	super( name );
    }

    public MultiInstanceBenchmark( String name, Object[] args ) {
	super( name, args );
    }
    

    protected Object[] instances;

    protected void setUp() throws Exception {
	preSetUp();
	
	int iters = getIterationCount();
	
	instances = new Object[iters];
	for( int i=0; i<iters; i++ ) {
	    instances[i] = createInstance();
	}
    }

    protected void preSetUp() {
    }
    
    protected abstract Object createInstance();

    protected void tearDown() throws Exception {
	instances = null;
    }
    
}
