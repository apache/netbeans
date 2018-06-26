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
package org.netbeans.modules.php.doctrine2.annotations.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2CommonLineAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public Doctrine2CommonLineAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = Doctrine2CommonLineAnnotationLineParser.getDefault();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 19));
        assertEquals("Index", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 23));
        assertEquals("Foo\\Index", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@UniqueConstraint(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@UniqueConstraint(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 30));
        assertEquals("UniqueConstraint", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\UniqueConstraint(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\UniqueConstraint(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 34));
        assertEquals("Foo\\UniqueConstraint", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@JoinColumn(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@JoinColumn(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 24));
        assertEquals("JoinColumn", type1);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\JoinColumn(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\JoinColumn(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 28));
        assertEquals("Foo\\JoinColumn", type1);
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Blah\\Blah(name=\"user_idx\", columns={\"email\"})} ");
        assertNull(parsedLine);
    }

    public void testWithTypedParam_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    repositoryClass=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("repositoryClass=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(21, 45));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(59, 64));
        assertEquals("Index", type2);
    }

    public void testWithTypedParam_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    targetDocument=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("targetDocument=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(20, 44));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(58, 63));
        assertEquals("Index", type2);
    }

    public void testWithTypedParam_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    targetEntity=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("targetEntity=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(18, 42));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(56, 61));
        assertEquals("Index", type2);
    }

}
