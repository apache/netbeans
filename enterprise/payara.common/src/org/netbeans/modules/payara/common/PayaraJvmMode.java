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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author kratz
 */
public enum PayaraJvmMode {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Normal mode. */
    NORMAL,

    /** Debug mode. */
    DEBUG,

    /** Profiling mode. */
    PROFILE;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(PayaraJvmMode.class);

    /** Payara version enumeration length. */
    public static final int length = PayaraJvmMode.values().length;

    /**  A <code>String</code> representation of NORMAL value. */
    private static final String NORMAL_STR = "normalMode";

    /**  A <code>String</code> representation of DEBUG value. */
    private static final String DEBUG_STR = "debugMode";

    /**  A <code>String</code> representation of PROFILE value. */
    private static final String PROFILE_STR = "profileMode";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, PayaraJvmMode> stringValuesMap
            = new HashMap<String, PayaraJvmMode>(length);
    static {
        for (PayaraJvmMode mode : PayaraJvmMode.values()) {
            stringValuesMap.put(mode.toString().toUpperCase(), mode);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////
   
    /**
     * Returns a <code>PayaraJvmMode</code> with a value represented by the
     * specified <code>String</code>. The <code>PayaraJvmMode</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param name Value containing <code>PayaraJvmMode</code> 
     *             <code>toString</code> representation.
     * @return <code>PayaraJvmMode</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static PayaraJvmMode toValue(final String name) {
        if (name != null) {
            return (stringValuesMap.get(name.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>PayaraJvmMode</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case NORMAL:  return NORMAL_STR;
            case DEBUG:   return DEBUG_STR;
            case PROFILE: return PROFILE_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new IllegalStateException(NbBundle.getMessage(PayaraJvmMode.class,
                    "PayaraJvmMode.toString.invalid"));
        }
    }
}
