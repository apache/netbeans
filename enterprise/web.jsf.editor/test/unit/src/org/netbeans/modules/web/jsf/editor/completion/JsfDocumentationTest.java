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

package org.netbeans.modules.web.jsf.editor.completion;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *  
 * @author marekfukala
 */
public class JsfDocumentationTest extends NbTestCase {

    public JsfDocumentationTest(String name) {
        super(name);
    }

     public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JsfDocumentationTest("testSectioningPattern"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));//NOI18N
    }

    public void testDocZipPresence() throws IOException {
        URL zipUrl = JsfDocumentation.getZipURL();
        assertNotNull(zipUrl);

        URLConnection con = zipUrl.openConnection();
        assertNotNull(con);
    }

    public void testResolveLink() throws IOException {
        //from facelets descriptor help to a linked jsf-api-docs entry
        URL url = JsfDocumentation.getDefault().resolveLink(null, "../../../javadocs/javax/faces/component/UIViewParameter.html");
        assertNotNull(url);

        String content = JsfDocumentation.getContentAsString(url, null);
        assertNotNull(content);

        //and between jsf-api-docs entries
        URL url2 = JsfDocumentation.getDefault().resolveLink(url, "../../../javax/faces/component/UIComponent.html");
        assertNotNull(url2);

        String content2 = JsfDocumentation.getContentAsString(url2, null);
        assertNotNull(content2);

        //test absolute link
        URL aurl = new URL("http://oracle.com/index.html");
        URL url3 = JsfDocumentation.getDefault().resolveLink(url2, aurl.toExternalForm());

        assertEquals(aurl, url3);
        
    }

    public void testResolveFromIndexToHelp() {
        URL index = JsfDocumentation.getDefault().resolveLink(null, "index-all.html");
        assertNotNull(index);

        URL help = JsfDocumentation.getDefault().resolveLink(index, "./help-doc.html");
        assertNotNull(help);

        String helpContent = JsfDocumentation.getContentAsString(help, null);
        assertNotNull(helpContent);
        
    }
   

}