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

package org.netbeans.modules.masterfs.filebasedfs;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;

/**
 *
 * @author Radek Matous
 */
public final class Statistics {
    /** Creates a new instance of Statistics */

    public static final Statistics.TimeConsumer REFRESH_FOLDER =
            new Statistics.TimeConsumer("Folder refresh");//NOI18N
    public static final Statistics.TimeConsumer REFRESH_FILE =
            new Statistics.TimeConsumer("File refresh");//NOI18N
    public static final Statistics.TimeConsumer REFRESH_FS = 
            new Statistics.TimeConsumer("FileSystem refresh");//NOI18N    
    public static final Statistics.TimeConsumer LISTENERS_CALLS = 
            new Statistics.TimeConsumer("Invocation of FileChangeListeners");//NOI18N    
    
    
    private Statistics() {}
    
    
    public static Statistics.StopWatch getStopWatch(Statistics.TimeConsumer consumer) {
        return new Statistics.StopWatch(consumer);
    }
    
    public static int fileSystems() {
        return FileObjectFactory.getFactoriesSize();
    }
    
    public static int fileNamings() {
        return NamingFactory.getSize();
    }
    
    public static int fileObjects() {
        int retVal = 0;
        for (FileObjectFactory fbs : FileObjectFactory.getInstances()) {
            retVal += fileObjectsPerFileSystem(fbs);
        }
        
        return retVal;
    }
    
    public static int fileObjectsPerFileSystem(FileObjectFactory factory) {
        return factory.getSize();
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

        @Override
        public String toString() {
            return description + ": " + numberOfCalls + " calls in " + elapsedTime + "ms";
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

