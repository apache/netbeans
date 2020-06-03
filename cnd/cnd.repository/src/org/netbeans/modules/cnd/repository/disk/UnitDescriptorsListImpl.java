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

package org.netbeans.modules.cnd.repository.disk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.UnitDescriptorsList;

/**
 *
 */
public final class UnitDescriptorsListImpl implements UnitDescriptorsList{

    private final Map<UnitDescriptor, Integer> map = new HashMap<UnitDescriptor, Integer>();
    private final Object lock = new Object();
    private final AtomicInteger counter = new AtomicInteger(0);

    /*package*/ UnitDescriptorsListImpl() {
    }

    void setMaxValue(int max) {
       counter.set(max);
    }
    
    int getMaxValue() {
        return counter.get();
    }

    /*package*/ int registerNewUnitDescriptor(UnitDescriptor unitDescriptor) {
        synchronized (lock) {
            Integer result = map.get(unitDescriptor);
            if (result == null) {
                result = counter.getAndIncrement();
                map.put(unitDescriptor, result);
            }
            return result;
        }
    }

    /*package*/ void addUnitDescriptor(int layerUniID, UnitDescriptor unitDescriptor) {
        synchronized (lock) {
            map.put(unitDescriptor, layerUniID);
        }
    }


    /*package*/ Integer remove(final Integer layerUniID) {
        synchronized (lock) {
            return map.remove(getUnitDescriptor(layerUniID));
        }
    }

    /*package*/ void clear() {
        synchronized (lock) {
            map.clear();
        }
    }

    /*package*/ Map<UnitDescriptor, Integer> getMap() {
        synchronized (lock) {
            return Collections.unmodifiableMap(map);
        }
    }

    /**
     *
     * @param layerUniID
     * @return
     */
    @Override
    public UnitDescriptor getUnitDescriptor(Integer layerUniID) {
        synchronized (lock) {
            for (Map.Entry<UnitDescriptor, Integer> entry : map.entrySet()) {
                if (entry.getValue().equals(layerUniID)) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    @Override
    public  boolean contains(UnitDescriptor clientUnitDescriptor) {
        synchronized (lock) {
            return map.containsKey(clientUnitDescriptor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[clientUnitDescriptor <-> layerUniID]\n"); // NOI18N
        synchronized (lock) {
            for (Map.Entry<UnitDescriptor, Integer> entry : map.entrySet()) {
                sb.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n"); // NOI18N
            }
            return sb.toString();
        }
    }

    @Override
    public Collection<Integer> getUnitIDs() {
        synchronized (lock) {
            return map.values();
        }
    }

}
