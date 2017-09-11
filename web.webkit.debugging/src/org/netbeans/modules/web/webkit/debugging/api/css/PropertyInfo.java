/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Information about a supported CSS property.
 *
 * @author Jan Stola
 */
public class PropertyInfo {
    /** Property name. */
    private final String name;
    /** Longhand property names. */
    private final List<String> longhands;

    /**
     * Creates a new {@code PropertyInfo} that corresponds to the given JSONObject.
     *
     * @param propertyInfo JSONObject describing the property.
     */
    PropertyInfo(JSONObject propertyInfo) {
        name = (String)propertyInfo.get("name"); // NOI18N
        JSONArray longHandsArray = (JSONArray)propertyInfo.get("longhands"); // NOI18N
        if (longHandsArray == null) {
            longhands = Collections.EMPTY_LIST;
        } else {
            longhands = (List<String>)longHandsArray;
        }
    }

    /**
     * Creates a new empty {@code PropertyInfo} for a property
     * with the specified name.
     *
     * @param name name of a CSS property.
     */
    PropertyInfo(String name) {
        this.name = name;
        this.longhands = Collections.EMPTY_LIST;
    }

    /**
     * Returns the name of the property.
     *
     * @return name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns longhand property names.
     *
     * @return longhand property names.
     */
    public List<String> getLonghands() {
        return Collections.unmodifiableList(longhands);
    }

}
