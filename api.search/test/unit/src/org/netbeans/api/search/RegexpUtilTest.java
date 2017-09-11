/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.api.search;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhavlin
 */
public class RegexpUtilTest {

    @Test
    public void testMakeMultiRegexp() {
        assertEquals("", makeMultiRegexp(""));
        assertEquals("a", makeMultiRegexp("a"));
        assertEquals("ab", makeMultiRegexp("ab"));
        assertEquals("abc", makeMultiRegexp("abc"));
        assertEquals("a.", makeMultiRegexp("a?"));
        assertEquals("a.*", makeMultiRegexp("a*"));
        assertEquals(".a", makeMultiRegexp("?a"));
        assertEquals(".*a", makeMultiRegexp("*a"));
        assertEquals("a.b", makeMultiRegexp("a?b"));
        assertEquals(".a.", makeMultiRegexp("?a?"));
        assertEquals("a.*b.c", makeMultiRegexp("a*b?c"));
        assertEquals("a...*b", makeMultiRegexp("a?*?b"));
        assertEquals("a..*b", makeMultiRegexp("a*?*b"));

        assertEquals("a|b", makeMultiRegexp("a b"));
        assertEquals("a\\!b", makeMultiRegexp("a!b"));
        assertEquals("a\\\"b", makeMultiRegexp("a\"b"));
        assertEquals("a\\#b", makeMultiRegexp("a#b"));
        assertEquals("a\\$b", makeMultiRegexp("a$b"));
        assertEquals("a\\%b", makeMultiRegexp("a%b"));
        assertEquals("a\\&b", makeMultiRegexp("a&b"));
        assertEquals("a\\'b", makeMultiRegexp("a'b"));
        assertEquals("a\\(b", makeMultiRegexp("a(b"));
        assertEquals("a\\)b", makeMultiRegexp("a)b"));
        assertEquals("a\\+b", makeMultiRegexp("a+b"));
        assertEquals("a|b", makeMultiRegexp("a,b"));
        assertEquals("a\\-b", makeMultiRegexp("a-b"));
        assertEquals("a\\.b", makeMultiRegexp("a.b"));
        assertEquals("a\\/b", makeMultiRegexp("a/b"));

        assertEquals("a0b", makeMultiRegexp("a0b"));
        assertEquals("a1b", makeMultiRegexp("a1b"));
        assertEquals("a2b", makeMultiRegexp("a2b"));
        assertEquals("a3b", makeMultiRegexp("a3b"));
        assertEquals("a4b", makeMultiRegexp("a4b"));
        assertEquals("a5b", makeMultiRegexp("a5b"));
        assertEquals("a6b", makeMultiRegexp("a6b"));
        assertEquals("a7b", makeMultiRegexp("a7b"));
        assertEquals("a8b", makeMultiRegexp("a8b"));
        assertEquals("a9b", makeMultiRegexp("a9b"));

        assertEquals("a\\:b", makeMultiRegexp("a:b"));
        assertEquals("a\\;b", makeMultiRegexp("a;b"));
        assertEquals("a\\<b", makeMultiRegexp("a<b"));
        assertEquals("a\\=b", makeMultiRegexp("a=b"));
        assertEquals("a\\>b", makeMultiRegexp("a>b"));
        assertEquals("a\\@b", makeMultiRegexp("a@b"));
        assertEquals("a\\[b", makeMultiRegexp("a[b"));
        assertEquals("ab", makeMultiRegexp("a\\b"));
        assertEquals("a\\]b", makeMultiRegexp("a]b"));
        assertEquals("a\\^b", makeMultiRegexp("a^b"));
        assertEquals("a\\_b", makeMultiRegexp("a_b"));
        assertEquals("a\\`b", makeMultiRegexp("a`b"));
        assertEquals("a\\{b", makeMultiRegexp("a{b"));
        assertEquals("a\\|b", makeMultiRegexp("a|b"));
        assertEquals("a\\}b", makeMultiRegexp("a}b"));
        assertEquals("a\\~b", makeMultiRegexp("a~b"));
        assertEquals("a\\\u007fb", makeMultiRegexp("a\u007fb"));

        assertEquals("a\u0080b", makeMultiRegexp("a\u0080b"));
        assertEquals("a\u00c1b", makeMultiRegexp("a\u00c1b"));

        assertEquals("abc\\\\", makeMultiRegexp("abc\\"));

        assertEquals("", makeMultiRegexp(""));
        assertEquals("", makeMultiRegexp(" "));
        assertEquals("", makeMultiRegexp(","));
        assertEquals("", makeMultiRegexp(", "));
        assertEquals("", makeMultiRegexp(" ,"));
        assertEquals("a", makeMultiRegexp("a,"));
        assertEquals("a", makeMultiRegexp("a "));
        assertEquals("a", makeMultiRegexp("a, "));
        assertEquals("a", makeMultiRegexp("a ,"));
        assertEquals("a", makeMultiRegexp(",a"));
        assertEquals("a", makeMultiRegexp(" a"));
        assertEquals("a", makeMultiRegexp(", a"));
        assertEquals("a", makeMultiRegexp(" ,a"));
        assertEquals("a|b", makeMultiRegexp("a b"));
        assertEquals("a|b", makeMultiRegexp("a,b"));
        assertEquals("a|b", makeMultiRegexp("a, b"));
        assertEquals("a|b", makeMultiRegexp("a ,b"));
        assertEquals(" ", makeMultiRegexp("\\ "));
        assertEquals("\\,", makeMultiRegexp("\\,"));
        assertEquals("\\,", makeMultiRegexp("\\, "));
        assertEquals(" ", makeMultiRegexp(",\\ "));
        assertEquals("\\, ", makeMultiRegexp("\\,\\ "));
        assertEquals(" ", makeMultiRegexp("\\ ,"));
        assertEquals("\\,", makeMultiRegexp(" \\,"));
        assertEquals(" \\,", makeMultiRegexp("\\ \\,"));
        assertEquals("a", makeMultiRegexp("\\a,"));
        assertEquals("a\\,", makeMultiRegexp("a\\,"));
        assertEquals("a\\,", makeMultiRegexp("\\a\\,"));
        assertEquals("a", makeMultiRegexp("\\a "));
        assertEquals("a ", makeMultiRegexp("a\\ "));
        assertEquals("a ", makeMultiRegexp("\\a\\ "));
        assertEquals("a|\\\\", makeMultiRegexp("a, \\"));
        assertEquals("a| ", makeMultiRegexp("a,\\ "));
        assertEquals("a| \\\\", makeMultiRegexp("a,\\ \\"));
        assertEquals("a\\,", makeMultiRegexp("a\\, "));
        assertEquals("a\\,|\\\\", makeMultiRegexp("a\\, \\"));
        assertEquals("a\\, ", makeMultiRegexp("a\\,\\ "));
        assertEquals("a\\, \\\\", makeMultiRegexp("a\\,\\ \\"));
        assertEquals("a", makeMultiRegexp("\\a, "));
        assertEquals("a|\\\\", makeMultiRegexp("\\a, \\"));
        assertEquals("a| ", makeMultiRegexp("\\a,\\ "));
        assertEquals("a| \\\\", makeMultiRegexp("\\a,\\ \\"));
        assertEquals("a\\,", makeMultiRegexp("\\a\\, "));
        assertEquals("a\\,|\\\\", makeMultiRegexp("\\a\\, \\"));
        assertEquals("a\\, ", makeMultiRegexp("\\a\\,\\ "));
        assertEquals("a\\, \\\\", makeMultiRegexp("\\a\\,\\ \\"));
        assertEquals("a|\\\\", makeMultiRegexp("a ,\\"));
        assertEquals("a|\\,", makeMultiRegexp("a \\,"));
        assertEquals("a|\\,\\\\", makeMultiRegexp("a \\,\\"));
        assertEquals("a ", makeMultiRegexp("a\\ ,"));
        assertEquals("a |\\\\", makeMultiRegexp("a\\ ,\\"));
        assertEquals("a \\,", makeMultiRegexp("a\\ \\,"));
        assertEquals("a \\,\\\\", makeMultiRegexp("a\\ \\,\\"));
        assertEquals("a", makeMultiRegexp("\\a ,"));
        assertEquals("a|\\\\", makeMultiRegexp("\\a ,\\"));
        assertEquals("a|\\,", makeMultiRegexp("\\a \\,"));
        assertEquals("a|\\,\\\\", makeMultiRegexp("\\a \\,\\"));
        assertEquals("a ", makeMultiRegexp("\\a\\ ,"));
        assertEquals("a |\\\\", makeMultiRegexp("\\a\\ ,\\"));
        assertEquals("a \\,", makeMultiRegexp("\\a\\ \\,"));
        assertEquals("a \\,\\\\", makeMultiRegexp("\\a\\ \\,\\"));

        assertEquals("a|b", makeMultiRegexp("a, b"));
        assertEquals("a|.*b.", makeMultiRegexp("a,*b?"));
        assertEquals("a|\\*b.", makeMultiRegexp("a,\\*b?"));
        assertEquals("a|.*b\\?", makeMultiRegexp("a,*b\\?"));
    }

    private String makeMultiRegexp(String string) {
        return RegexpUtil.makeFileNamePattern(
                SearchScopeOptions.create(string, false)).pattern();
    }
}