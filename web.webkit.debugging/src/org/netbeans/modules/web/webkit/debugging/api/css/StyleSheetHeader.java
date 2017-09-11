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

import org.json.simple.JSONObject;

/**
 * CSS stylesheet meta-information.
 *
 * @author Jan Stola
 */
public class StyleSheetHeader {
    /** Stylesheet identifier. */
    private final String styleSheetId;
    /** Owner frame identifier. */
    private final String frameId;
    /** Stylesheet resource URL. */
    private final String sourceURL;
    /** Stylesheet origin. */
    private final StyleSheetOrigin origin;
    /** Stylesheet title. */
    private final String title;
    /** Determines whether the stylesheet is disabled. */
    private final boolean disabled;

    /**
     * Creates a new {@code StyleSheetHeader} that corresponds to the given JSONObject.
     *
     * @param header JSONObject describing the stylesheet header.
     */
    StyleSheetHeader(JSONObject header) {
        styleSheetId = (String)header.get("styleSheetId"); // NOI18N
        frameId = (String)header.get("frameId"); // NOI18N
        sourceURL = (String)header.get("sourceURL"); // NOI18N
        String originCode = (String)header.get("origin"); // NOI18N
        origin = StyleSheetOrigin.forCode(originCode); // NOI18N
        title = (String)header.get("title"); // NOI18N
        disabled = (Boolean)header.get("disabled"); // NOI18N
    }

    /**
     * Returns identifier of the stylesheet.
     *
     * @return identifier of the stylesheet.
     */
    public String getStyleSheetId() {
        return styleSheetId;
    }

    /**
     * Returns identifier of the owner frame.
     *
     * @return identifier of the owner frame.
     */
    public String getFrameId() {
        return frameId;
    }

    /**
     * Returns resource URL of the stylesheet.
     *
     * @return resource URL of the stylesheet.
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * Returns origin of the stylesheet.
     *
     * @return origin of the stylesheet.
     */
    public StyleSheetOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns title of the stylesheet.
     *
     * @return title of the stylesheet.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Determines whether the stylesheet is disabled or not.
     *
     * @return {@code true} when the stylesheet is disabled, returns
     * {@code false} otherwise.
     */
    public boolean isDisabled() {
        return disabled;
    }

}
