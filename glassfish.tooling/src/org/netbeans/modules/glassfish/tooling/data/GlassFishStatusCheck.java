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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Server status check type.
 * <p/>
 * @author Tomas Kraus
 */
public enum GlassFishStatusCheck {

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
    private static final Logger LOGGER = new Logger(GlassFishStatusCheck.class);

    /** GlassFish version enumeration length. */
    public static final int length = GlassFishStatusCheck.values().length;

    /**  A <code>String</code> representation of PORT value. */
    private static final String PORT_STR = "PORT";

    /**  A <code>String</code> representation of VERSION value. */
    private static final String VERSION_STR = "VERSION";

    /**  A <code>String</code> representation of LOCATIONS value. */
    private static final String LOCATIONS_STR = "LOCATIONS";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, GlassFishStatusCheck> stringValuesMap
            = new HashMap(values().length);
    static {
        for (GlassFishStatusCheck state : GlassFishStatusCheck.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>GlassFishStatusCheck</code> with a value represented
     * by the specified <code>String</code>.
     * <p/>
     * The <code>GlassFishStatusCheck</code> returned represents existing value
     * only if specified <code>String</code> matches any <code>String</code>
     * returned by <code>toString</code> method. Otherwise <code>null</code>
     * value is returned.
     * <p>
     * @param name Value containing <code>GlassFishStatusCheck</code> 
     *             <code>toString</code> representation.
     * @return <code>GlassFishStatusCheck</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static GlassFishStatusCheck toValue(final String name) {
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
     * Convert <code>GlassFishStatusCheck</code> value to <code>String</code>.
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
