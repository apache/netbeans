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

package org.netbeans.modules.web.core.jsploader.api;

/**
 * Defines cookie which supports parsing of jsp file.
 * Note: Do not implement this cookie, use the factory only.
 * (The same contract like for window system API interfaces, you as
 * provider can later add methods to it, the client is not implementor).
 * @author Petr Pisl, Tomas Mysik
 */
public interface TagLibParseCookie extends org.openide.nodes.Node.Cookie {
    /**
     * Get data important for opening the page in the editor, e.g. whether the page is in classic
     * or XML syntax, or what is the file encoding.
     * @param preferCurrent <code>true</code> if the returned value should be actual and not from a cache.
     * @param useEditor <code>true</code> if the returned value should be taken from editor and from the disk.
     * @return {@link OpenInfo} instance.
     * @since 2.0
     */
    public OpenInfo getOpenInfo(boolean preferCurrent, boolean useEditor);

    
    /** Sets document dirty flag after modification. 
     */
    public void setDocumentDirty(boolean b);
    
    /** Checks if the document is dirty - modified after last parsing
     */
    public boolean isDocumentDirty();
    
    /** @return JSP auto parsing task. 
     */
    public org.openide.util.Task autoParse();
    
    /**
     * Class representing data important for opening the page in the editor, e.g. whether the page is in classic
     * or XML syntax, or what is the file encoding. Implementations of this interface are returned by
     * {@link TagLibParseCookie#getCachedOpenInfo TagLibParseCookie.getCachedOpenInfo()}.
     * @since 2.0
     */
    public static final class OpenInfo {
        private final boolean xmlSyntax;
        private final String encoding;

        private OpenInfo(boolean xmlSyntax, String encoding) {
            this.xmlSyntax = xmlSyntax;
            this.encoding = encoding;
        }

        /**
         * Factory method for creating new {@link OpenInfo} instance.
         * @param xmlSyntax <code>true</code> if the page is in XML syntax, <code>false</code> otherwise.
         * @param encoding page encoding.
         * @return new {@link OpenInfo} instance.
         */
        public static OpenInfo create(boolean xmlSyntax, String encoding) {
            return new OpenInfo(xmlSyntax, encoding);
        }

        /**
         * Check whether the file is in XML encoding.
         * @return <code>true</code> if the page is in XML syntax, <code>false</code> otherwise.
         */
        public boolean isXmlSyntax() {
            return xmlSyntax;
        }

        /**
         * Get the encoding of the file.
         * @return encoding of the file.
         */
        public String getEncoding() {
            return encoding;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(OpenInfo.class.getName());
            sb.append("[xmlSyntax: ");
            sb.append(xmlSyntax);
            sb.append(", encoding: ");
            sb.append(encoding);
            sb.append("]");
            return sb.toString();
        }
    }
}
