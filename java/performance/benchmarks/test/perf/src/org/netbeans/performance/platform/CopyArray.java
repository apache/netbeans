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
 * Benchmark measuring how efficiently is the System.arraycopy implemented,
 * how big overhead does it have on small arrays.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class CopyArray extends Benchmark {

    public CopyArray(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(5), new Integer(10),
            new Integer(100), new Integer(1000)
        });
    }

    private Object[] src, dest;
    private byte[] src_b, dest_b;

    private String[] dest_s;
    private Object[] src_s;

    protected void setUp() {
        int magnitude = ((Integer)getArgument()).intValue();
	src = new Object[magnitude];
	dest = new Object[magnitude];
	src_b = new byte[magnitude];
	dest_b = new byte[magnitude];

	src_s = new Object[magnitude];
	dest_s = new String[magnitude];
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }


    /**
     */
    public void testCopyNativeObjects() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src, 0, dest, 0, magnitude);
        }
    }

    /**
     */
    public void testCopyNativeMixed() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src_s, 0, dest_s, 0, magnitude);
        }
    }
    
    /**
     */
    public void testCopyNativeBytes() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src_b, 0, dest_b, 0, magnitude);
        }
    }


    /**
     */
    public void testCopyLoopObjects() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest[i] = src[i];
        }
    }

    /**
     */
    public void testCopyLoopBytes() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest_b[i] = src_b[i];
        }
    }

    /**
     */
    public void testCopyLoopMixed() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest_s[i] = (String) src_s[i];
        }
    }

    public static void main( String[] args ) {
	simpleRun( CopyArray.class );
    }

}
