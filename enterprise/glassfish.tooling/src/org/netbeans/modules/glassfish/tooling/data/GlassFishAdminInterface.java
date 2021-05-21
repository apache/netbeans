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
package org.netbeans.modules.glassfish.tooling.data;

import java.util.HashMap;
import java.util.Map;

/**
 * GlassFish Server Administration Interface.
 * <p>
 * Local GlassFish server administration interface type used to mark proper
 * administration interface for individual GlassFish servers.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum GlassFishAdminInterface {
    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////
    /** GlassFish server administration interface is REST. */
    REST,
    /** GlassFish server administration interface is HTTP. */
    HTTP;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  A <code>String</code> representation of REST value. */
    static final String REST_STR = "REST";

    /**  A <code>String</code> representation of HTTP value. */
    static final String HTTP_STR = "HTTP";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, GlassFishAdminInterface> stringValuesMap
            = new HashMap<>(values().length);

    // Initialize backward String conversion <code>Map</code>.
    static {
        for (GlassFishAdminInterface adminInterface
                : GlassFishAdminInterface.values()) {
            stringValuesMap.put(
                    adminInterface.toString().toUpperCase(), adminInterface);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>GlassFishAdminInterface</code> with a value represented
     * by the specified <code>String</code>. The
     * <code>GlassFishAdminInterface</code> returned represents existing value
     * only if specified <code>String</code> matches any <code>String</code>
     * returned by <code>toString</code> method. Otherwise <code>null</code>
     * value is returned.
     * <p>
     * @param name Value containing <code>GlassFishAdminInterface</code> 
     *             <code>toString</code> representation.
     * @return <code>GlassFishAdminInterface</code> value represented
     *         by <code>String</code> or <code>null</code> if value was
     *         not recognized.
     */
    public static GlassFishAdminInterface toValue(String name) {
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
     * Convert <code>GlassFishAdminInterface</code> value to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case REST: return REST_STR;
            case HTTP: return HTTP_STR;
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default:   throw new DataException(
                        DataException.INVALID_ADMIN_INTERFACE);
        }
    }
}
