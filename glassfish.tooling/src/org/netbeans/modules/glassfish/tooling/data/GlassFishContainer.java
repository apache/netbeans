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
package org.netbeans.modules.glassfish.tooling.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * GlassFish Server Containers.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum GlassFishContainer implements Comparator<GlassFishContainer> {

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
    private static final Map<String, GlassFishContainer> stringValuesMap
            = new HashMap(2 * values().length);

    // Initialize backward String conversion Map.
    static {
        for (GlassFishContainer container : GlassFishContainer.values()) {
            stringValuesMap.put(container.toString().toUpperCase(), container);
        }
    }

    /**
     * Returns a <code>GlassFishContainer</code> with a value represented by
     * the specified <code>String</code>. The <code>GlassFishContainer</code>
     * returned represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param containerStr Value containing container <code>String</code>
     *                   representation.
     * @return <code>GlassFishContainer</code> value represented
     *         by <code>String</code> or <code>null</code> if value was
     *         not recognized.
     */
    public static GlassFishContainer toValue(String containerStr) {
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
     * Convert <code>GlassFishContainer</code> value to <code>String</code>.
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
    public int compare(GlassFishContainer container1,
            GlassFishContainer container2) {
        return container1 != null && container2 != null
                ? container1.ordinal() - container2.ordinal()
                : container1 != null
                    ? container2.ordinal()
                    : container2 != null
                        ? -container1.ordinal()
                        : 0;
    }

}
