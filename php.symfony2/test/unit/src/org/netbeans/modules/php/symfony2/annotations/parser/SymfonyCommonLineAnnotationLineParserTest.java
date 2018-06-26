/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.annotations.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyCommonLineAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public SymfonyCommonLineAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = SymfonyCommonLineAnnotationLineParser.getDefault();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    \"short_bio\" = {@Assert\\NotBlank(), @Assert\\MaxLength(limit = 100, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("\"short_bio\" = {@Assert\\NotBlank(), @Assert\\MaxLength(limit = 100,", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(20, 35));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(40, 56));
        assertNotNull(type2);
        assertEquals("Assert\\MaxLength", type2);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    @Assert\\NotBlank, @Assert\\MinLength(5), ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("@Assert\\NotBlank, @Assert\\MinLength(5),", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(5, 20));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(23, 39));
        assertNotNull(type2);
        assertEquals("Assert\\MinLength", type2);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    @Assert\\NotBlank, type=\"Foo\\Bar\" ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("@Assert\\NotBlank, type=\"Foo\\Bar\"", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(5, 20));
        assertNotNull(type1);
        assertEquals("Assert\\NotBlank", type1);
        String type2 = types.get(new OffsetRange(28, 35));
        assertNotNull(type2);
        assertEquals("Foo\\Bar", type2);
    }

}
