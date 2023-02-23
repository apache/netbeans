/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.jsp.lexer;

import java.util.Map;

/** Holds data relevant to the JSP coloring for one JSP page. 
 *
 * @author Marek Fukala
 */
public final class JspParseData {

    private Map<String, String> prefixMap;
    private boolean isELIgnored, isXMLSyntax;
    private boolean initialized;

    public JspParseData(Map<String, String> prefixMap, boolean isELIgnored, boolean isXMLSyntax, boolean isInitialized) {
        this.prefixMap = prefixMap;
        this.isELIgnored = isELIgnored;
        this.isXMLSyntax = isXMLSyntax;
        this.initialized = isInitialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean initialized() {
        boolean oldVal = initialized;
        this.initialized = true;
        return oldVal;
    }

    /** Updates coloring data. The update is initiated by parser successfuly finished parsing. */
    public void updateParseData(Map<String, String> prefixMap, boolean isELIgnored, boolean isXMLSyntax) {
        this.prefixMap = prefixMap;
        this.isELIgnored = isELIgnored;
        this.isXMLSyntax = isXMLSyntax;
    }

    /** Returns true if the given tag library prefix is known in this page.
     */
    public boolean isTagLibRegistered(CharSequence prefix) {
        if (prefixMap == null) {
            return false;
        }
        return prefixMap.containsKey(prefix.toString());
    }

    /** Returns true if the EL is ignored in this page.
     */
    public boolean isELIgnored() {
        return isELIgnored;
    }

    /** Returns true if the page is in xml syntax (JSP Documnet). 
     * If the page is in standard syntax, returns false.
     */
    public boolean isXMLSyntax() {
        return isXMLSyntax;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("JspParseData[prefixes=");
        if (prefixMap != null) {
            prefixMap.forEach((prefix, v) -> {
                buf.append(prefix);
                buf.append('-');
                buf.append(v);
                buf.append(',');
            });
        } else {
            buf.append("null");
        }
        buf.append("; isELIgnored=");
        buf.append(isELIgnored());
        buf.append("; isXMLSyntax=");
        buf.append(isXMLSyntax());
        buf.append(')');

        return buf.toString();
    }
}
