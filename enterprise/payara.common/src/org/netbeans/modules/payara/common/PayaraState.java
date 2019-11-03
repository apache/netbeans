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
package org.netbeans.modules.payara.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.common.status.AuthFailureStateListener;
import org.netbeans.modules.payara.common.status.MonitoringInitStateListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraServerStatus;

/**
 * Server state checks public module API.
 * <p/>
 * This API runs Payara server administration commands at the background
 * and is accessing server properties including administrator password stored
 * in <code>Keyring</code>.<br/>
 * Do not use this class in NetBeans startup code to avoid <code>Keyring</code>
 * access deadlocks.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraState {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check mode.
     * <p/>
     * Allows to select server state check mode.
     */
    public static enum Mode {

        ////////////////////////////////////////////////////////////////////////
        // Enum values                                                        //
        ////////////////////////////////////////////////////////////////////////

        /** Default server state check mode. All special features
         *  are turned off. */
        DEFAULT,
        /** Startup mode. Sets longer administration commands timeout
         *  and displays Payara 3.1.2 WS bug warning. */
        STARTUP,
        /** Refresh mode. Displays enable-secure-admin warning
         *  for remote servers. */
        REFRESH;

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(PayaraState.class);

    /** Initial server status check timeout [ms]. Maximum period of time to wait
     *  for status monitoring to settle down. */
    private static final int INIT_MONITORING_TIMEOUT = 5000;

    /**
     * Start monitoring Payara server.
     * <p/>
     * This method may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance Payara server instance to be monitored.
     */
    public static boolean monitor(final PayaraServer instance) {
        boolean added;
        // Check if server is already being monitored.
        PayaraServerStatus status = PayaraStatus.get(instance);
        if (status == null) {
            MonitoringInitStateListener listener
                    = new MonitoringInitStateListener();
            // All state change events except UNKNOWN.
            added = PayaraStatus.add(instance, listener, false,
                    PayaraStatus.OFFLINE, PayaraStatus.STARTUP,
                    PayaraStatus.ONLINE, PayaraStatus.SHUTDOWN);
            if (added) {
                if (instance.getVersion() != null) {
                   AuthFailureStateListener authListener
                           =  new AuthFailureStateListener(
                           instance.getVersion().ordinal()
                           >= PayaraVersion.PF_4_1_144.ordinal());
                   PayaraStatus.addChangeListener(instance, authListener, PayaraStatus.STARTUP);
                    PayaraStatus.addErrorListener(instance, authListener);
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
                    PayaraStatus.removeListener(instance, listener);
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
     * @param instance Payara server instance.
     * @param listener Already active status monitoring listener waiting
     *                 for leaving <code>UNKNOWN</code> state.
     * @param timeout  How log to wait for <code>UNKNOWN</code> state
     *                 to be resolved in ms.
     */
    private static void waitForKnownState(final PayaraServer instance,
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
            PayaraStatus.removeListener(instance, listener);
        }

    }

    /**
     * Retrieve Payara server status object from status monitoring.
     * <p/>
     * Can block up to <code>timeout</code> ms when server monitoring
     * was suspended to wait for new status check to finish. Do not use
     * with non zero <code>timeout</code> in AWT event queue thread.
     * <p/>
     * @param instance Payara server instance.
     * @param timeout  How log to wait for <code>UNKNOWN</code> state
     *                 to be resolved. Value of <code>0</code> turns blocking
     *                 off.
     * @return Payara server status object.
     * @throws IllegalStateException when status object is null even after 
     *         monitoring of this instance was explicitely started.
     */
    public static PayaraServerStatus getStatus(
            final PayaraServer instance, final long timeout) {       
        MonitoringInitStateListener listener = timeout > 0
               ? new MonitoringInitStateListener() : null;
        PayaraServerStatus status = PayaraStatus.get(instance, listener);
        if (status == null) {
            monitor(instance);
            status = PayaraStatus.get(instance);
            if (status == null) {
                throw new IllegalStateException(NbBundle.getMessage(PayaraState.class,
                        "PayaraState.getStatus.statusNull"));
            }
        } else {
            if (listener != null && listener.isActive()) {
                waitForKnownState(instance, listener, timeout);
            }
        }
        return status;
    }

    /**
     * Retrieve Payara server status object from status monitoring.
     * <p/>
     * This call is always non blocking but it will return <code>UNKNOWN</code>
     * state immediately when server state monitoring is suspended.
     * <p/>
     * @param instance Payara server instance.
     * @return Payara server status object.
     * @throws IllegalStateException when status object is null even after 
     *         monitoring of this instance was explicitely started.
     */
    public static PayaraServerStatus getStatus(
            final PayaraServer instance) {
        return getStatus(instance, 0);
    }

    /**
     * Check if Payara server is running in <code>DEFAULT</code> mode.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance Payara server instance.
     * @return Returns <code>true</code> when Payara server is online
     *         or <code>false</code> otherwise.
     */
    public static boolean isOnline(final PayaraServer instance) {
        return getStatus(instance).getStatus() == PayaraStatus.ONLINE;
    }

    /**
     * Check if Payara server is offline.
     * <p/>
     * Check may cause delay when server status was not monitored before
     * to give status monitoring time to settle down.
     * <p/>
     * @param instance Payara server instance.
     * @return Returns <code>true</code> when Payara server offline
     *         or <code>false</code> otherwise.
     */
    public static boolean isOffline(final PayaraServer instance) {
        return getStatus(instance).getStatus() == PayaraStatus.OFFLINE;
    }

    /**
     * Check if Payara server can be started;
     * <p/>
     * Server can be started only when 
     * <p/>
     * @param instance Payara server instance.
     * @return Value of <code>true</code> when Payara server can be started
     *         or <code>false</code> otherwise.
     */
    public static boolean canStart(final PayaraServer instance) {
        PayaraServerStatus status = getStatus(instance);
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
