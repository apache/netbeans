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
package org.netbeans.modules.glassfish.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.status.AuthFailureStateListener;
import org.netbeans.modules.glassfish.common.status.MonitoringInitStateListener;
import org.openide.util.NbBundle;

/**
 * Server state checks public module API.
 * <p/>
 * This API runs GlassFish server administration commands at the background
 * and is accessing server properties including administrator password stored
 * in <code>Keyring</code>.<br/>
 * Do not use this class in NetBeans startup code to avoid <code>Keyring</code>
 * access deadlocks.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishState {

    // Inner classes                                                          //
    /**
     * Check mode.
     * <p/>
     * Allows to select server state check mode.
     */
    public static enum Mode {

        // Enum values                                                        //
        /** Default server state check mode. All special features
         *  are turned off. */
        DEFAULT,
        /** Startup mode. Sets longer administration commands timeout
         *  and displays GlassFish 3.1.2 WS bug warning. */
        STARTUP,
        /** Refresh mode. Displays enable-secure-admin warning
         *  for remote servers. */
        REFRESH;

        // Methods                                                            //
        /**
         * Convert <code>Mode</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value
         *         of this object.
         */
        @Override
        public String toString() {
            switch(this) {
                case DEFAULT: return "DEFAULT";
                case STARTUP: return "STARTUP";
                case REFRESH: return "REFRESH";
                default: throw new IllegalStateException("Unknown Mode value");
            }
        }

    }

    // Class attributes                                                       //
    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassFishState.class);

    /** Initial server status check timeout [ms]. Maximum period of time to wait
     *  for status monitoring to settle down. */
    private static final int INIT_MONITORING_TIMEOUT = 5000;

    /**
     * Start monitoring GlassFish server.
     * <p/>
     * This method may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance to be monitored.
     */
    public static boolean monitor(final GlassFishServer instance) {
        boolean added;
        // Check if server is already being monitored.
        GlassFishServerStatus status = GlassFishStatus.get(instance);
        if (status == null) {
            MonitoringInitStateListener listener
                    = new MonitoringInitStateListener();
            // All state change events except UNKNOWN.
            added = GlassFishStatus.add(instance, listener, false,
                    GlassFishStatus.OFFLINE, GlassFishStatus.STARTUP,
                    GlassFishStatus.ONLINE, GlassFishStatus.SHUTDOWN);
            if (added) {
                if (instance.getVersion() != null) {
                   AuthFailureStateListener authListener
                           =  new AuthFailureStateListener(
                           instance.getVersion().ordinal()
                           >= GlassFishVersion.GF_4.ordinal());
                   GlassFishStatus.addChangeListener(
                           instance, authListener, GlassFishStatus.STARTUP);
                    GlassFishStatus.addErrorListener(instance, authListener);
                }
                try {
                    long startTime = System.currentTimeMillis();
                    long waitTime = INIT_MONITORING_TIMEOUT; 
                    synchronized (listener) {
                        // Guard against spurious wakeup.
                        while (!listener.isWakeUp() && waitTime > 0) {
                            listener.wait(waitTime);
                            waitTime = INIT_MONITORING_TIMEOUT
                            + startTime - System.currentTimeMillis();
                        }
                    }
                } catch (InterruptedException ie) {
                    LOGGER.log(Level.FINE,
                            "Interrupted while waiting on server monitoring");
                } finally {
                    GlassFishStatus.removeListener(instance, listener);
                }
            }
        } else {
            added = false;
        }
        return added;
    }

    /**
     * Wait for status monitoring to resolve <code>UNKNOWN</code> state.
     * <p/>
     * Status monitoring listener will be removed when finished.
     * <p/>
     * @param instance GlassFish server instance.
     * @param listener Already active status monitoring listener waiting
     *                 for leaving <code>UNKNOWN</code> state.
     * @param timeout  How log to wait for <code>UNKNOWN</code> state
     *                 to be resolved in ms.
     */
    private static void waitForKnownState(final GlassFishServer instance,
            final MonitoringInitStateListener listener, final long timeout) {
        try {
            long startTime = System.currentTimeMillis();
            long waitTime = timeout;
            synchronized (listener) {
                // Guard against spurious wakeup.
                while (!listener.isWakeUp() && waitTime > 0) {
                    listener.wait(waitTime);
                    waitTime = timeout
                            + startTime - System.currentTimeMillis();
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.FINE,
                    "Interrupted while waiting on server monitoring");
        } finally {
            GlassFishStatus.removeListener(instance, listener);
        }

    }

    /**
     * Retrieve GlassFish server status object from status monitoring.
     * <p/>
     * Can block up to <code>timeout</code> ms when server monitoring
     * was suspended to wait for new status check to finish. Do not use
     * with non zero <code>timeout</code> in AWT event queue thread.
     * <p/>
     * @param instance GlassFish server instance.
     * @param timeout  How log to wait for <code>UNKNOWN</code> state
     *                 to be resolved. Value of <code>0</code> turns blocking
     *                 off.
     * @return GlassFish server status object.
     * @throws IllegalStateException when status object is null even after 
     *         monitoring of this instance was explicitely started.
     */
    public static GlassFishServerStatus getStatus(
            final GlassFishServer instance, final long timeout) {       
        MonitoringInitStateListener listener = timeout > 0
               ? new MonitoringInitStateListener() : null;
        GlassFishServerStatus status = GlassFishStatus.get(instance, listener);
        if (status == null) {
            monitor(instance);
            status = GlassFishStatus.get(instance);
            if (status == null) {
                throw new IllegalStateException(NbBundle.getMessage(
                        GlassFishState.class,
                        "GlassFishState.getStatus.statusNull"));
            }
        } else {
            if (listener != null && listener.isActive()) {
                waitForKnownState(instance, listener, timeout);
            }
        }
        return status;
    }

    /**
     * Retrieve GlassFish server status object from status monitoring.
     * <p/>
     * This call is always non blocking but it will return <code>UNKNOWN</code>
     * state immediately when server state monitoring is suspended.
     * <p/>
     * @param instance GlassFish server instance.
     * @return GlassFish server status object.
     * @throws IllegalStateException when status object is null even after 
     *         monitoring of this instance was explicitely started.
     */
    public static GlassFishServerStatus getStatus(
            final GlassFishServer instance) {
        return getStatus(instance, 0);
    }

    /**
     * Check if GlassFish server is running in <code>DEFAULT</code> mode.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance.
     * @return Returns <code>true</code> when GlassFish server is online
     *         or <code>false</code> otherwise.
     */
    public static boolean isOnline(final GlassFishServer instance) {
        return getStatus(instance).getStatus() == GlassFishStatus.ONLINE;
    }

    /**
     * Check if GlassFish server is offline.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance GlassFish server instance.
     * @return Returns <code>true</code> when GlassFish server offline
     *         or <code>false</code> otherwise.
     */
    public static boolean isOffline(final GlassFishServer instance) {
        return getStatus(instance).getStatus() == GlassFishStatus.OFFLINE;
    }

    /**
     * Check if GlassFish server can be started;
     * <p/>
     * Server can be started only when 
     * <p/>
     * @param instance GlassFish server instance.
     * @return Value of <code>true</code> when GlassFish server can be started
     *         or <code>false</code> otherwise.
     */
    public static boolean canStart(final GlassFishServer instance) {
        GlassFishServerStatus status = getStatus(instance);
        switch(status.getStatus()) {
            case UNKNOWN: case ONLINE: case SHUTDOWN: case STARTUP:
                return false;
            default:
                // Allow start even with admin port occupied.
                //return !ServerUtils.isDASRunning(instance);
                return true;
        }

    }

}
