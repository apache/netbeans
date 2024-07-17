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

package org.netbeans.modules.web.jspparser;


import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;

/**
 * @author pj97932
 * @version 1.0
 */
public class FastScanTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FastScanTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);
    }
    
    public void testPage1() throws Exception {
        doFastScanTest("jspparser-data/wmroot", "subdir/Page1.jsp", new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"));
    }
    
    public void testEncodingBug198637() throws Exception {
        doFastScanTest("project2/web", "encoding198637.jsp", new JspParserAPI.JspOpenInfo(false, "utf-8"));
    }
    
    public void doFastScanTest(String wmRootPath, String path, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        FileObject wmRoot = TestUtil.getFileInWorkDir(wmRootPath, this);
        FileObject tempFile = wmRoot.getFileObject(path);
        assertNotNull("Path: " + wmRoot + "/" + path, tempFile);
        parseIt(wmRoot, tempFile, correctInfo);
    }
    
    private void parseIt(FileObject root, FileObject jspFile, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        log("calling parseIt, root: " + root + "  file: " + jspFile);
        JspParserAPI api = JspParserFactory.getJspParser();
        JspParserAPI.JspOpenInfo info = api.getJspOpenInfo(jspFile, TestUtil.getWebModule(jspFile), false);
        log("file: " + jspFile + "   enc: " + info.getEncoding() + "   isXML: " + info.isXmlSyntax());
        assertEquals(correctInfo, info);
    }
}
