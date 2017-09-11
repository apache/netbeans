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
    static final protected int defaultSectionCount = 2;

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
