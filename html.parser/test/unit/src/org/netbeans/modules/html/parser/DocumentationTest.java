/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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