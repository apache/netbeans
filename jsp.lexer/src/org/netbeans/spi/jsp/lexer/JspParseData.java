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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            for (String prefix : prefixMap.keySet()) {
                buf.append(prefix);
                buf.append('-');
                buf.append(prefixMap.get(prefix));
                buf.append(',');
            }
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
