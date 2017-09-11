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
 * FindNamespaceVisitorTest.java
 * JUnit based test
 *
 * Created on November 18, 2005, 10:34 AM
 */

package org.netbeans.modules.xml.xdm.visitor;

import junit.framework.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.w3c.dom.NamedNodeMap;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author ajit
 */
public class FindNamespaceVisitorTest extends TestCase {
    
    public FindNamespaceVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FindNamespaceVisitorTest.class);
        
        return suite;
    }

    /**
     * Test of findNamespace method, of class org.netbeans.modules.xml.xdm.visitor.FindNamespaceVisitor.
     */
    public void testFindNamespace() throws Exception {
        
        XDMModel xdmModel = Util.loadXDMModel("diff/TravelItinerary1.xsd");
        Document root = xdmModel.getDocument();
        FindNamespaceVisitor instance = new FindNamespaceVisitor(root);
        
        Node target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3);
        String expResult = "http://www.w3.org/2001/XMLSchema";
        String result = instance.findNamespace(target);
        assertEquals(expResult, result);

        target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3).getAttributes().item(0);
        expResult = null;
        result = instance.findNamespace(target);
        assertEquals(expResult, result);
    }
    
}
