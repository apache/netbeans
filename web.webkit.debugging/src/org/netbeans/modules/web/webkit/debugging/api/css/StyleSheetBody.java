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
 * Content of CSS stylesheet.
 *
 * @author Jan Stola
 */
public class StyleSheetBody {
    /** Identifier of the stylesheet. */
    private final String styleSheetId;
    /** Rules of the stylesheet. */
    private final List<Rule> rules;
    /** Text of the stylesheet (if available). */
    private final String text;
    /** Header of the style-sheet. */
    private final StyleSheetHeader styleSheetHeader;

    /**
     * Creates a new {@code StyleSheetBody} that corresponds to the given JSONObject.
     *
     * @param header header of the style-sheet.
     * @param body JSONObject describing the body of the stylesheet.
     */
    StyleSheetBody(StyleSheetHeader header, JSONObject body) {
        styleSheetHeader = header;
        styleSheetId = (String)body.get("styleSheetId"); // NOI18N
        JSONArray cssRules = (JSONArray)body.get("rules"); // NOI18N
        rules = new ArrayList<Rule>(cssRules.size());
        for (Object o : cssRules) {
            JSONObject cssRule = (JSONObject)o;
            Rule rule = new Rule(cssRule);
            rule.setParentStyleSheet(this);
            rules.add(rule);
        }
        text = (String)body.get("text"); // NOI18N
    }

    /**
     * Creates a new {@code StyleSheetBody}.
     *
     * @param header header of the style-sheet.
     * @param text text of the style-sheet.
     */
    StyleSheetBody(StyleSheetHeader header, String text) {
        this.styleSheetHeader = header;
        this.styleSheetId = header.getStyleSheetId();
        this.text = text;
        this.rules = Collections.EMPTY_LIST;
    }

    /**
     * Returns the identifier of the stylesheet.
     *
     * @return identifier of the stylesheet.
     */
    public String getStyleSheetId() {
        return styleSheetId;
    }

    /**
     * Returns the header of the style-sheet.
     * 
     * @return header of the style-sheet.
     */
    public StyleSheetHeader getHeader() {
        return styleSheetHeader;
    }

    /**
     * Returns the rules of the stylesheet.
     *
     * @return
     */
    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Returns the text of the stylesheet.
     *
     * @return text of the stylesheet or {@code null} if it is not available.
     */
    public String getText() {
        return text;
    }

}
