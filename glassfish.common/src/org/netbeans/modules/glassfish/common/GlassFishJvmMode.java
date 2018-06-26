/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author kratz
 */
public enum GlassFishJvmMode {

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
            = GlassFishLogger.get(GlassFishJvmMode.class);

    /** GlassFish version enumeration length. */
    public static final int length = GlassFishJvmMode.values().length;

    /**  A <code>String</code> representation of NORMAL value. */
    private static final String NORMAL_STR = "normalMode";

    /**  A <code>String</code> representation of DEBUG value. */
    private static final String DEBUG_STR = "debugMode";

    /**  A <code>String</code> representation of PROFILE value. */
    private static final String PROFILE_STR = "profileMode";

    /** Stored <code>String</code> values for backward <code>String</code>
     *  conversion. */
    private static final Map<String, GlassFishJvmMode> stringValuesMap
            = new HashMap<String, GlassFishJvmMode>(length);
    static {
        for (GlassFishJvmMode mode : GlassFishJvmMode.values()) {
            stringValuesMap.put(mode.toString().toUpperCase(), mode);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////
   
    /**
     * Returns a <code>GlassFishJvmMode</code> with a value represented by the
     * specified <code>String</code>. The <code>GlassFishJvmMode</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param name Value containing <code>GlassFishJvmMode</code> 
     *             <code>toString</code> representation.
     * @return <code>GlassFishJvmMode</code> value represented
     *         by <code>String</code> or <code>null</code> if value
     *         was not recognized.
     */
    public static GlassFishJvmMode toValue(final String name) {
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
     * Convert <code>GlassFishJvmMode</code> value to <code>String</code>.
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
            default: throw new IllegalStateException(NbBundle.getMessage(
                    GlassFishJvmMode.class,
                    "GlassFishJvmMode.toString.invalid"));
        }
    }
}
