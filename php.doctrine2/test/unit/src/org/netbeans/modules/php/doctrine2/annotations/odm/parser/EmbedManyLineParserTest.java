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
package org.netbeans.modules.php.doctrine2.annotations.odm.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class EmbedManyLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public EmbedManyLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new TypedParametersAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsEmbedManyParsedLine_01() throws Exception {
        assertTrue(parser.parse("EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("EmbedManys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany   ");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany\t\t  ");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
        String type2 = types.get(new OffsetRange(26, 31));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(103, 120));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(131, 148));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("Annotations\\EmbedMany", type1);
        String type2 = types.get(new OffsetRange(38, 43));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(115, 132));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(143, 160));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\EmbedMany", type1);
        String type2 = types.get(new OffsetRange(35, 40));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(112, 129));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(140, 157));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("embedmany");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("embedmany", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\embedmany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\embedmany", type1);
        String type2 = types.get(new OffsetRange(35, 40));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(112, 129));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(140, 157));
        assertEquals("Documents\\SongTag", type4);
    }

}
