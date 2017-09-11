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
