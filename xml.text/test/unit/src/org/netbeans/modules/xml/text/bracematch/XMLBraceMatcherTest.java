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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 44);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 39 && origin[1] == 48);
        int[] match = instance.doFindMatches();
        assert(match[0] == 78 && match[1] == 79);
        
        //between "<!DOCTYPE" and ">"
        instance = new XMLBraceMatcher(doc, 65);
        origin = instance.doFindOrigin();
        assert(origin == null);
        match = instance.doFindMatches();
        
        //in declaration end i.e. ">"
        instance = new XMLBraceMatcher(doc, 79);
        origin = instance.doFindOrigin();
        assert(origin[0] == 78 && origin[1] == 79);
        match = instance.doFindMatches();
        assert(match[0] == 39 && match[1] == 48);
    }
    
    public void testComment() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside start of comment i.e "<!--"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 81);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 80 && origin[1] == 84);
        int[] match = instance.doFindMatches();
        assert(match[0] == 103 && match[1] == 106);
        
        //somewhere between "<!--" and "-->"
        instance = new XMLBraceMatcher(doc, 95);
        origin = instance.doFindOrigin();
        assert(origin == null);
        match = instance.doFindMatches();
        
        //inside "-->"
        instance = new XMLBraceMatcher(doc, 105);
        origin = instance.doFindOrigin();
        assert(origin[0] == 103 && origin[1] == 106);
        match = instance.doFindMatches();
        assert(match[0] == 80 && match[1] == 84);
    }
    
    public void testTag1() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //inside start of root tag  "<root"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 110);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 107 && origin[1] == 126);
        int[] match = instance.doFindMatches();
        assert(match[0] == 204 && match[1] == 211);
        
        //somewhere between "<root" and ">"
        instance = new XMLBraceMatcher(doc, 120);
        origin = instance.doFindOrigin();
        assert(origin[0] == 107 && origin[1] == 126);
        match = instance.doFindMatches();
        
        //inside "</root>"
        instance = new XMLBraceMatcher(doc, 207);
        origin = instance.doFindOrigin();
        assert(origin[0] == 204 && origin[1] == 211);
        match = instance.doFindMatches();
        assert(match[0] == 107 && match[1] == 112);
        assert(match[2] == 125 && match[3] == 126);
    }
    
    public void testTag2() throws Exception {
        BaseDocument doc = getDocument("bracematch/test.xml");
        //just before > in "<root...>"
        XMLBraceMatcher instance = new XMLBraceMatcher(doc, 125);
        int[] origin = instance.doFindOrigin();
        assert(origin[0] == 107 && origin[1] == 126);
        int[] match = instance.doFindMatches();
        assert(match[0] == 204 && match[1] == 211);
        
        //just before > in "</root...>"
        instance = new XMLBraceMatcher(doc, 210);
        origin = instance.doFindOrigin();
        assert(origin[0] == 204 && origin[1] == 211);
        match = instance.doFindMatches();
        assert(match[0] == 107 && match[1] == 112);
        assert(match[2] == 125 && match[3] == 126);
    }
}
