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
