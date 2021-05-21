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
package org.netbeans.modules.payara.tooling.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaEE modules supported by Payara.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum ModuleType {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** The module is an EAR archive. */
    EAR,
    /** The module is an Enterprise Java Bean archive. */
    EJB,
    /** The module is an Client Application archive. */
    CAR,
    /** The module is an Connector archive. */
    RAR,
    /** The module is an Web Application archive. */
    WAR;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara JavaEE profile enumeration length. */
    public static final int length = ModuleType.values().length;

    /**  A <code>String</code> representation of EAR value. */
    static final String EAR_STR = "ear";

    /**  A <code>String</code> representation of EJB value. */
    static final String EJB_STR = "ejb";

    /**  A <code>String</code> representation of CAR value. */
    static final String CAR_STR = "car";

    /**  A <code>String</code> representation of RAR value. */
    static final String RAR_STR = "rar";

    /**  A <code>String</code> representation of WAR value. */
    static final String WAR_STR = "war";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, ModuleType> stringValuesMap
            = new HashMap<>(2 * values().length);

    // Initialize backward String conversion Map.
    static {
        for (ModuleType profile : ModuleType.values()) {
            stringValuesMap.put(profile.toString().toUpperCase(), profile);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>ModuleType</code> with a value represented by the
     * specified <code>String</code>. The <code>ModuleType</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param stateStr Value containing <code>ModuleType</code> 
     *                 <code>toString</code> representation.
     * @return <code>ModuleType</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static ModuleType toValue(final String stateStr) {
        if (stateStr != null) {
            return (stringValuesMap.get(stateStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert module type name to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case EAR:     return EAR_STR;
            case EJB:     return EJB_STR;
            case CAR:     return CAR_STR;
            case RAR:     return RAR_STR;
            case WAR:     return WAR_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default:   throw new ServerConfigException(
                        ServerConfigException.INVALID_MODULE_TYPE_NAME);
        }
    }
}
