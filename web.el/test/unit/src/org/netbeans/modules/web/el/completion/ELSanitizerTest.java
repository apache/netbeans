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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el.completion;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@code ELSanitizer}
 *
 * @author Erno Mononen
 */
public class ELSanitizerTest {

    public ELSanitizerTest() {
    }


    @Test
    public void testSanitizeDots() {
        String sanitized = ELSanitizer.sanitize("#{foo.}");
        assertEquals("#{foo.x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar.}");
        assertEquals("#{foo.bar.x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo[}");
        assertEquals("#{foo['x']}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo(}");
        assertEquals("#{foo()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{just[");
        assertEquals("#{just['x']}", sanitized);
    }

    @Test
    public void testSanitizeSpace() {
        String sanitized = ELSanitizer.sanitize("#{foo. }");
        assertEquals("#{foo.x }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.    }");
        assertEquals("#{foo.x    }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar. }");
        assertEquals("#{foo.bar.x }", sanitized);
    }

    @Test
    public void testSanitizeBrackets() {
        String sanitized = ELSanitizer.sanitize("#{foo[}");
        assertEquals("#{foo['x']}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo");
        assertEquals("#{foo}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo(}");
        assertEquals("#{foo()}", sanitized);
    }

    @Test
    public void testSanitizeOperators() {
        String sanitized = ELSanitizer.sanitize("#{foo +}");
        assertEquals("#{foo +x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo -}");
        assertEquals("#{foo -x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo /}");
        assertEquals("#{foo /x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo +");
        assertEquals("#{foo +x}", sanitized);
    }

    @Test
    public void testSanitizeTrailingSpaces() {
        String sanitized = ELSanitizer.sanitize("#{ }");
        assertEquals("#{ x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{ ");
        assertEquals("#{ x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{a + }");
        assertEquals("#{a + x}", sanitized);
    }

    @Test
    public void testSanitizeKeywords() {
        String sanitized = ELSanitizer.sanitize("#{a and}");
        assertEquals("#{a and x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar gt");
        assertEquals("#{foo.bar gt x}", sanitized);
    }

    @Test
    public void testSanitizeEmpty() {
        String sanitized = ELSanitizer.sanitize("#{}");
        assertEquals("#{x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{");
        assertEquals("#{x}", sanitized);
    }
    
    @Test
    public void testSanitizeUnclosedExpressionInHTML() {
        String sanitized = ELSanitizer.sanitize("#{<body><div>hello", 2);
        assertEquals("#{x}", sanitized);
        
        sanitized = ELSanitizer.sanitize("#{   <body><div>hello", 2);
        assertEquals("#{x}", sanitized);
        
        sanitized = ELSanitizer.sanitize("#{   <body>\n<div>\nhello", 2);
        assertEquals("#{x}", sanitized);

    }

    @Test
    public void testSanitizeFunctions() {
        String sanitized = ELSanitizer.sanitize("#{foo:}");
        assertEquals("#{foo:x()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo: ");
        assertEquals("#{foo:x() }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo:te");
        assertEquals("#{foo:te()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo:te ");
        assertEquals("#{foo:te() }", sanitized);
    }

    @Test
    public void testFindLastNonWhiteSpace() {
        assertEquals(2, ELSanitizer.findLastNonWhiteSpace("foo "));
        assertEquals(2, ELSanitizer.findLastNonWhiteSpace("foo     "));
        assertEquals(6, ELSanitizer.findLastNonWhiteSpace("foo bar"));
        assertEquals(6, ELSanitizer.findLastNonWhiteSpace("foo bar "));
    }

    @Test
    public void testIssue234865() {
        String sanitized = ELSanitizer.sanitize("#{['word', 4].stream().peek(i->)}");
        assertEquals("#{['word', 4].stream().peek(i->x)}", sanitized);
    }
}