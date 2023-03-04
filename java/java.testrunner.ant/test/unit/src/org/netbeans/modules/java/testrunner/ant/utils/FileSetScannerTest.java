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

package org.netbeans.modules.java.testrunner.ant.utils;

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.java.testrunner.ant.utils.FileSetScanner.AntPattern;

/**
 *
 * @author  Marian Petras
 */
public class FileSetScannerTest extends TestCase {
    
    private static final String SEP = File.separator;
    
    
    public FileSetScannerTest(String testName) {
        super(testName);
    }
    
    
    public void testConstructor() {
        System.out.println("constructor");
        
        try {
            new AntPattern(new String[] {"ij", "kl"});
            new AntPattern(new String[] {"ij"});
            new AntPattern(new String[] {});
        } catch (IllegalArgumentException ex) {
            fail("The IllegalArgumentException should not be thrown");
        }
        
        try {
            new AntPattern(null);
            fail("The constructor should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }
    
    public void testEquals() {
        System.out.println("equals");
        
        assertEquals(new AntPattern(new String[0]),
                     new AntPattern(new String[0]));
        
        assertNotEqual(new AntPattern(new String[0]),
                       null);
        
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {"cat", "dog"}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {new String("cat"), "dog"}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {"cat", new String("dog")}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {new String("cat"),
                                                  new String("dog")}));
        
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat", "dog", "pig"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat", "cow"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {}));
        assertNotEqual(new AntPattern(new String[] {"cat"}),
                       new AntPattern(new String[] {"cow"}));
    }
    
    public void testParsePatternString() {
        System.out.println("parsePatternString");
        
        String patternString;
        AntPattern expResult;
        AntPattern result;
        
        FileSetScanner s =
                new FileSetScanner(new FileSet(new AntProject()));
        
        patternString = replaceSep("ab");
        expResult = new AntPattern(new String[] {"ab"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd");
        expResult = new AntPattern(new String[] {"ab", "cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd/");
        expResult = new AntPattern(new String[] {"ab", "cd", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/**");
        expResult = new AntPattern(new String[] {"ab", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd**");
        expResult = new AntPattern(new String[] {"ab", "cd**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/**cd");
        expResult = new AntPattern(new String[] {"ab", "**cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd*");
        expResult = new AntPattern(new String[] {"ab", "cd*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/*cd");
        expResult = new AntPattern(new String[] {"ab", "*cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/c*d");
        expResult = new AntPattern(new String[] {"ab", "c*d"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/c*d*");
        expResult = new AntPattern(new String[] {"ab", "c*d*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("**/cd");
        expResult = new AntPattern(new String[] {"**", "cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/");
        expResult = new AntPattern(new String[] {"ab", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/*");
        expResult = new AntPattern(new String[] {"ab", "*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("a?bc/def*g/*.java");
        expResult = new AntPattern(new String[] {"a?bc", "def*g", "*.java"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("abc/**/**/xyz");
        expResult = new AntPattern(new String[] {"abc", "**", "xyz"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("abc/**/**");
        expResult = new AntPattern(new String[] {"abc", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**/**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/xyz/*.java");
        expResult = new AntPattern(new String[] {"**", "xyz", "*.java"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**/xyz/**");
        expResult = new AntPattern(new String[] {"**", "xyz", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("ab/**/cd/**/**/ef/gh");
        expResult = new AntPattern(new String[] {
                                        "ab", "**", "cd", "**", "ef", "gh"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

    }
    
    public void testQuote() {
        assertEquals("", AntPattern.quote(""));
        assertEquals("abcd", AntPattern.quote("abcd"));
        assertEquals("ab\\*cd", AntPattern.quote("ab*cd"));
        assertEquals("\\^abcd", AntPattern.quote("^abcd"));
        assertEquals("abcd\\$", AntPattern.quote("abcd$"));
        assertEquals("ab\\|cd", AntPattern.quote("ab|cd"));
        assertEquals("a\\.bcd", AntPattern.quote("a.bcd"));
        assertEquals("a\\.\\*bcd", AntPattern.quote("a.*bcd"));
        assertEquals("ab\\[p-q\\]cd", AntPattern.quote("ab[p-q]cd"));
        assertEquals("ab\\(cd\\)\\*", AntPattern.quote("ab(cd)*"));
        assertEquals("a\\+bcd\\$", AntPattern.quote("a+bcd$"));
        assertEquals("abcd\\{4,\\}", AntPattern.quote("abcd{4,}"));
    }
    
    private static String replaceSep(String pattern) {
        return File.separatorChar == '/'
               ? pattern 
               : pattern.replace('/', File.separatorChar);
    }
    
    private static void assertNotEqual(Object a, Object b) {
        assertTrue((a == null) && (b != null)
                   || (a != null) && !a.equals(b));
    }

}
