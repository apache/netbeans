/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.annotation;

import java.util.Collections;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LinkLineParserTest extends PHPTestBase {
    private LinkLineParser parser;

    public LinkLineParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new LinkLineParser();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link http://www.seznam.cz");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link ");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\t");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\thttp://www.seznam.cz");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link           http://www.seznam.cz     ");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link\t    \thttp://www.seznam.cz   \t   ");
        assertEquals("link", parsedLine.getName());
        assertEquals("http://www.seznam.cz", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("link");
        assertEquals("link", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("@link http://www.seznam.cz");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("foo http://www.seznam.cz");
        assertNull(parsedLine);
    }

    public void testInvalidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("linked http://www.seznam.cz");
        assertNull(parsedLine);
    }

}
