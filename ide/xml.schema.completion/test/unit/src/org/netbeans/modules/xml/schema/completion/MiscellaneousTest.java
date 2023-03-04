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
