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

package org.netbeans.modules.languages.parser;

import junit.framework.TestCase;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.StringInput;

/**
 *
 * @author Jan Jancura
 */
public class PatternTest extends TestCase {
    
    public PatternTest (String testName) {
        super (testName);
    }

    public void testNext () throws ParseException {
        testReadToken ("'/'?'>'", "/>>", true, ">");
        testReadToken ("'/'?'>'", ">>", true, ">");
        testReadToken ("'/'?'>'", "//>>", false, "//>>");
        testReadToken ("[^'a']*'a'", "", false, "");
        testReadToken ("[^'a']*'a'", "ab", true, "b");
        testReadToken ("[^'a']*'a'", "qqab", true, "b");
        testReadToken ("[^'a']*'a'", "qabaw", true, "baw");
        testReadToken ("([^'a']*'a')+", "qabaw", true, "w");
        testReadToken ("([^'a']*'a')+", "aaabw", true, "bw");
        testReadToken ("([^'a']*'a')+", "aabaw", true, "w");
        testReadToken ("([^'a']*'a')+", "bb", false, "bb");
        testReadToken ("([^'a']*'a')*", "qabaw", true, "w");
        testReadToken ("([^'a']*'a')*", "aaabw", true, "bw");
        testReadToken ("([^'a']*'a')*", "aabaw", true, "w");
        testReadToken ("([^'a']*'a')*", "bb", true, "bb");
        testReadToken ("([^'a']*'a')+'b'", "qwabw", true, "w");
        testReadToken ("([^'a']*'a')+'b'", "qaabw", true, "w");
        testReadToken ("([^'a']*'a')+'b'", "qabdfabw", true, "w");
        testReadToken ("('ab')*|('dc')*", "ababdc", true, "dc");        
        testReadToken ("('ab')*|('dc')*", "dcdcab", true, "ab");        
        testReadToken ("('ab')*|('dc')*", "", true, "");        
        testReadToken ("('ab'|'dc')*", "abdcabbb", true, "bb");
        testReadToken ("('ab'|'dc')*", "", true, "");
        testReadToken ("('ab')*('dc')*", "ababdcdcc", true, "c");
        testReadToken ("('ab')*('dc')*", "dcab", true, "ab");
        testReadToken ("('ab')*('dc')*", "aba", true, "a");
        testReadToken ("('ab')*('dc')*", "", true, "");
        testReadToken ("'abc'|'a'.'d'", "abd", true, "");
        testReadToken ("('a'.'c')*'abd'", "abcabdab", true, "ab");
        testReadToken ("('a'.'c')*'abd'", "abdab", true, "ab");
        testReadToken ("('a'.'c')*'abd'", "abcaxcabda", true, "a");
        testReadToken ("('a'.'c')+'abc'", "axcabcabca", true, "a");
        testReadToken ("('a'..)*'abd'", "abdabda", true, "a");
        testReadToken ("('a'..)*'abd'", "abda", true, "a");
        testReadToken ("('a'..)*'abd'", "axxabda", true, "a");
        testReadToken ("('a'.'c'|'asd')|'a'.'d'", "asd", true, "");
        testReadToken ("('a'.'c'|'asd')|'a'.'d'", "axd", true, "");
        testReadToken ("'as'['q''w''e']'as'", "asqass", true, "s");
        testReadToken ("'as'['q''w''e']'as'", "aseass", true, "s");
        testReadToken ("'as'['q''w''e']'as'", "asqwass", false, "asqwass");
        testReadToken ("'a''s'*'d'", "ad", true, "");
        testReadToken ("'a''s'*'d'", "assdw", true, "w");
        testReadToken ("'a'.'d'", "ad", false, "ad");
        testReadToken ("'a'.'dd'", "adddd", true, "d");
        testReadToken ("'as'['q''w']+'zx'", "aszxx", false, "aszxx");
        testReadToken ("'as'['q''w']+'zx'", "asqwzxx", true, "x");
        testReadToken ("'as'['q''w']+'zx'", "asqwqwzxx", true, "x");
        testReadToken ("'as'['q''w']*'zx'", "asqqwzxx", true, "x");
        testReadToken ("'as'['q''w']*'zx'", "aszxx", true, "x");
        testReadToken ("'as'['q''w']*'zx'", "asqwzzx", false, "asqwzzx");
        testReadToken ("'ab'|'cd'", "ab", true, "");
        testReadToken ("('ab'|'cd'|'de')*", "ab", true, "");
        testReadToken ("('ab'|'cd'|'de')*", "ababdeaq", true, "aq");
        testMatches   ("('ab'|'cd'|'de')*", "", true);
        testReadToken ("('ab')*|('abc')*", "ab", true, "");
        testReadToken ("('ab')*|('abc')*", "ababcabd", true, "cabd");
        testReadToken ("('ab')*|('abc')*", "abcabcabd", true, "abd");
        testReadToken ("('ab')*|('abc')*", "abcad", true, "ad");
        testReadToken ("('abc')*|('ab')*", "ab", true, "");
        testReadToken ("'a'[^'b']|'acd'", "acdd", true, "d");
        testReadToken ("'a'[^'b']|'acd'", "ab", false, "ab");
        testReadToken ("'a'[^'b''c''d']*", "aswsqqsbx", true, "bx");
        testMatches   ("('a')*'a'", "", false);
        testReadToken ("('a')*'a'", "ac", true, "c");
        testReadToken ("('a')*'a'", "aac", true, "c");
        testMatches   ("('a''a'*)*", "", true); 
        testReadToken ("('a''a'*)*", "a", true, ""); 
        testReadToken ("('a''a'*)*", "aaaa", true, ""); 
        testReadToken ("[^'a']*'a'('a'|[^'a''b'][^'a']*'a')*'b'", "sdasdaadsaabfabf", true, "fabf");
        testReadToken ("'/*'-'*/'", "/**//", true, "/");
        testReadToken ("'/*'-'*/'", "/***/*", true, "*");
        testReadToken ("'/*'-'*/'", "/*asdf*sdf*sdf//sdf/sdf*/a", true, "a");
        testReadToken ("'/*'-'*/'", "/*", false, "/*");
        testReadToken ("'/*'-'*/'", "/**qwe", false, "/**qwe");
        testReadToken ("'/*'-'*/'", "/*a*/*/qwe", true, "*/qwe");
        testReadToken ("-'abc'", "abaabbbabcd", true, "d");
        testReadToken ("-'abc'", "abaabcbbabcd", true, "bbabcd");
        testReadToken ("-'abc'", "abaabacbbabacd", false, "abaabacbbabacd");
        testReadToken ("'\\u0041'['\\u0030'-'\\u0039']*", "A12001ax", true, "ax");
        //testReadToken ("'\"' ( [^ '\"' '\n' '\r'] | ('\\' ['r' 'n' 't' '\\' '\'' '\"']) )* '\"'", "\\\\", true, "");
    }
    
    private static void testReadToken (
        String expression,
        String text,
        boolean result,
        String ext
    ) throws ParseException {
        StringInput in = new StringInput (text);
        Pattern p = Pattern.create (expression);
        assertEquals (expression + " <" + text + ">", result, p.next (in) != null);
        assertEquals (expression + " <" + text + ">", ext, in.getAsText ());
    }
    
    private static void testMatches (
        String expression,
        String text,
        boolean result
    ) throws ParseException {
        Pattern p = Pattern.create (expression);
        assertEquals (expression + " <" + text + ">", result, p.matches (text));
    }    
}
