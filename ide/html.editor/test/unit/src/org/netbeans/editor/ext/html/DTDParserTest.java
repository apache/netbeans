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
package org.netbeans.editor.ext.html;

import java.io.IOException;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Content;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Element;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class DTDParserTest extends TestBase {

    private static final String HTML401_STRICT = "-//W3C//DTD HTML 4.01//EN";  // NOI18N
    private static final String HTML401_TRANS = "-//W3C//DTD HTML 4.01 Transitional//EN"; //NOI18N
    private static final String XHTML_DOCTYPE = "-//W3C//DTD XHTML 1.0 Strict//EN";  // NOI18N

    public DTDParserTest(String testName) {
        super(testName);
    }

    public static Test xsuite() throws IOException {
        TestSuite suite = new TestSuite();
        suite.addTest(new DTDParserTest("testReduceSgmlAndGroups"));
        return suite;
    }

    public void testDTDParserXHTML() {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(XHTML_DOCTYPE, null);
        assertNotNull(dtd);

        Element htmlElement = dtd.getElement("html");
        assertNotNull(htmlElement);
        Content htmlc = htmlElement.getContentModel().getContent();
        assertNotNull(htmlc);
        assertEquals(1, htmlc.getPossibleElements().size());

        Element headElement = dtd.getElement("head");
        assertNotNull(headElement);

        Content c = htmlElement.getContentModel().getContent();
        assertTrue(c.getPossibleElements().contains(headElement));

        c = c.reduce("head");

        Element bodyElement = dtd.getElement("body");
        assertNotNull(bodyElement);

        assertTrue(c.getPossibleElements().contains(bodyElement));
    }

    public void testDTDParser_HTML_BODY_Elements() {
        testDTDParser_HTML_BODY_Elements(HTML401_STRICT);
        testDTDParser_HTML_BODY_Elements(HTML401_TRANS);
    }

    private void testDTDParser_HTML_BODY_Elements(String doctype) {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(doctype, null);
        assertNotNull(dtd);

        Element htmlElement = dtd.getElement("HTML");
        assertNotNull(htmlElement);

        Element headElement = dtd.getElement("HEAD");
        assertNotNull(headElement);

        Content c = htmlElement.getContentModel().getContent();
        assertTrue(c.getPossibleElements().contains(headElement));

        c = c.reduce("HEAD");

        Element bodyElement = dtd.getElement("BODY");
        assertNotNull(bodyElement);

        assertTrue(c.getPossibleElements().contains(bodyElement));
    }

    public void testTable() {
        testTable(HTML401_STRICT);
        testTable(HTML401_TRANS);
    }

    private void testTable(String doctype) {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(doctype, null);
        assertNotNull(dtd);

        Element el = dtd.getElement("TABLE");
        assertNotNull(el);
        Content c = el.getContentModel().getContent();

        assertNotNull(c);

    }

    public void testReduceSgmlAmpGroups() {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(HTML401_TRANS, null);
        assertNotNull(dtd);
        Element el = dtd.getElement("HEAD");
        assertNotNull(el);

        Content orig = el.getContentModel().getContent();
        Content c = orig;
        assertNotNull(c);

        System.out.println(c);

        //try reducing the elements in the declaration order (TITLE & ISINDEX? & BASE?)
        c = c.reduce("TITLE");
        assertNotNull(c);
        assertNotEqual(DTD.Content.EMPTY_CONTENT, c);

        assertNull(c.reduce("TITLE")); //can't reduce twice

        c = c.reduce("ISINDEX");
        assertNotNull(c);

        c = c.reduce("BASE");
        assertNotNull(c);

        assertEquals(DTD.Content.EMPTY_CONTENT, c);

        //now try different order, order doesn't play role in & groups
        c = orig;

        c = c.reduce("BASE");
        assertNotNull(c);

        assertNotEqual(DTD.Content.EMPTY_CONTENT, c);

        c = c.reduce("ISINDEX");
        assertNotNull(c);
        assertNotEqual(DTD.Content.EMPTY_CONTENT, c);

        c = c.reduce("TITLE");
        assertNotNull(c);
        assertEquals(DTD.Content.EMPTY_CONTENT, c);


    }

    public void testOption() {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(HTML401_STRICT, null);
        assertNotNull(dtd);

        Element el = dtd.getElement("OPTION");
        assertNotNull(el);
        assertFalse(el.isEmpty());

    }
    
    public void testFrameset() {
        DTD dtd = org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD("-//W3C//DTD HTML 4.01 Frameset//EN", null);
        assertNotNull(dtd);
    }

    private static void assertNotEqual(Object o1, Object o2) {
        if(o1.equals(o2)) {
            throw new AssertionFailedError("Objects are unexpectedly equal: " + o1.toString());
        }
    }

//      private void dumpContent(Content c) {
//          for(Object obj : c.getPossibleElements()) {
//            Element e = (Element)obj;
//            System.out.println(e);
//        }
//      }
}
