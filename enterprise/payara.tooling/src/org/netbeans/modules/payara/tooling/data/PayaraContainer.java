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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Payara Server Containers.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum PayaraContainer implements Comparator<PayaraContainer> {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** EAR application. */
    EAR,
    /** jRuby application. */
    JRUBY,
    /** Web application. */
    WEB,
    /** EJB application. */
    EJB,
    /** Application client. */
    APPCLIENT,
    /** Connector. */
    CONNECTOR,
    /** Unknown application. */
    UNKNOWN;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  A <code>String</code> representation of EAR value. */
    static final String EAR_STR = "ear";

    /**  A <code>String</code> representation of JRUBY value. */
    static final String JRUBY_STR = "jruby";

    /**  A <code>String</code> representation of WEB value. */
    static final String WEB_STR = "web";

    /**  A <code>String</code> representation of EJB value. */
    static final String EJB_STR = "ejb";

    /**  A <code>String</code> representation of APPCLIENT value. */
    static final String APPCLIENT_STR = "appclient";

    /**  A <code>String</code> representation of CONNECTOR value. */
    static final String CONNECTOR_STR = "connector";

    /**  A <code>String</code> representation of UNKNOWN value. */
    static final String UNKNOWN_STR = "unknown";

    /** Version elements separator character. */
    public static final char SEPARATOR = ',';

    /**
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, PayaraContainer> stringValuesMap
            = new HashMap(2 * values().length);

    // Initialize backward String conversion Map.
    static {
        for (PayaraContainer container : PayaraContainer.values()) {
            stringValuesMap.put(container.toString().toUpperCase(), container);
        }
    }

    /**
     * Returns a <code>PayaraContainer</code> with a value represented by
     * the specified <code>String</code>. The <code>PayaraContainer</code>
     * returned represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param containerStr Value containing container <code>String</code>
     *                   representation.
     * @return <code>PayaraContainer</code> value represented
     *         by <code>String</code> or <code>null</code> if value was
     *         not recognized.
     */
    public static PayaraContainer toValue(String containerStr) {
        if (containerStr != null) {
            return (stringValuesMap.get(containerStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>PayaraContainer</code> value to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case EAR:       return EAR_STR;
            case JRUBY:     return JRUBY_STR;
            case WEB:       return WEB_STR;
            case EJB:       return EJB_STR;
            case APPCLIENT: return APPCLIENT_STR;
            case CONNECTOR: return CONNECTOR_STR;
            case UNKNOWN:   return UNKNOWN_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new DataException(
                        DataException.INVALID_CONTAINER);
        }
    }

    /**
     * Compares its two arguments for order.
     * <p/>
     * Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to,
     * or greater than the second.
     * <p/>
     * @param container1 The first object to be compared.
     * @param container2 The second object to be compared.
     * @return A negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second. 
     */
    @Override
    public int compare(PayaraContainer container1,
            PayaraContainer container2) {
        return container1 != null && container2 != null
                ? container1.ordinal() - container2.ordinal()
                : container1 != null
                    ? container2.ordinal()
                    : container2 != null
                        ? -container1.ordinal()
                        : 0;
    }

}
