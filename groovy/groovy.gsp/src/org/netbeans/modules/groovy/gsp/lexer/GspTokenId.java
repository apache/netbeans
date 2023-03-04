/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
