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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRoot;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRootAttribute;

/**
 * Tests initialization of CompletionContext and various utility methods that
 * are used heavily for code completion.
 * 
 * @author Samaresh
 */
public class MiscellaneousTest extends AbstractTestCase {
        
    public MiscellaneousTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new MiscellaneousTest("testCompletionContext1"));        
        suite.addTest(new MiscellaneousTest("testReadNamespace"));
        suite.addTest(new MiscellaneousTest("testGetDocRoot"));
        suite.addTest(new MiscellaneousTest("testIsDTDBasedDocument1"));
        suite.addTest(new MiscellaneousTest("testIsDTDBasedDocument2"));        
        return suite;
    }
    
    //issue 133819
    public void testCompletionContext1() throws Exception {
        setupCompletion("resources/Context.xml", null);
        
        //at offset 320
        CompletionContextImpl context = getContextAtOffset(320);
        assert(context.getPathFromRoot().size() == 1);
        QName qname = context.getPathFromRoot().get(0);
        assert(qname.getLocalPart().equals("application"));
        assert(qname.getNamespaceURI().
                equals("http://geronimo.apache.org/xml/ns/j2ee/application-2.0"));
        assert(context.getSchemas().get(0).toString().
                equals("http://geronimo.apache.org/xml/ns/j2ee/application-2.0"));
        
        //at offset 614
        context = getContextAtOffset(614);
        assert(context.getPathFromRoot().size() == 1);
        qname = context.getPathFromRoot().get(0);
        assert(qname.getLocalPart().equals("environment"));
        assert(qname.getNamespaceURI().
                equals("http://geronimo.apache.org/xml/ns/deployment-1.2"));
        assert(context.getSchemas().get(0).toString().
                equals("http://geronimo.apache.org/xml/ns/deployment-1.2"));
        
        //at offset 643
        context = getContextAtOffset(643);
        assert(context.getPathFromRoot().size() == 1);
        qname = context.getPathFromRoot().get(0);
        assert(qname.getLocalPart().equals("application"));
        assert(qname.getNamespaceURI().
                equals("http://geronimo.apache.org/xml/ns/j2ee/application-2.0"));
        assert(context.getSchemas().get(0).toString().
                equals("http://geronimo.apache.org/xml/ns/j2ee/application-2.0"));
    }    

    /**
     * Finds the docroot and its attributes.
     */
    public void testGetDocRoot() throws Exception {
        String[] expectedResult = {
            "xmlns:c=http://www.camera.com",
            "xmlns:n=http://www.nikon.com",
            "xmlns:o=http://www.olympus.com",
            "xmlns:p=http://www.pentax.com",
            "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance",
            "xsi:schemaLocation=http://www.camera.com camera.xsd"
        };
        
        setupCompletion("resources/Camera.xml", null);
        DocRoot root = CompletionUtil.getDocRoot(getDocument());
        assert("c:camera".equals(root.getName()));
        List<DocRootAttribute> attributes = root.getAttributes();
        assert(attributes.size() == 6);
        String[] results = new String[attributes.size()];
        for(int i=0; i<attributes.size(); i++) {
            results[i] = attributes.get(i).toString();
        }
        assertResult(results, expectedResult);
    }
    
    /**
     * Tests to see if the document declares any DOCTYPE.
     */
    public void testIsDTDBasedDocument1() throws Exception {
        setupCompletion("resources/DocType.xml", null);
        assert(CompletionUtil.isDTDBasedDocument(instanceDocument));
    }
    
    /**
     * Tests to see if the document declares any DOCTYPE. Also it serves to
     * test the performance of finding DOCTYPE declaration in a XML.
     */
    public void testIsDTDBasedDocument2() throws Exception {
        long start = System.currentTimeMillis();
        setupCompletion("resources/NFL.xml", null);
        assert(!CompletionUtil.isDTDBasedDocument(instanceDocument));
        long end = System.currentTimeMillis();
        System.out.println("Time taken for isDTDBasedDocument: " + (end-start));
    }
    
    /**
     * Reads the namespaces specified in an instance document like project.xml.
     */
    public void testReadNamespace() throws Exception {
        setupCompletion("resources/project.xml", null);
        java.util.HashMap<String, String> nsMap = CompletionUtil.getNamespacesFromStartTags(getDocument());
        String[] results = new String[nsMap.size()];
        results = nsMap.keySet().toArray(results);
        String[] expectedResult = {"http://www.netbeans.org/ns/project/1","http://www.netbeans.org/ns/nb-module-project/3"};
        assertResult(results, expectedResult);
    }
    
}
