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
 * The Benchmark measuring the difference between using public and
 * private constructor of the private inner class.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class InnerClassConstruct extends Benchmark {

    public InnerClassConstruct(String name) {
        super( name );
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    /**
     * Pour into the call stack and then create an object.
     * Used as a reference to divide the time between recursive decline
     * and Exception creation.
     */
    public void testCreatePrivate() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
	    new Priv();
        }
    }

    /**
     * Create an Exception deep in the call stack, filling its stack trace.
     */
    public void testCreatePublic() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
            new Publ();
        }
    }

    public static void main( String[] args ) {
	simpleRun( InnerClassConstruct.class );
    }

    private final class Priv {}
    
    private final class Publ {
	public Publ() {}
    }

}
