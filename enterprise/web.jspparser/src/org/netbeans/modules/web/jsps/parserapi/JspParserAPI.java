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

package org.netbeans.modules.web.jsps.parserapi;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.jspparser.ContextUtil;

/**
 * @author  pj97932, Tomas Mysik
 * @version
 */
public interface JspParserAPI {

    /** Mode in which some errors (such as error parsing a tag library) are ignored. */
    int ERROR_IGNORE = 1;
    /** Mode in which some errors (such as error parsing a tag library) are reported,
     * but no accurate error description is needed. */
    int ERROR_REPORT_ANY = 2;
    /** Mode in which an accurate description of all errors is required, so an actual attempt to parse all
     * tag libraries is done, so the parser throws a root cause exception. */
    int ERROR_REPORT_ACCURATE = 3;
    
    String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    /** Returns the information necessary for opening a JSP page in the editor.
     * 
     * @param jspFile the page to analyze
     * @param wm web module in whose context to compile
     * @param useEditor whether to use data from the existing open JSP document, or from the file on the disk
     * @return open information, using either the editor, or the file on the disk
     */    
    JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm, boolean useEditor);
    
    /** Analyzes JSP and returns the parsed data about the page.
     * 
     * @param wmRoot root of the web module which gives context to this page,
     *   may be null if the page is not within a web module
     * @param jspFile the page to analyze
     * @param proj project in whose context to compile
     * @param errorReportingMode mode for reporting errors, see above
     * @return Parsing results.
     */    
    JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm, int errorReportingMode);
    
    /** Returns the classloader which loads classes from the given web module 
     * (within a project context).
     */
    URLClassLoader getModuleClassLoader(WebModule wm);
    
    /** Creates a description of a tag library. */
    //public TagLibParseSupport.TagLibData createTagLibData(JspInfo.TagLibraryData info, FileSystem fs);
    
    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. 
     * @param wmRoot the web module for which to return the map
     * @return Map which maps global tag library URI to the location 
     * (resource path) of its tld. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    Map<String, String[]> getTaglibMap(WebModule wm) throws IOException;
    
    /**
     * Add listener which will be called after TLD, TAG files are changed.
     * @param listener TLD listener.
     * @since 3.1
     */
    void addTldChangeListener(TldChangeListener listener);

    /**
     * Remove TLD listener.
     * @param listener TLD listener.
     * @since 3.1
     */
    void removeTldChangeListener(TldChangeListener listener);
    
    /** This class represents a result of parsing. It indicates either success
     * or failure. In case of success, provides information about the parsed page,
     * in case of failure, provides information about parsing errors.
     */
    public static final class ParseResult {
        
        protected final PageInfo pageInfo;
        protected final Node.Nodes nodes;
        protected final JspParserAPI.ErrorDescriptor[] errors;
        protected final boolean parsedOK;
       
        /** Creates a new ParseResult in case of parse success.
         * @param pageInfo information about the parsed page (from Jasper)
         * @param node exact structure of the  (from Jasper)
         */
        public ParseResult(PageInfo pageInfo, Node.Nodes nodes) {
            this(pageInfo, nodes, null);
        }
        
        /** Creates a new ParseResult in case of parse failure.
         * @param errors information about parse errors
         */
        public ParseResult(JspParserAPI.ErrorDescriptor[] errors) {
            this(null, null, errors);
        }
        
        /** Creates a new ParseResult. If the errors array is null or empty,
         *  the parse is considered successful.
         * @param pageInfo information about the parsed page (from Jasper), may be null
         * @param node exact structure of the  (from Jasper), may be null
         * @param errors information about parse errors, or null, if parsing was successful
         */
        public ParseResult(PageInfo pageInfo, Node.Nodes nodes, JspParserAPI.ErrorDescriptor[] errors) {
            this.pageInfo = pageInfo;
            this.nodes = nodes;
            this.errors = errors;
            this.parsedOK = ((errors == null) || (errors.length == 0));
        }
        
        /** Indicates success or failure of parsing.
         */
        public boolean isParsingSuccess() {
            return parsedOK;
        }
        
        /** Returns all global information about the parsed page.
         *  @exception IllegalStateException if parsing failed
         */
        public PageInfo getPageInfo() {
            return pageInfo;
        }
        
        /** Returns the hierarchical structure of the page.
         *  @exception IllegalStateException if parsing failed
         */
        public Node.Nodes getNodes() {
            return nodes;
        }
        
        /** Returns information about the parse errors if parsing failed.
         *  @exception IllegalStateException if parsing succeeded
         */
        public JspParserAPI.ErrorDescriptor[] getErrors() {
            if (!(parsedOK)) {
                return errors;
            }
            throw new IllegalStateException();
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("--------- JspParserAPI.parseResult(), success: "); // NOI18N
            result.append(isParsingSuccess());
            result.append("\n"); // NOI18N
            if (pageInfo != null) {
                result.append(" ---- PAGEINFO\n"); // NOI18N
                result.append(pageInfo.toString());
            }
            if (nodes != null) {
                result.append("\n ---- NODES\n"); // NOI18N
                result.append(nodes.toString());
                result.append("\n"); // NOI18N
            }
            if (!isParsingSuccess()) {
                result.append("\n ---- ERRORS\n"); // NOI18N
                for (int i = 0; i < errors.length; i++) {
                    result.append(errors[i].toString());
                }
            }
            return result.toString();
        }
    }
    
    /** Contains data important for opening the page
     * in the editor, e.g. whether the page is in classic
     * or XML syntax, or what is the file encoding.
     */
    public static final class JspOpenInfo {
        
        private final boolean isXml;
        private final String encoding;
        
        public JspOpenInfo(boolean isXml, String encoding) {
            this.isXml = isXml;
            this.encoding = encoding;
        }
        
        public boolean isXmlSyntax() {
            return isXml;
        }
        
        public String getEncoding() {
            return encoding;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof JspOpenInfo) {
                JspOpenInfo openInfo2 = (JspOpenInfo) o;
                return getEncoding().equals(openInfo2.getEncoding())
                        && isXmlSyntax() == openInfo2.isXmlSyntax();
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return encoding.hashCode() + (isXml ? 1 : 0);
        }
        
        @Override
        public String toString() {
            return super.toString() + " [isXml: " + isXml + ", encoding: " + encoding + "]"; // NOI18N
        }
        
    }

    /** Represents a description of a parse error.
     */
    public static final class ErrorDescriptor {

        protected final FileObject wmRoot;
        protected final FileObject source;
        protected final int line;
        protected final int column;
        protected final String errorMessage;
        protected final String referenceText;

        /** Creates a new ErrorDescriptor. 
         * @param wmRoot the web module in which the error occurs. May be null in some (unusual) cases.
         * @param source the file in which the error occurred. This may be different from the page that was 
         *  originally compiled/parsed, if this is a page segment.
         * @param line line number on which the error occurred
         * @param column column number on which the error occurred
         * @param errorMessage message containing the description of the error
         * @param rererenceText a piece of code (line) that contains the error. May be empty.
         */
        public ErrorDescriptor(FileObject wmRoot, FileObject source, int line, int column, String errorMessage,
                String referenceText) {
            this.wmRoot = wmRoot;
            this.source = source;
            this.line = line;
            this.column = column;
            this.errorMessage = errorMessage;
            this.referenceText = referenceText;
        }

        /** Returns a file containing the error. */
        public FileObject getSource() {
            return source;
        }

        /** Get the line of the error. */
        public int getLine() {
            return line;
        }

        /** Get the column of the error. */
        public int getColumn() {
            return column;
        }

        /** Get the error message associated with the error. */
        public String getErrorMessage() {
            return errorMessage;
        }

        /** Get the string which contains the error (i.e. contents of the line containing the error. */
        public String getReferenceText() {
            return referenceText;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("ERROR in ") // NOI18N
                  .append(getSourcePath())
                  .append(" at [") // NOI18N
                  .append(getLine())
                  .append(", ") // NOI18N
                  .append(getColumn())
                  .append("] ") // NOI18N
                  .append(getErrorMessage())
                  .append("\n") // NOI18N
                  .append(getReferenceText())
                  .append("\n"); // NOI18N
            return result.toString();
        }
        
        private String getSourcePath() {
            if (wmRoot == null) {
                return getSource().getNameExt();
            }
            return ContextUtil.findRelativeContextPath(wmRoot, getSource());
        }
    }
}
