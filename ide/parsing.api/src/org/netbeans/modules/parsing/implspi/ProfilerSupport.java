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

package org.netbeans.modules.parsing.implspi;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Semi-private SPI for self-profiling of the parsing system.
 *
 * @author sdedic
 * @since 9.2
 */
public abstract class ProfilerSupport {
    /**
     * Starts the profiling. The support will collect data until {@link #cancel} or {@link #stopAndSnapshot}
     * is called.
     */
    public abstract void start();
    
    /**
     * Cancels the profiling and discards data already collected.
     */
    public abstract void cancel();
    
    /**
     * Stops the profiling and flushes the data.
     * 
     * @param dos output stream to store the collected data.
     */
    public abstract void stopAndSnapshot(DataOutputStream dos) throws IOException;
    
    public interface Factory {
        public ProfilerSupport  create(String id);
    }
}
