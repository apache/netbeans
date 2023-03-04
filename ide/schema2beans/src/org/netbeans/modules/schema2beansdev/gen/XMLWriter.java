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

package org.netbeans.modules.schema2beansdev.gen;

import java.io.*;
import java.util.*;

public class XMLWriter extends IndentingWriter {
    protected String xmlVersion = "1.0";
    protected String encoding = "UTF-8";
    protected boolean header = true;
    protected Stack tags;

    public int HEADER_SECTION = 0;
    public int BODY_SECTION = 1;
    protected static final int defaultSectionCount = 2;

    public XMLWriter() {
        super(defaultSectionCount);
        privateInit();
    }

    public XMLWriter(boolean header) {
        super(defaultSectionCount);
        this.header = header;
        privateInit();
    }
    
    public XMLWriter(String encoding) {
        super(defaultSectionCount);
        this.encoding = encoding;
        privateInit();
    }
    
    public XMLWriter(String encoding, String xmlVersion) {
        super(defaultSectionCount);
        this.encoding = encoding;
        this.xmlVersion = xmlVersion;
        privateInit();
    }

    /**
     * Insert a custom section after another section.
     * eg:
     *   JavaWriter jw = new JavaWriter();
     *   int SPECIAL_SECTION = jw.insertSectionAfter(jw.CONSTRUCTOR_SECTION);
     */
    public int insertSectionAfter(int sectionNum) {
        insertAdditionalBuffers(sectionNum, 1);
        if (sectionNum < HEADER_SECTION)  ++HEADER_SECTION;
        if (sectionNum < BODY_SECTION)  ++BODY_SECTION;
        return sectionNum + 1;
    }

    public void reset() {
        super.reset();
        privateInit();
    }

    private void privateInit() {
        tags = new Stack();
        try {
            if (header) {
                select(HEADER_SECTION);
                write("<?xml version='");
                write(xmlVersion);
                write("' encoding='");
                write(encoding);
                write("' ?>\n");
            }
            select(BODY_SECTION);
        } catch (IOException e) {
            // This exception should not occur.
            throw new RuntimeException(e);
        }
    }
    
    public void startTag(String tag) throws IOException {
        startTag(null, tag, null, true);
    }

    public void startTag(String tag, boolean finish) throws IOException {
        startTag(null, tag, null, finish);
    }

    public void startTag(String namespace, String tag)
        throws IOException {
        startTag(namespace, tag, null, true);
    }

    public void startTag(String namespace, String tag, boolean finish)
        throws IOException {
        startTag(namespace, tag, null, finish);
    }

    public void startTag(String namespace, String tag, String attributeString)
        throws IOException {
        startTag(namespace, tag, attributeString, true);
    }

    /**
     * @param finish  Whether or not to add the finishing ">", if not then
     *                setFirst(" ") is called anticipating the addition of
     *                attributes.
     * @param attributeString  The attributes.  Make sure to XML escape.
     */
    public void startTag(String namespace, String tag, String attributeString,
                         boolean finish) throws IOException {
        String fullTag;
        if (namespace != null)
            fullTag = namespace+":"+tag;
        else
            fullTag = tag;
        tags.push(fullTag);
        write("<");
        write(fullTag);
        if (attributeString != null) {
            if (!attributeString.startsWith(" "))
                write(" ");
            write(attributeString);
        }
        if (finish)
            finishStartTag();
        else
            setFirst(" ");
        indentRight();
    }

    /**
     * Finish the start tag, and we expect to have children
     * (ie, that means the client will call endTag).
     */
    public void finishStartTag() throws IOException {
        write(">");
    }

    /**
     * @param children  if false, then there are no children, and
     *                  endTag is called automatically.
     */
    public void finishStartTag(boolean children, boolean useCr) throws IOException {
        if (!children) {
            write("/");
            indentLeft();
            tags.pop();
        }
        finishStartTag();
        if (useCr)
            cr();
    }

    public void endTag() throws IOException {
        endTag(true);
    }
    
    public void endTag(boolean useCr) throws IOException {
        indentLeft();
        String fullTag = (String) tags.pop();
        write("</");
        write(fullTag);
        write(">");
        if (useCr)
            cr();
    }
}
