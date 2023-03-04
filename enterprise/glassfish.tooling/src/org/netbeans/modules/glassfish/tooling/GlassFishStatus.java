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
package org.netbeans.modules.glassfish.tooling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.netbeans.modules.glassfish.tooling.data.DataException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.server.state.GlassFishStatusEntity;
import org.netbeans.modules.glassfish.tooling.server.state.StatusJob;
import org.netbeans.modules.glassfish.tooling.server.state.StatusScheduler;

/**
 * GlassFish server status.
 * <p>Local server can be in 4 possible states:<p/>
 * <ul><li><i>Offline:</i> Server is not running</li>
 * <ul><li>no process</li>
 * <li>no network listeners</li>
 * <li>no administration interface responses</li></ul>
 * <li><i>Startup/Restart:</i> Server start or restart was requested</li>
 * <ul><li>active process (PID should change for restart)</li>
 * <li>network listeners may or may not be active</li>
 * <li>no administration interface responses</li></ul>
 * <li><i>Online:</i> Server is running</li>
 * <ul><li>active process</li>
 * <li>active network listeners</li>
 * <li>valid administration interface responses</li></ul>
 * <li><i>Shutdown:</i> Server shutdown was requested but server is still running</li>
 * <ul><li>active process</li>
 * <li>network listeners may or may not be active</li>
 * <li>administration interface may or may not be active</li></ul></ul>
 * <p>Remote server can be in 4 possible states:</p>
 * <ul><li><i>Offline:</i> Server is not running
 * <ul><li>no network listeners
 * <li>no administration interface responses</li></ul>
 * <li><i>Restart:</i> Server restart was requested</li>
 * <ul><li>network listeners may or may not be active</li>
 * <li>no administration interface responses</li></ul>
 * <li><i>Online:</i> Server is running</li>
 * <ul><li>active network listeners</li>
 * <li>valid administration interface responses</li></ul>
 * <li><i>Shutdown:</i> Server shutdown was requested but server is still running</li>
 * <ul><li>network listeners may or may not be active</li>
 * <li>administration interface may or may not be active</li></ul></ul>
 * <p/>
 * @author Tomas Kraus
 */
public enum GlassFishStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Server status is unknown. */
    UNKNOWN,

    /** Server is offline (not running or not responding). */
    OFFLINE,

    /** Server start or restart was requested but server is still not
     *  fully responding. */
    STARTUP,

    /** Server is running an responding. */
    ONLINE,

    /** Server shutdown was requested but server is still running
     *  or responding. */
    SHUTDOWN;
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(GlassFishStatus.class);

    /** GlassFish version enumeration length. */
    public static final int length = GlassFishStatus.values().length;

    /**  A <code>String</code> representation of UNKNOWN value. */
    private static final String UNKNOWN_STR = "UNKNOWN";

    /**  A <code>String</code> representation of OFFLINE value. */
    private static final String OFFLINE_STR = "OFFLINE";

    /**  A <code>String</code> representation of STARTUP value. */
    private static final String STARTUP_STR = "STARTUP";

    /**  A <code>String</code> representation of ONLINE value. */
    private static final String ONLINE_STR = "ONLINE";

    /**  A <code>String</code> representation of SHUTDOWN value. */
    private static final String SHUTDOWN_STR = "SHUTDOWN";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, GlassFishStatus> stringValuesMap
            = new HashMap<>(values().length);
    static {
        for (GlassFishStatus state : GlassFishStatus.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>GlassFishStatus</code> with a value represented by the
     * specified <code>String</code>.
     * <p/>
     * The <code>GlassFishStatus</code> returned represents existing value only
     * if specified <code>String</code> matches any <code>String</code> returned
     * by <code>toString</code> method. Otherwise <code>null</code> value
     * is returned.
     * <p>
     * @param name Value containing <code>GlassFishStatus</code> 
     *             <code>toString</code> representation.
     * @return <code>GlassFishStatus</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static GlassFishStatus toValue(final String name) {
        if (name != null) {
            return (stringValuesMap.get(name.toUpperCase()));
        } else {
            return null;
        }
    }

    /**
     * Initialize GlassFish server status task scheduler to use external
     * executor.
     * <p/>
     * This method must be called before adding first GlassFisg server instance
     * into scheduler.
     * <p/>
     * @param executor External executor to be used in scheduler.
     */
    public static void initScheduler(
            final ScheduledThreadPoolExecutor executor) {
        StatusScheduler.init(executor);
    }

    /**
     * Register GlassFish server instance into scheduler and launch server
     * status checking jobs.
     * <p/>
     * It is possible to call this method for the same GlassFisg server instance
     * many times. Server instance will be added only if it is not already
     * registered.
     * <p/>
     * @param srv GlassFish server instance to be registered.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public static boolean add(final GlassFishServer srv) {
        StatusScheduler scheduler = StatusScheduler.getInstance();
        if (!scheduler.exists(srv)) {
            GlassFishStatusEntity status = new GlassFishStatusEntity(srv);
            return StatusScheduler.getInstance().add(status);
        } else {
            return false;
        }
    }

    /**
     * Register GlassFish server instance into scheduler, register server status
     * listener and launch server status checking jobs.
     * <p/>
     * It is possible to call this method for the same GlassFisg server instance
     * many times. Server instance and listener will be added only if it is not
     * already registered.
     * <p/>
     * @param srv GlassFish server instance to be registered.
     * @param listener Server status listener to be registered.
     * @param currentState Notify about current server status after every check
     *                     when <code>true</code>.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public static boolean add(final GlassFishServer srv,
            final GlassFishStatusListener listener, final boolean currentState,
            final GlassFishStatus... newState) {
        StatusScheduler scheduler = StatusScheduler.getInstance();
        if (!scheduler.exists(srv)) {
            GlassFishStatusEntity status = new GlassFishStatusEntity(srv);
            return StatusScheduler.getInstance()
                    .add(status, listener, currentState, newState);
        } else {
            return false;
        }
    }

    /**
     * Get current GlassFish server instance status.
     * <p/>
     * When status checking is disabled, it will restart it and return current
     * status which is probably <code>UNKNOWN</code>.
     * <p/>
     * @param srv GlassFish server instance to be searched.
     * @return GlassFish server instance status. Returns <code>UNKNOWN</code>
     *         value for unregistered server instance.
     */
    public static GlassFishStatus getStatus(final GlassFishServer srv) {
        GlassFishServerStatus status
                = StatusScheduler.getInstance().get(srv, null);
        return status != null ? status.getStatus() : GlassFishStatus.UNKNOWN;
    }

    /**
     * Get current GlassFish server instance status.
     * <p/>
     * When status checking is disabled, it will restart it and return current
     * status which is probably <code>UNKNOWN</code>. If listener is provided,
     * it will be registered to receive any state change notification following
     * status checking restart. This listener won't be unregistered
     * automatically so caller should handle it properly.
     * <p/>
     * @param srv      GlassFish server instance to be searched.
     * @param listener Server status listener to be registered when status
     *                 checking is being restarted.
     * @return GlassFish server instance status. Returns <code>UNKNOWN</code>
     *         value for unregistered server instance.
     */
    public static GlassFishStatus getStatus(final GlassFishServer srv,
            final GlassFishStatusListener listener) {
        GlassFishServerStatus status
                = StatusScheduler.getInstance().get(srv, listener);
        return status != null ? status.getStatus() : GlassFishStatus.UNKNOWN;
    }

    /**
     * Get current GlassFish server instance {@link GlassFishServerStatus}
     * object.
     * <p/>
     * When status checking is disabled, it will restart it and return current
     * status which is probably <code>UNKNOWN</code>.
     * <p/>
     * @param srv GlassFish server instance to be searched.
     * @return GlassFish server status {@link GlassFishServerStatus} object.
     *         Returns <code>null</code> value for unregistered server instance.
     */
    public static GlassFishServerStatus get(final GlassFishServer srv) {
        return StatusScheduler.getInstance().get(srv, null);
    }
    
    /**
     * Get current GlassFish server instance {@link GlassFishServerStatus}
     * object.
     * <p/>
     * When status checking is disabled, it will restart it and return current
     * status which is probably <code>UNKNOWN</code>. If listener is provided,
     * it will be registered to receive any state change notification following
     * status checking restart. This listener won't be unregistered
     * automatically so caller should handle it properly.
     * <p/>
     * @param srv GlassFish server instance to be searched.
     * @param listener Server status listener to be registered when status
     *                 checking is being restarted.
     * @return GlassFish server status {@link GlassFishServerStatus} object.
     *         Returns <code>null</code> value for unregistered server instance.
     */
    public static GlassFishServerStatus get(final GlassFishServer srv,
            final GlassFishStatusListener listener) {
        return StatusScheduler.getInstance().get(srv, listener);
    }

    /**
     * Trigger startup mode for GlassFish server instance.
     * <p/>
     * This will switch status monitoring into startup mode where server
     * is being checked more often.
     * <p/>
     * @param srv      GlassFish server instance to be switched 
     *                 into startup mode.
     * @param force    Force startup mode for GlassFish server instance
     *                 from any state then <code>true</code>.
     * @param listener Server status listener to be registered together with
     *                 switching into startup mode.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public static boolean start(final GlassFishServer srv, final boolean force,
            final GlassFishStatusListener listener,
            final GlassFishStatus... newState) {
        return StatusScheduler.getInstance().start(
                srv, force, listener, newState);
    }

    /**
     * Trigger startup mode for GlassFish server instance.
     * <p/>
     * This will switch status monitoring into startup mode where server
     * is being checked more often.
     * <p/>
     * @param srv GlassFish server instance to be switched into startup mode.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public static boolean start(final GlassFishServer srv) {
        return StatusScheduler.getInstance().start(srv, false, null);
    }

    /**
     * Trigger shutdown mode for GlassFish server instance.
     * <p/>
     * This will switch status monitoring into shutdown mode where server
     * is being checked more often.
     * <p/>
     * @param srv GlassFish server instance to be switched into shutdown mode.
     * @return Value of <code>true</code> when server instance was successfully
     *         added into scheduler and status checking job was started
     *         or <code>false</code> otherwise.
     */
    public static boolean shutdown(final GlassFishServer srv) {
        return StatusScheduler.getInstance().shutdown(srv);
    }

    /**
     * Remove GlassFish server instance from scheduler and stop server
     * status checking jobs.
     * <p/>
     * It is possible to call this method for the same GlassFisg server instance
     * many times. Server instance will be removed only if it is registered.
     * <p/>
     * @param srv GlassFish server instance to be removed.
     * @return Value of <code>true</code> when server instance was successfully
     *         removed from scheduler and status checking job was stopped
     *         or <code>false</code> when server instance was not registered.
     */
    public static boolean remove(final GlassFishServer srv) {
        return StatusScheduler.getInstance().remove(srv);
    }

    /**
     * Suspend server status monitoring for GlassFisg server instance.
     * <p/>
     * @param srv GlassFish server instance for which to suspend monitoring.
     * @return Value of <code>true</code> when server instance monitoring
     *         was suspended or <code>false</code> when server instance
     *         is not registered.
     */
    public static boolean suspend(final GlassFishServer srv) {
        return StatusScheduler.getInstance().suspend(srv);
    }

    /**
     * Register server status listener to be notified about current server
     * status after every check.
     * <p/>
     * @param srv GlassFish server instance being monitored.
     * @param listener Server status listener to be registered.
     * @return Value of <code>true</code> when listener was added
     *         or <code>false</code> when this listener was already registered
     *         or GlassFish server instance was not registered.
     */
    public static boolean addCheckListener(final GlassFishServer srv,
            final GlassFishStatusListener listener) {
        final StatusJob job = StatusScheduler.getInstance().getJob(srv);
        if (job != null) {
            return job.addCurrStatusListener(listener);
        } else {
            return false;
        }
    }
    
    /**
     * Register server status listener to be notified about server status
     * change.
     * <p/>
     * @param srv GlassFish server instance being monitored.
     * @param listener Server status listener to be registered.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when listener was added in at least
     *         one list or <code>false</code> when this listener was already
     *         registered in all requested lists or GlassFish server instance
     *         was not registered.
     */
    public static boolean addChangeListener(final GlassFishServer srv,
            final GlassFishStatusListener listener,
            final GlassFishStatus... newState) {
        final StatusJob job = StatusScheduler.getInstance().getJob(srv);
        if (job != null) {
            return job.addNewStatusListener(listener, newState);
        } else {
            return false;
        }
    }

    /**
     * Register server status listener to be notified about server status
     * change.
     * <p/>
     * @param srv GlassFish server instance being monitored.
     * @param listener Server status listener to be registered.
     * @return Value of <code>true</code> when listener was added in at least
     *         one list or <code>false</code> when this listener was already
     *         registered in all requested lists or GlassFish server instance
     *         was not registered.
     */
    public static boolean addErrorListener(final GlassFishServer srv,
            final GlassFishStatusListener listener) {
        final StatusJob job = StatusScheduler.getInstance().getJob(srv);
        if (job != null) {
            return job.addErrorListener(listener);
        } else {
            return false;
        }
    }

    /**
     * Register server status listener.
     * <p/>
     * @param srv GlassFish server instance being monitored.
     * @param listener Server status listener to be registered.
     * @param currentState Notify about current server status after every check
     *                     when <code>true</code>.
     * @param newState Notify about server status change for new states
     *                 provided as this argument.
     * @return Value of <code>true</code> when listener was added in at least
     *         one list or <code>false</code> when this listener was already
     *         registered in all requested lists or GlassFish server instance
     *         was not registered.
     */
    public static boolean addListener(final GlassFishServer srv,
            final GlassFishStatusListener listener, final boolean currentState,
            final GlassFishStatus... newState) {
        final StatusJob job = StatusScheduler.getInstance().getJob(srv);
        if (job != null) {
            return job.addStatusListener(listener, currentState, newState);
        } else {
            return false;
        }
    }

    /**
     * Unregister server status listener.
     * <p/>
     * @param srv GlassFish server instance being monitored.
     * @param listener Server status listener to be unregistered.
     * @return Value of <code>true</code> when listener was found and removed
     *         or <code>false</code> when listener was not found among
     *         registered listeners or GlassFish server instance was not
     *         registered.
     */
    public static boolean removeListener(final GlassFishServer srv,
            final GlassFishStatusListener listener) {
        final StatusJob job = StatusScheduler.getInstance().getJob(srv);
        if (job != null) {
            return job.removeStatusListener(listener);
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>GlassFishStatus</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        final String METHOD = "toString";
        switch (this) {
            case UNKNOWN:  return UNKNOWN_STR;
            case OFFLINE:  return OFFLINE_STR;
            case STARTUP:  return STARTUP_STR;
            case ONLINE:   return ONLINE_STR;
            case SHUTDOWN: return SHUTDOWN_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new DataException(
                    LOGGER.excMsg(METHOD, "invalidState"));
        }
    }

}
