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

package org.netbeans.modules.web.freeform;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Test stuff in WebProjectNature.
 * @author Jesse Glick, Tomas Mysik
 * @see org.netbeans.modules.java.freeform.JavaProjectNature
 */
public class WebProjectNatureTest extends NbTestCase {

    public WebProjectNatureTest(String name) {
        super(name);
    }

    public void testUpgradeSchema() throws Exception {
        // Formatting has to be the same as Xerces' formatter produces for this test to pass:
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<web-data xmlns=\"http://www.netbeans.org/ns/freeform-project-web/1\">\n" +
                      "    <!-- Hello there. -->\n" +
                      "    <foo bar=\"baz\" quux=\"whatever\">hello</foo>\n" +
                      "    <x>OK</x>\n" +
                      "</web-data>\n";
        String xml2expected = xml1.replaceAll("/1", "/2");
        Document doc1 = XMLUtil.parse(new InputSource(new StringReader(xml1)), false, true, null, null);
        Element el1 = doc1.getDocumentElement();
        Element el2 = LookupProviderImpl.upgradeSchema(el1);
        Document doc2 = XMLUtil.createDocument(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, null, null);
        doc2.removeChild(doc2.getDocumentElement());
        doc2.appendChild(doc2.importNode(el2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc2, baos, "UTF-8");
        String xml2actual = baos.toString("UTF-8").replaceAll(System.getProperty("line.separator"), "\n");
        assertEquals("Correct upgrade result", xml2expected, xml2actual);
    }
    
}
