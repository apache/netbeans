/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelimpl.memory;

/**
 * Allows to register listener that are notified in the case of low memory.
 * Singleton.
 */
public abstract class LowMemoryNotifier {
    
    /** proetcted constructor to prevent external creation */
    protected LowMemoryNotifier() {
    }
    
    public static LowMemoryNotifier instance() {
        return instance;
    }
    
    /** Registers low memory lstener */
    public abstract void addListener(LowMemoryListener listener);
    
    /** Unregisters low memory lstener */
    public abstract void removeListener(LowMemoryListener listener);
    
    /** 
     * Sets the memory usage percentage threshold to the given value
     * @param percentage the new threshold value in percents
     */
    public abstract void setThresholdPercentage(double percentage);
    
    
    private static final LowMemoryNotifier instance = new LowMemoryNotifierImpl();
}
