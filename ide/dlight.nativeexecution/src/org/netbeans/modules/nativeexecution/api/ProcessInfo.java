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
package org.netbeans.modules.nativeexecution.api;

import java.util.concurrent.TimeUnit;

/**
 * This interface is supposed to contain additional information about the process
 * also for now it is supposed that this information is created once and doesn't
 * changed... (?)
 * I.e. for now, if it is created - all calls to getters are fast.
 *
 * @author ak119685
 */
public interface ProcessInfo {

    /**
     * Currently this method returns the timestamp of process creation.
     * This is NOT an astronomic timestamp. It is some time offset, starting
     * from some point... (like System.nanoTime())
     *
     * But it is guaranteed that timer starting point is the same for different
     * processes on the same ExecutionEnvironment
     *
     * @param unit - returned timestamp units.
     * @return timestamp of process creation
     */
    long getCreationTimestamp(TimeUnit unit);
}
