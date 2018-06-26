/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaEE modules supported by Glassfish.
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

    /** GlassFish JavaEE profile enumeration length. */
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
