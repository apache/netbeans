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

package org.netbeans.modules.html.editor.api.gsf;

import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;

/**
 *
 * @author marekfukala
 */
public class HtmlParserResultTest extends NbTestCase {
    
    public HtmlParserResultTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetHtmlVersion_html401_source() throws Exception {
        String code = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head><title>xxx</title></head><body>yyy</body></html>";
        assertEquals(HtmlVersion.HTML41_TRANSATIONAL, getHtmlSourceVersion(code));
    }

    public void testGetHtmlVersion_html5_source() throws Exception {
        String code = "<!doctype html><html><head><title>xxx</title></head><body>yyy</body></html>";
        assertEquals(HtmlVersion.HTML5, getHtmlSourceVersion(code));
    }

    private HtmlVersion getHtmlSourceVersion(CharSequence code) {
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(new HtmlSource(code)).analyze();

        HtmlParserResult cslresult = HtmlParserResultAccessor.get().createInstance(result);
        assertNotNull(cslresult);

        HtmlVersion version = cslresult.getHtmlVersion();
        assertNotNull(version);

        return version;
    }

}
