/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
