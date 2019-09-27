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
package org.netbeans.modules.payara.tooling.data;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.payara.tooling.logging.Logger;

/**
 * Server status check type.
 * <p/>
 * @author Tomas Kraus
 */
public enum PayaraStatusCheck {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Administration port check. */
    PORT,

    /** Version command check. */
    VERSION,

    /** Locations command check. */
    LOCATIONS;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(PayaraStatusCheck.class);

    /** Payara version enumeration length. */
    public static final int length = PayaraStatusCheck.values().length;

    /**  A <code>String</code> representation of PORT value. */
    private static final String PORT_STR = "PORT";

    /**  A <code>String</code> representation of VERSION value. */
    private static final String VERSION_STR = "VERSION";

    /**  A <code>String</code> representation of LOCATIONS value. */
    private static final String LOCATIONS_STR = "LOCATIONS";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, PayaraStatusCheck> stringValuesMap
            = new HashMap(values().length);
    static {
        for (PayaraStatusCheck state : PayaraStatusCheck.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>PayaraStatusCheck</code> with a value represented
     * by the specified <code>String</code>.
     * <p/>
     * The <code>PayaraStatusCheck</code> returned represents existing value
     * only if specified <code>String</code> matches any <code>String</code>
     * returned by <code>toString</code> method. Otherwise <code>null</code>
     * value is returned.
     * <p>
     * @param name Value containing <code>PayaraStatusCheck</code> 
     *             <code>toString</code> representation.
     * @return <code>PayaraStatusCheck</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static PayaraStatusCheck toValue(final String name) {
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
     * Convert <code>PayaraStatusCheck</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        final String METHOD = "toString";
        switch (this) {
            case PORT:      return PORT_STR;
            case VERSION:   return VERSION_STR;
            case LOCATIONS: return LOCATIONS_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new DataException(
                    LOGGER.excMsg(METHOD, "invalidStatusCheck"));
        }
    }

}
