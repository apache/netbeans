/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.gsp.lexer;

import org.netbeans.api.lexer.TokenId;

/**
 * Enum containing of the GSP lexer tokens. Possible tags in GSP that are processing
 * by the Lexer are:
 * 1) Opening tags:
 *      ${     or  %{    or  \${    or &lt%
 *      &lt%-- or  &lt%@  or  &lt%=
 *
 * 2) Closing tags:
 *         }   or  }%  or  %>   or  />  or  --%>
 *
 * @author Martin Janicek
 */
public enum GspTokenId implements TokenId {

    HTML("html"),
    WHITESPACE("whitespace"),
    ERROR("error"),
 
    // GTags tokens
    GTAG_OPENING_START("gtag"),                      // <g:    ...
    GTAG_OPENING_NAME("gtag"),                       // <g:if  ...
    GTAG_OPENING_END("gtag"),                        // <g:  ... >
    GTAG_ATTRIBUTE_NAME("attribute-name"),           // <g: someAttribute=
    GTAG_ATTRIBUTE_VALUE("attribute-value"),         // <g: someAttribute="value"
    GTAG_INDEPENDENT_END("gtag"),                    // <g:    ... />
    GTAG_CLOSING_START("gtag"),                      // </g:   ...
    GTAG_CLOSING_NAME("gtag"),                       // </g:if ...
    GTAG_CLOSING_END("gtag"),                        // </g:   ... >

    // Comments allowed in GSP files
    COMMENT_JSP_STYLE_START("comment"),              // <%--
    COMMENT_JSP_STYLE_CONTENT("comment"),            // <%-- ...
    COMMENT_JSP_STYLE_END("comment"),                // <%-- ... --%>

    COMMENT_HTML_STYLE_START("comment"),             // <!--
    COMMENT_HTML_STYLE_CONTENT("comment"),           // <!-- ...
    COMMENT_HTML_STYLE_END("comment"),               // <!-- ... --!>

    COMMENT_GSP_STYLE_START("comment"),              // %{--
    COMMENT_GSP_STYLE_CONTENT("comment"),            // %{-- ...
    COMMENT_GSP_STYLE_END("comment"),                // %{-- ... --}%

    // Page directive
    PAGE_DIRECTIVE_START("start-delimiter"),         // <%@  ...
    PAGE_DIRECTIVE_NAME("directive"),                // <%@page import ... %> or <%@page contentType ... %>
    PAGE_ATTRIBUTE_NAME("attribute-name"),           // <%@page someAttribute=
    PAGE_ATTRIBUTE_VALUE("attribute-value"),         // <%@page someAttribute="value"
    PAGE_DIRECTIVE_END("end-delimiter"),             // <%@  ... %>

    // Scriptlet output value
    SCRIPTLET_OUTPUT_VALUE_START("start-delimiter"), // <%=  ...
    SCRIPTLET_OUTPUT_VALUE_CONTENT("groovy"),        // <%=  "something"
    SCRIPTLET_OUTPUT_VALUE_END("end-delimiter"),     // <%=  ... %>

    // Scriptlets
    SCRIPTLET_START("start-delimiter"),              // <%  ...
    SCRIPTLET_CONTENT("groovy"),                     // <%  "something"
    SCRIPTLET_END("end-delimiter"),                  // <%  ... %>

    // GStrings
    GSTRING_START("start-delimiter"),                // ${  ...  or   \${  ...
    GSTRING_CONTENT("groovy"),                       // ${  "something"
    GSTRING_END("end-delimiter");                    // ${  ...  }


    private final String primaryCategory;


    private GspTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    
    /**
     * Finds out if the parameter is a token representing embedded Groovy content.
     * Typically that code might be between ${ ..some code.. }
     *
     * @param tokenID tokenID we want to check
     * @return true if the tokenID is Groovy content, false otherwise
     */
    public boolean isGroovyContent() {
        return checkPrimaryCategory("groovy"); // NOI18N
    }

    public boolean isComment() {
        return checkPrimaryCategory("comment"); // NOI18N
    }

    public boolean isDelimiter() {
        return isStartDelimiter() || isEndDelimiter();
    }

    public boolean isStartDelimiter() {
        return checkPrimaryCategory("start-delimiter"); // NOI18N
    }

    public boolean isEndDelimiter() {
        return checkPrimaryCategory("end-delimiter"); // NOI18N
    }

    private boolean checkPrimaryCategory(String category) {
        if (category.equals(primaryCategory)) { // NOI18N
            return true;
        }
        return false;
    }
}
