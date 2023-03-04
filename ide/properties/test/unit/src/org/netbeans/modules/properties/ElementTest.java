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

package org.netbeans.modules.properties;

import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author  Marian Petras
 */
public class ElementTest extends TestCase {
    
    private Method escaperMethod;

    public ElementTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        escaperMethod = null;
    }

    public void testEscapeSpecialCharsInKey() throws Exception {
        initMethod();

        /* spaces: */
        checkTranslation("", "");
        checkTranslation(" ", "\\ ");
        checkTranslation("  ", "\\ \\ ");
        checkTranslation("   ", "\\ \\ \\ ");
        checkTranslation("a ", "a\\ ");
        checkTranslation(" a", "\\ a");
        checkTranslation(" a ", "\\ a\\ ");
        checkTranslation("a b", "a\\ b");
        checkTranslation("a  b", "a\\ \\ b");
        checkTranslation("a   b", "a\\ \\ \\ b");
        checkTranslation("a    b", "a\\ \\ \\ \\ b");
        checkTranslation("a b c", "a\\ b\\ c");
        checkTranslation("a b  c", "a\\ b\\ \\ c");
        checkTranslation("alpha beta gamma", "alpha\\ beta\\ gamma");
        checkTranslation(" alpha  beta   gamma    ",
                         "\\ alpha\\ \\ beta\\ \\ \\ gamma\\ \\ \\ \\ ");

        /* equal signs: */
        checkTranslation("", "");
        checkTranslation("=", "\\=");
        checkTranslation("==", "\\=\\=");
        checkTranslation("===", "\\=\\=\\=");
        checkTranslation("a=", "a\\=");
        checkTranslation("=a", "\\=a");
        checkTranslation("=a=", "\\=a\\=");
        checkTranslation("a=b", "a\\=b");
        checkTranslation("a==b", "a\\=\\=b");
        checkTranslation("a===b", "a\\=\\=\\=b");
        checkTranslation("a====b", "a\\=\\=\\=\\=b");
        checkTranslation("a=b=c", "a\\=b\\=c");
        checkTranslation("a=b==c", "a\\=b\\=\\=c");
        checkTranslation("alpha=beta=gamma", "alpha\\=beta\\=gamma");
        checkTranslation("=alpha==beta===gamma====",
                         "\\=alpha\\=\\=beta\\=\\=\\=gamma\\=\\=\\=\\=");

        /* double quotes: */
        checkTranslation("", "");
        checkTranslation(":", "\\:");
        checkTranslation("::", "\\:\\:");
        checkTranslation(":::", "\\:\\:\\:");
        checkTranslation("a:", "a\\:");
        checkTranslation(":a", "\\:a");
        checkTranslation(":a:", "\\:a\\:");
        checkTranslation("a:b", "a\\:b");
        checkTranslation("a::b", "a\\:\\:b");
        checkTranslation("a:::b", "a\\:\\:\\:b");
        checkTranslation("a::::b", "a\\:\\:\\:\\:b");
        checkTranslation("a:b:c", "a\\:b\\:c");
        checkTranslation("a:b::c", "a\\:b\\:\\:c");
        checkTranslation("alpha:beta:gamma", "alpha\\:beta\\:gamma");
        checkTranslation(":alpha::beta:::gamma::::",
                         "\\:alpha\\:\\:beta\\:\\:\\:gamma\\:\\:\\:\\:");

        /* BS, tab, LF, FF, CR and other ISO control chars: */
        checkTranslation("\u0000", "\\u0000");
        checkTranslation("\u0001", "\\u0001");
        checkTranslation("\u0002", "\\u0002");
        checkTranslation("\u0003", "\\u0003");
        checkTranslation("\u0004", "\\u0004");
        checkTranslation("\u0005", "\\u0005");
        checkTranslation("\u0006", "\\u0006");
        checkTranslation("\u0007", "\\u0007");
        checkTranslation("\b",     "\\b");
        checkTranslation("\t",     "\\t");
        checkTranslation("\n",     "\\n");
        checkTranslation("\u000b", "\\u000b");
        checkTranslation("\f",     "\\f");
        checkTranslation("\r",     "\\r");
        checkTranslation("\u000e", "\\u000e");
        checkTranslation("\u000f", "\\u000f");
        checkTranslation("\u0010", "\\u0010");
        checkTranslation("\u0011", "\\u0011");
        checkTranslation("\u0012", "\\u0012");
        checkTranslation("\u0013", "\\u0013");
        checkTranslation("\u0014", "\\u0014");
        checkTranslation("\u0015", "\\u0015");
        checkTranslation("\u0016", "\\u0016");
        checkTranslation("\u0017", "\\u0017");
        checkTranslation("\u0018", "\\u0018");
        checkTranslation("\u0019", "\\u0019");
        checkTranslation("\u001a", "\\u001a");
        checkTranslation("\u001b", "\\u001b");
        checkTranslation("\u001c", "\\u001c");
        checkTranslation("\u001d", "\\u001d");
        checkTranslation("\u001e", "\\u001e");
        checkTranslation("\u001f", "\\u001f");

        /* comment chars (#, !): */
        checkTranslation("#", "\\#");
        checkTranslation("#abc", "\\#abc");
        checkTranslation("##", "\\##");
        checkTranslation("##abc", "\\##abc");
        checkTranslation("##abc#def", "\\##abc#def");
        checkTranslation("!", "\\!");
        checkTranslation("!abc", "\\!abc");
        checkTranslation("!!", "\\!!");
        checkTranslation("!!abc", "\\!!abc");
        checkTranslation("!!abc!def", "\\!!abc!def");
        checkTranslation(" #", "\\ #");
        checkTranslation(" !", "\\ !");
        checkTranslation("#!", "\\#!");
        checkTranslation("!#", "\\!#");
        checkTranslation("# !", "\\#\\ !");
        checkTranslation("! #", "\\!\\ #");
        checkTranslation(" #!", "\\ #!");
        checkTranslation(" !#", "\\ !#");
    }

    private void checkTranslation(String origText,
                                  String expectedTranslation) throws Exception {
        String actualTranslation = (String) escaperMethod.invoke(null, origText);
        assertEquals(expectedTranslation, actualTranslation);
    }

    private void initMethod() throws Exception {
        Class<Element.KeyElem> cls = Element.KeyElem.class;
        escaperMethod = cls.getDeclaredMethod("escapeSpecialChars", String.class);
        escaperMethod.setAccessible(true);
    }

}
