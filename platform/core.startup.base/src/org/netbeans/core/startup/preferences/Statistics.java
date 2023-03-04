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

package org.netbeans.core.startup.preferences;

/**
 *
 * @author Radek Matous
 */
public final class Statistics {
    /** Creates a new instance of Statistics */
    
    public static final Statistics.TimeConsumer FLUSH =
            new Statistics.TimeConsumer("Flush");//NOI18N
    public static final Statistics.TimeConsumer LOAD =
            new Statistics.TimeConsumer("Load");//NOI18N
    public static final Statistics.TimeConsumer REMOVE_NODE =
            new Statistics.TimeConsumer("Remove node");//NOI18N
    public static final Statistics.TimeConsumer CHILDREN_NAMES =
            new Statistics.TimeConsumer("Children names");//NOI18N
    
    
    private Statistics() {}
    
    
    public static Statistics.StopWatch getStopWatch(Statistics.TimeConsumer consumer) {
        return new Statistics.StopWatch(consumer);
    }

    public static Statistics.StopWatch getStopWatch(Statistics.TimeConsumer consumer, boolean start) {
        Statistics.StopWatch retval = new Statistics.StopWatch(consumer);
        if (start) {
            retval.start();
        }
        return retval;
    }
    
    public static final class TimeConsumer {
        private int elapsedTime;
        private int numberOfCalls;
        private final String description;
        
        private TimeConsumer(final String description) {
            this.description = description;
        }
        
        public int getConsumedTime() {
            return elapsedTime;
        }
        
        public int getNumberOfCalls() {
            return numberOfCalls;
        }        
        
        public void reset() {
    	    elapsedTime = 0;
            numberOfCalls = 0;
        }

        public @Override String toString() {
            return description + ": " + numberOfCalls + " calls in " + elapsedTime + "ms";//NOI18N
        }
		
        private void incrementNumerOfCalls() {
            numberOfCalls++;
            
        }
    }
    
    public static final class StopWatch {
        private long startTime = 0;
        private final Statistics.TimeConsumer activity;
        
        
        /** Creates a new instance of ElapsedTime */
        private StopWatch(Statistics.TimeConsumer activity) {
            this.activity = activity;
        }
        
        
        public void start() {
            startTime = System.currentTimeMillis();
        }
        
        public void stop() {
            assert startTime != 0;
            activity.elapsedTime += (System.currentTimeMillis() - startTime);
            activity.incrementNumerOfCalls();
            startTime = 0;
        }
    }
}

