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
 * Benchmark measuring how long it takes to set the name of the Thread.
 * Yarda's concenr was to not slow down the request processor by
 * pooling threads and changing the thread's name frequently.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class SetThreadName extends Benchmark {

    public SetThreadName(String name) {
        super( name );
	t.start();
	t2.start();
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    Thread t = new Thread() {
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
    };

    static class Thread2 extends Thread {
	int prio;
	
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
	
	public void setPrio(int p) {
	    if (prio == p) return;
	    setPriority(p);
	    prio = p;
	}
    }

    Thread2 t2 = new Thread2();
    

    /**
     */
    public void testSetThreadName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #" + count);
        }
    }

    /**
     */
    public void testSetFixedName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #A");
	    t.setName("Thread #B");
        }
    }

    /**
     */
    public void testCreateName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    String s = "Thread #" + count;
        }
    }

    /**
     */
    public void testSetPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(4+(count%3));
        }
    }

    /**
     */
    public void testSetFixedPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(5);
        }
    }

    /**
     */
    public void testSetSimilarPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t2.setPrio(4+((count/100)%3));
        }
    }

    
    public static void main( String[] args ) {
	simpleRun( SetThreadName.class );
    }

}
