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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Color;
import org.json.simple.JSONObject;

/**
 * Configuration data for the highlighting of page elements.
 * See {@code DOM.HighlightConfig} in WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class HighlightConfig {
    /** Determines whether the info tooltip should be shown. */
    public boolean showInfo;
    /** Color used to paint the content box, can be {@code null}. */
    public Color contentColor;
    /** Color used to paint the padding, can be {@code null}. */
    public Color paddingColor;
    /** Color used to paint the border, can be {@code null}. */
    public Color borderColor;
    /** Color used to paint the margin, can be {@code null}. */
    public Color marginColor;

    /**
     * Converts this highlight configuration into {@code DOM.HighlightConfig} format.
     * 
     * @return this highlight configuration in {@code DOM.HighlightConfig} format.
     */
    JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("showInfo", showInfo); // NOI18N
        if (contentColor != null) {
            object.put("contentColor", colorToRGBA(contentColor)); // NOI18N
        }
        if (paddingColor != null) {
            object.put("paddingColor", colorToRGBA(paddingColor)); // NOI18N
        }
        if (borderColor != null) {
            object.put("borderColor", colorToRGBA(borderColor)); // NOI18N
        }
        if (marginColor != null) {
            object.put("marginColor", colorToRGBA(marginColor)); // NOI18N
        }
        return object;
    }

    /**
     * Converts the given color into a JSONObject in {@code DOM.RGBA} format.
     * 
     * @param color color to convert.
     * @return color in {@code DOM.RGBA} format.
     */
    static JSONObject colorToRGBA(Color color) {
        JSONObject object = new JSONObject();
        object.put("r", color.getRed()); // NOI18N
        object.put("g", color.getGreen()); // NOI18N
        object.put("b", color.getBlue()); // NOI18N
        if (color.getAlpha() != 255) {
            object.put("a", color.getAlpha()/255.0); // NOI18N
        }
        return object;
    }
    
}
