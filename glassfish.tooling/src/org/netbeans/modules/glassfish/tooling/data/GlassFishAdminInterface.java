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
            = new HashMap(values().length);

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
