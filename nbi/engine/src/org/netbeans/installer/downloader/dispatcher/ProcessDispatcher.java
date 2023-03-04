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

package org.netbeans.installer.downloader.dispatcher;

public interface ProcessDispatcher {
    /**
     * entry point to add process. This don't give any information when it will be 
     * processed return false if process discarded. default impl - always true.
     */
    boolean schedule(Process process);

    /**
     * Force process termination. Deprecated since in any case of implementation 
     * it will deal with thread.stop() which is deprecated
     */
    @Deprecated
    void terminate(Process process);

    void setLoadFactor(LoadFactor factor);

    /**
     * loadFactor allow managing system resources usages. By default Full - means 
     * no internal managment In default impl loadFactor impact on frequency of 
     * blank quantums.
     */
    LoadFactor loadFactor();

    boolean isActive();

    int activeCount();

    int waitingCount();

    void start();

    /**
     * when dispatcher stops it terminate all running processes and also clear 
     * waiting queue.
     */
    void stop();
}
