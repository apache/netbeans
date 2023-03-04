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
package org.netbeans.modules.profiler.nbimpl.javac;

import org.netbeans.api.java.source.Task;

/**
 * A scan sensitive version of the java source {@linkplain Task}.
 * Allows the subclasses to define whether they require up-to-date data or
 * whether they would be satisfied with the last good knows one.
 * 
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public abstract class ScanSensitiveTask<P> implements Task<P> {
    private boolean uptodate;
    
    /**
     * Creates an instance not requiring up-to-date data
     */
    public ScanSensitiveTask() {
        this(false);
    }
    
    /**
     * 
     * @param uptodate TRUE = requires up-to-date data (eg. needs to wait for the scanning to finish)
     */
    public ScanSensitiveTask(boolean uptodate) {
        this.uptodate = uptodate;
    }
    
    /**
     * Used for indication of whether the task was able to obtain required data or not.
     * 
     * @return <b>TRUE</b> if the task got the required data; <b>FALSE</b> otherwise
     */
    public boolean shouldRetry() {
        return false;
    }
    
    /**
     * Indicates the requirements regarding the up-to-dateness of the accessed data
     * @return <b>TRUE</b> if the task needs up-to-date data (eg. needs to wait for the scanning to finish); <b>FALSE</b> otherwise
     */
    public final boolean requiresUpToDate() {
        return this.uptodate;
    }
}
