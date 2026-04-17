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
package org.netbeans.modules.glassfish.tooling.server.state;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.data.DataException;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Server status check internal state.
 * <p/>
 * Internally there are more states to be recognized when server is partially up
 * but not fully responding. This helps to use just subset of checks in some
 * states and also to use different checks synchronization strategy.
 * <p/>
 * @author Tomas Kraus
 */
public enum StatusJobState {
    // Enum values                                                            //
    /** Server status checks are turned off. */
    NO_CHECK,

    /** Server status is unknown. */
    UNKNOWN,

    /** Server status yet unknown but administrator port is alive. */
    UNKNOWN_PORT,

    /** Server is offline (not running, not responding and administrator port
     *  is not alive). */
    OFFLINE,

    /** Server is offline (not responding but running and administrator port
     *  is alive). */
    OFFLINE_PORT,

    /** Server start or restart was requested but server is still not
     *  responding and administrator port is not alive. */
    STARTUP,

    /** Server start or restart was requested but server is still not
     *  bully responding but administrator port is alive. */
    STARTUP_PORT,

    /** Server is running and responding. */
    ONLINE,

    /** Server shutdown was requested but server is still running, responding
     *  and administrator port is alive. */
    SHUTDOWN,
    
    /** Server shutdown was requested but server is still running, administrator
     *  port is alive but server is not responding. . */
    SHUTDOWN_PORT;

    // Class attributes                                                       //
    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(StatusJobState.class);

    /** GlassFish version enumeration length. */
    public static final int length = StatusJobState.values().length;

    /**  A <code>String</code> representation of NO_CHECK value. */
    private static final String NO_CHECK_STR = "NO_CHECK";

    /**  A <code>String</code> representation of UNKNOWN value. */
    private static final String UNKNOWN_STR = "UNKNOWN";

    /**  A <code>String</code> representation of UNKNOWN_PORT value. */
    private static final String UNKNOWN_PORT_STR = "UNKNOWN_PORT";

    /**  A <code>String</code> representation of OFFLINE value. */
    private static final String OFFLINE_STR = "OFFLINE";

    /**  A <code>String</code> representation of OFFLINE_PORT value. */
    private static final String OFFLINE_PORT_STR = "OFFLINE_PORT";

    /**  A <code>String</code> representation of STARTUP value. */
    private static final String STARTUP_STR = "STARTUP";

    /**  A <code>String</code> representation of STARTUP_PORT value. */
    private static final String STARTUP_PORT_STR = "STARTUP_PORT";

    /**  A <code>String</code> representation of ONLINE value. */
    private static final String ONLINE_STR = "ONLINE";

    /**  A <code>String</code> representation of SHUTDOWN value. */
    private static final String SHUTDOWN_STR = "SHUTDOWN";

    /**  A <code>String</code> representation of SHUTDOWN_PORT value. */
    private static final String SHUTDOWN_PORT_STR = "SHUTDOWN_PORT";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, StatusJobState> stringValuesMap
            = new HashMap<>(length);
    static {
        for (StatusJobState state : StatusJobState.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    /** Server status check internal state to public GlassFish server status
     *  translation table. */
    private static final GlassFishStatus toGlassFishStatus[] = {
        GlassFishStatus.UNKNOWN,  // NO_CHECK
        GlassFishStatus.UNKNOWN,  // UNKNOWN
        GlassFishStatus.UNKNOWN,  // UNKNOWN_PORT
        GlassFishStatus.OFFLINE,  // OFFLINE
        GlassFishStatus.OFFLINE,  // OFFLINE_PORT
        GlassFishStatus.STARTUP,  // STARTUP
        GlassFishStatus.STARTUP,  // STARTUP_PORT
        GlassFishStatus.ONLINE,   // ONLINE
        GlassFishStatus.SHUTDOWN, // SHUTDOWN
        GlassFishStatus.SHUTDOWN  // SHUTDOWN_PORT
    };

    // Static methods                                                         //
    /**
     * Returns a <code>GlassFishStatus</code> with a value represented by the
     * specified <code>String</code>. The <code>GlassFishStatus</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param name Value containing <code>GlassFishStatus</code> 
     *             <code>toString</code> representation.
     * @return <code>GlassFishStatus</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static StatusJobState toValue(final String name) {
        if (name != null) {
            return (stringValuesMap.get(name.toUpperCase()));
        } else {
            return null;
        }
    }

    // Methods                                                                //
    /**
     * Convert <code>StatusJobState</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        final String METHOD = "toString";
        switch (this) {
            case NO_CHECK:      return NO_CHECK_STR;
            case UNKNOWN:       return UNKNOWN_STR;
            case UNKNOWN_PORT:  return UNKNOWN_PORT_STR;
            case OFFLINE:       return OFFLINE_STR;
            case OFFLINE_PORT:  return OFFLINE_PORT_STR;
            case STARTUP:       return STARTUP_STR;
            case STARTUP_PORT:  return STARTUP_PORT_STR;
            case ONLINE:        return ONLINE_STR;
            case SHUTDOWN:      return SHUTDOWN_STR;
            case SHUTDOWN_PORT: return SHUTDOWN_PORT_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new DataException(
                        LOGGER.excMsg(METHOD, "invalidVersion"));
        }
    }
    
    /**
     * Convert <code>StatusJobState</code> value to {@link GlassFishStatus}.
     * <p/>
     * @return A {@link GlassFishStatus} representation of the value
     *         of this object.
     */
    GlassFishStatus toGlassFishStatus() {
        return toGlassFishStatus[this.ordinal()];
    }

}
