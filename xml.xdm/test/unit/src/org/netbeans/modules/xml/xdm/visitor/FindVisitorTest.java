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

/*
 * FindVisitorTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 1:39 PM
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.*;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author ajit
 */
public class FindVisitorTest extends TestCase {
    
    public FindVisitorTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FindVisitorTest.class);
        
        return suite;
    }
    
    public void testFind() {
        
        FindVisitor instance = new FindVisitor();

        Document root = xmlModel.getDocument();

        // try to find company
        Element company = (Element)root.getChildNodes().item(0);
        Node result = instance.find(root, company.getId());
        assertEquals(company, result);

        // try to find attribute
        Element employee = (Element)company.getChildNodes().item(1);
        Attribute attr = (Attribute)employee.getAttributes().item(0);
        result = instance.find(root, attr.getId());
        assertEquals(attr, result);

        // try to find text
        Text txt = (Text)employee.getChildNodes().item(0);
        result = instance.find(root, txt.getId());
        assertEquals(txt, result);
    }
    
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("visitor/test.xml");
        xmlModel.sync();
    }
    
    private XDMModel xmlModel;
}
