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

package org.netbeans.modules.xml.text.bracematch;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.AbstractTestCase;

/**
 * Open test.xml in editor and use column numbers to calculate the
 * expected results.
 * 
 * @author Samaresh
 */
public class XMLBraceMatcherTest extends AbstractTestCase {
    
    public XMLBraceMatcherTest(String testName) {
        super(testName);
    }            

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLBraceMatcherTest("testPI"));
        suite.addTest(new XMLBraceMatcherTest("testDeclaration"));
        suite.addTest(new XMLBraceMatcherTest("testComment"));
        suite.addTest(new XMLBraceMatcherTest("testTag1"));
        suite.addTest(new XMLBraceMatcherTest("testTag2"));
        return suite;
    }
    
    public void testPI() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside PI_START
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 1);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 0 && origin[1] == 2);
        int[] match = instance.doFindMatches();
        assert(match[0] == 36 && match[1] == 38);
        
        //between PI_START/PI_END
        instance = new XMLBraceMatcher(doc, 20);
        origin = instance.doFindOrigin();
        assert(origin == null);
        match = instance.doFindMatches();
        
        //inside PI_END
        instance = new XMLBraceMatcher(doc, 37);
        origin = instance.doFindOrigin();
        assert(origin[0] == 36 && origin[1] == 38);
        match = instance.doFindMatches();
        assert(match[0] == 0 && match[1] == 2);
    }

    public void testDeclaration() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside "<!DOCTYPE"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 866);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 861 && origin[1] == 870);
        int[] match = instance.doFindMatches();
        assert(match[0] == 900 && match[1] == 901);
        
        //between "<!DOCTYPE" and ">"
        instance = new XMLBraceMatcher(doc, 887);
        origin = instance.doFindOrigin();
        assert(origin == null);
        match = instance.doFindMatches();
        
        //in declaration end i.e. ">"
        instance = new XMLBraceMatcher(doc, 901);
        origin = instance.doFindOrigin();
        assert(origin[0] == 900 && origin[1] == 901);
        match = instance.doFindMatches();
        assert(match[0] == 861 && match[1] == 870);
    }
    
    public void testComment() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside start of comment i.e "<!--"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 903);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 902 && origin[1] == 906);
        int[] match = instance.doFindMatches();
        assert(match[0] == 925 && match[1] == 928);
        
        //somewhere between "<!--" and "-->"
        instance = new XMLBraceMatcher(doc, 917);
        origin = instance.doFindOrigin();
        assert(origin == null);
        match = instance.doFindMatches();
        
        //inside "-->"
        instance = new XMLBraceMatcher(doc, 927);
        origin = instance.doFindOrigin();
        assert(origin[0] == 925 && origin[1] == 928);
        match = instance.doFindMatches();
        assert(match[0] == 902 && match[1] == 906);
    }
    
    public void testTag1() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside start of root tag  "<root"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 932);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 929 && origin[1] == 948);
        int[] match = instance.doFindMatches();
        assert(match[0] == 1026 && match[1] == 1033);
        
        //somewhere between "<root" and ">"
        instance = new XMLBraceMatcher(doc, 942);
        origin = instance.doFindOrigin();
        assert(origin[0] == 929 && origin[1] == 948);
        match = instance.doFindMatches();
        
        //inside "</root>"
        instance = new XMLBraceMatcher(doc, 1029);
        origin = instance.doFindOrigin();
        assert(origin[0] == 1026 && origin[1] == 1033);
        match = instance.doFindMatches();
        assert(match[0] == 929 && match[1] == 934);
        assert(match[2] == 947 && match[3] == 948);
    }
    
    public void testTag2() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //just before > in "<root...>"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 947);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 929 && origin[1] == 948);
        int[] match = instance.doFindMatches();
        assert(match[0] == 1026 && match[1] == 1033);
        
        //just before > in "</root...>"
        instance = new XMLBraceMatcher(doc, 1032);
        origin = instance.doFindOrigin();
        assert(origin[0] == 1026 && origin[1] == 1033);
        match = instance.doFindMatches();
        assert(match[0] == 929 && match[1] == 934);
        assert(match[2] == 947 && match[3] == 948);
    }
}
