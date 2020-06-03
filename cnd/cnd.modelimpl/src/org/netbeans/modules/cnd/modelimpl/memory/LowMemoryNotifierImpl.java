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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * Implementation of LowMemoryNotifier.
 */

// package-local
class LowMemoryNotifierImpl extends LowMemoryNotifier implements NotificationListener {

    private final Logger logger;

    public LowMemoryNotifierImpl() {

        logger = Logger.getLogger(getClass().getPackage().getName());
        String level = System.getProperty(logger.getName());
        if (level != null) {
            try {
                logger.setLevel(Level.parse(level));
            } catch (IllegalArgumentException e) {
            }
        } else {
            logger.setLevel(Level.SEVERE);
        }

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        if (mbean instanceof NotificationEmitter) {
            NotificationEmitter emitter = (NotificationEmitter) mbean;
            emitter.addNotificationListener(this, null, null);
        }
    }

    @Override
    public void addListener(LowMemoryListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(LowMemoryListener listener) {
        logger.info("LowMemoryNotifierImpl.removeListener " + listener); // NOI18N
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void setThresholdPercentage(double percentage) {
        logger.info("LowMemoryNotifierImpl.setThresholdPercentage " + percentage); // NOI18N
        assert (0 < percentage && percentage < 1.0);
        long maxMemory = pool.getUsage().getMax();
        long threshold = (long) (maxMemory * percentage);
        pool.setUsageThreshold(threshold);
    }

    /**
     * Implements NotificationListener.
     *
     * @param notification The notification.
     *
     * @param handback An opaque object which helps the listener to associate information
     * regarding the MBean emitter. This object is passed to the MBean during the
     * addListener call and resent, without modification, to the listener. The MBean object
     * should not use or modify the object.
     */
    @Override
    public void handleNotification(Notification notification, Object hb) {
        logger.info("LowMemoryNotifierImpl.handleNotification " + notification); // NOI18N
        if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
            long maxMemory = pool.getUsage().getMax();
            long usedMemory = pool.getUsage().getUsed();
            logger.info("LowMemoryNotifierImpl.handleNotification " + maxMemory + '/' + usedMemory); // NOI18N
            fireMemoryLow(maxMemory, usedMemory);
        }
    }

    private static MemoryPoolMXBean findHeapPool() {
        for (MemoryPoolMXBean memPool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (memPool.getType() == MemoryType.HEAP && memPool.isUsageThresholdSupported()) {
                return memPool;
            }
        }
        return null;
    }

    private void fireMemoryLow(long maxMemory, long usedMemory) {
        LowMemoryEvent event = new LowMemoryEvent(this, maxMemory, usedMemory);
        LowMemoryListener[] la = getListeners();
        for (int i = 0; i < la.length; i++) {
            la[i].memoryLow(event);
        }
    }

    private LowMemoryListener[] getListeners() {
        synchronized (listeners) {
            LowMemoryListener[] result = new LowMemoryListener[listeners.size()];
            listeners.toArray(result);
            return result;
        }
    }
    private final Collection<LowMemoryListener> listeners = new LinkedList<>();
    private static final MemoryPoolMXBean pool = findHeapPool();
}
