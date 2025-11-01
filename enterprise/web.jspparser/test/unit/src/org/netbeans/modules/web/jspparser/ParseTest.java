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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.filesystems.FileObject;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * @author pj97932
 * @version 1.0
 */
public class ParseTest extends NbTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ParseTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);
    }

    // TODO: temporarily disabled because of JDK 5 and 6 differences
    public void disabledTestAnalysisBasicJspx() throws Exception {
        parserTestInProject("project2", "/web/basic.jspx");
    }

    public void testAnalysisMain() throws Exception {
        parserTestInProject("project2", "/web/main.jsp");
    }

    public void testAnalysisMainJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/main.jsp");
    }

    public void testAnalysisBean() throws Exception {
        parserTestInProject("project2", "/web/more_for_test/bean.jsp");
    }

    public void testAnalysisBeanJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/more_for_test/bean.jsp");
    }

    public void testAnalysisTagLinkList() throws Exception {
        parserTestInProject("project2", "/web/WEB-INF/tags/linklist.tag");
    }

    public void testAnalysisTagLinkListJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/WEB-INF/tags/linklist.tag");
    }

    public void testAnalysisFaulty() throws Exception {
        parserTestInProject("project2", "/web/faulty.jsp");
    }

    public void testAnalysisFaultyJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/faulty.jsp");
    }

    public void testAnalysisOutsideWM() throws Exception {
        parserTestInProject("project2", "/outside/outsidewm.jsp");
    }

    public void testAnalysisOutsideWMJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/outside/outsidewm.jsp");
    }

    // TODO: temporarily disabled because of JDK 5 and 6 differences
    public void disableTestAnalysisTagLibFromTagFiles_1_6() throws Exception {
            parserTestInProject("project2", "/web/testTagLibs.jsp");
    }

    public void testJSPInclude() throws Exception {
        parserTestInProject("project2", "/web/jspInclude.jsp");
    }

    public void testJSPIncludeJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/jspInclude.jsp");
    }

    public void testInclude() throws Exception {
        parserTestInProject("project2", "/web/include.jsp");

    }

    public void testIncludeJakarta() throws Exception {
        parserTestInProject("project2_jakarta", "/web/include.jsp");

    }

    public void testIncludePreludeCoda() throws Exception {
        JspParserAPI.ParseResult result = parserTestInProject("project2", "/web/includePreludeCoda.jsp");
        log("Prelude: " + result.getPageInfo().getIncludePrelude());
        log("Coda: " + result.getPageInfo().getIncludeCoda());
    }

    public void testIncludePreludeCodaJakarta() throws Exception {
        JspParserAPI.ParseResult result = parserTestInProject("project2_jakarta", "/web/includePreludeCoda.jsp");
        log("Prelude: " + result.getPageInfo().getIncludePrelude());
        log("Coda: " + result.getPageInfo().getIncludeCoda());
    }

    public JspParserAPI.ParseResult parserTestInProject(String projectFolderName, String pagePath) throws Exception {
        FileObject jspFo = TestUtil.getProjectFile(this, projectFolderName, pagePath);
        WebModule webModule = TestUtil.getWebModule(jspFo);
        // every test should have new environment (parser) to not influence the other one (by running in various order)
        JspParserAPI jspParser = new JspParserImpl();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        assertNotNull("The result from the parser was not obtained.", result);

        File goldenF = null;
        File outFile = null;
        try {
            goldenF = getGoldenFile();
        } finally {
            String fName = (goldenF == null) ? ("temp" + fileNr++ + ".result") : getBrotherFile(goldenF, "result");
            outFile = new File(getWorkDir(), fName);
            writeOutResult(result, outFile);
        }

        assertNotNull(outFile);
        try {
            assertFile(outFile, goldenF, getWorkDir());
        } catch (Error e) {
            System.out.println("diff -u " + goldenF + " " + outFile);
            throw e;
        }

        return result;
    }

    private static int fileNr = 1;

    private void writeOutResult(JspParserAPI.ParseResult result, File outFile) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        pw.write(result.toString());
        pw.close();
    }

    private String getBrotherFile(File f, String ext) {
        String goldenFile = f.getName();
        int i = goldenFile.lastIndexOf('.');
        if (i == -1) {
            i = goldenFile.length();
        }
        return goldenFile.substring(0, i) + "." + ext;
    }
}
