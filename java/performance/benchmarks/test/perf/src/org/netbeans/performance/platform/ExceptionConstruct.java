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

package org.netbeans.performance.platform;

import org.netbeans.performance.Benchmark;

/**
 * The Benchmark measuring how long would it take to construct an instance
 * of Exception. Measured bacause the Exception contains native-filled
 * structure describing the shape of the thread stack in the moment
 * of constructing, which depends on stack depth in the time of constructing.
 * Uses a set of Integer arguments to select the call stack depth.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ExceptionConstruct extends Benchmark {

    public ExceptionConstruct(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(5), new Integer(10),
            new Integer(100), new Integer(1000 )
        });
    }

    private static final Object createObj( int depth ) {
	if( depth == 0 ) return new Object();
	return createObj( depth-1 );
    }

    /**
     * Pour into the call stack and then create an object.
     * Used as a reference to divide the time between recursive decline
     * and Exception creation.
     */
    public void testCreateObjectDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            createObj( magnitude );
        }
    }

    private static final Object createExc( int depth ) {
	if( depth == 0 ) return new Exception();
	return createExc( depth-1 );
    }

    
    /**
     * Create an Exception deep in the call stack, filling its stack trace.
     */
    public void testCreateExceptionDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            createExc( magnitude );
        }
    }

    private static final Object throwExc( int depth ) throws Exception {
	if( depth == 0 ) throw new Exception();
	return createExc( depth-1 );
    }

    
    /**
     * Create an Exception deep in the call stack and let it bubble up
     * throughout the whole stack.
     */
    public void testThrowExceptionDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            try {
                createExc( magnitude );
            } catch( Exception e ) {}
        }
    }

    
    public static void main( String[] args ) {
	simpleRun( ExceptionConstruct.class );
    }

}
