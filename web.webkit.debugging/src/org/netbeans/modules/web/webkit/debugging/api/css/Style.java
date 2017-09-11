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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * CSS style.
 *
 * @author Jan Stola
 */
public class Style {
    /** Identifier of the style. */
    private final StyleId id;
    /** Properties in the style. */
    private final List<Property> properties;
    /** Style declaration text (if available). */
    private final String text;
    /** Style declaration range in the enclosing stylesheet (if available). */
    private SourceRange range;

    /**
     * Creates a new {@code Style} that corresponds to the given JSONObject.
     *
     * @param style JSONObject describing the style.
     */
    Style(JSONObject style) {
        this(style, null);
    }

    Style(JSONObject style, String preferredId) {
        if (preferredId != null) {
            id = new StyleId(preferredId);
        } else {
            if (style.containsKey("styleId")) { // NOI18N
                id = new StyleId((JSONObject)style.get("styleId")); // NOI18N
            } else if (style.containsKey("styleSheetId")) { // NOI18N
                id = new StyleId((String)style.get("styleSheetId")); // NOI18N
            } else {
                id = null;
            }
        }
        JSONArray cssProperties = (JSONArray)style.get("cssProperties"); // NOI18N
        properties = new ArrayList<Property>(cssProperties.size());
        for (Object o : cssProperties) {
            JSONObject cssProperty = (JSONObject)o;
            Property property = new Property(cssProperty);
            properties.add(property);
        }
        text = (String)style.get("cssText"); // NOI18N
        if (style.containsKey("range")) { // NOI18N
            range = new SourceRange((JSONObject)style.get("range")); // NOI18N
        }
    }

    /**
     * Returns the identifier of the style.
     *
     * @return identifier of the style.
     */
    public StyleId getId() {
        return id;
    }

    /**
     * Returns the properties in the style.
     *
     * @return properties in the style.
     */
    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns the style declaration text.
     *
     * @return style declaration text or {@code null} if it is not available.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the style declaration range in the enclosing stylesheet.
     *
     * @return style declaration range in the enclosing stylesheet or
     * {@code null} if this information is not available.
     */
    public SourceRange getRange() {
        return range;
    }

}
