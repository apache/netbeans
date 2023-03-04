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

package org.netbeans.modules.html.parser;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.model.ElementDescriptor;

/**
 *  
 * @author marekfukala
 */
public class DocumentationTest extends NbTestCase {

    public DocumentationTest(String name) {
        super(name);
    }

     public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new DocumentationTest("testSectioningPattern"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlDocumentation.setupDocumentationForUnitTests();
    }

    public void testDocZipPresence() throws IOException {
        URL zipUrl = HtmlDocumentation.getZipURL();
        assertNotNull(zipUrl);

        URLConnection con = zipUrl.openConnection();
        assertNotNull(con);
    }

    public void testResolveLink() throws IOException {
        URL url = HtmlDocumentation.getDefault().resolveLink(ElementDescriptor.ARTICLE.getHelpLink());
        assertNotNull(url);

        String content = HtmlDocumentation.getContentAsString(url, null);
        assertTrue(content.startsWith("<!DOCTYPE html>"));
    }

    public void testSectioningPattern() {
//        System.out.println(Documentation.SECTIONS_PATTERN_CODE);


        String code = "w<h1 id=\"mojeid\">xxx<h1 id=\"jeho\">sew";
        Matcher m = HtmlDocumentation.SECTIONS_PATTERN.matcher(code);
        int i = 0;
        while(m.find()) {
            i++;
//            System.out.println(m.group(1));
        }
        assertEquals(2, i);

        code = "<h4 id=\"the-article-element\" id=\"ddd\">";
        m = HtmlDocumentation.SECTIONS_PATTERN.matcher(code);
        i = 0;
        while(m.find()) {
            i++;
//            System.out.println(m.group(1));
        }
        assertEquals(1, i);


    }
    
    public void testSectionContent() throws IOException {
        URL url = HtmlDocumentation.getDefault().resolveLink(ElementDescriptor.ARTICLE.getHelpLink());
        assertNotNull(url);
        String content = HtmlDocumentation.getDefault().getHelpContent(url);
        assertTrue(content.substring(0, 200).indexOf("<h4 id=the-article-element>") != -1);

    }

}