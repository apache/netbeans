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
package org.netbeans.modules.cnd.repository.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;

/**
 * A list of all client UnitDescriptors per Storage.
 *
 * clientUnitDescriptor --- clientShortUnitID
 *
 */
/* package */ final class UnitDescriptorsDictionary {

    private final Map<UnitDescriptor, Integer> map = new HashMap<UnitDescriptor, Integer>();
    private final AtomicInteger counter = new AtomicInteger(7);
    private final Object lock = new Object();

    /**
     *
     * @param clientUnitDescriptor
     * @return clientShortUnitID
     */
    public int getUnitID(UnitDescriptor clientUnitDescriptor) {
        synchronized (lock) {
            Integer result = map.get(clientUnitDescriptor);
            if (result == null) {
                result = counter.getAndIncrement();
                map.put(clientUnitDescriptor, result);
            }

            return result;
        }
    }

    public Integer remove(final Integer clientShortUnitID) {
        synchronized (lock) {
            return map.remove(getUnitDescriptor(clientShortUnitID));
        }
    }

    /**
     *
     * @param clientShortUnitID
     * @return
     */
    UnitDescriptor getUnitDescriptor(Integer clientShortUnitID) {
        synchronized (lock) {
            for (Map.Entry<UnitDescriptor, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(clientShortUnitID)) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    boolean contains(UnitDescriptor clientUnitDescriptor) {
        synchronized (lock) {        
            return map.containsKey(clientUnitDescriptor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[clientUnitDescriptor <-> clientShortUnitID]\n"); // NOI18N
        synchronized (lock) {        
            for (Map.Entry<UnitDescriptor, Integer> entry : map.entrySet()) {
                sb.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n"); // NOI18N
            }
            return sb.toString();
        }
    }

    Collection<Integer> getUnitIDs() {
        synchronized (lock) { 
            return map.values();
        }
    }
}
