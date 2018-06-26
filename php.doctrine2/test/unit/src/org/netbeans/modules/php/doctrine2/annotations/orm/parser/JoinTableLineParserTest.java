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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class JoinTableLineParserTest extends NbTestCase {
    private EncapsulatingAnnotationLineParser parser;

    public JoinTableLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new EncapsulatingAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsJoinTableParsedLine_01() throws Exception {
        assertTrue(parser.parse("JoinTable") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinTableParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\JoinTable") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinTableParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\JoinTable") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinTableParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\JoinTable(name=\"user\")") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("JoinTables"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinTable");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("JoinTable", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinTable   ");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("JoinTable", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinTable\t\t  ");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("JoinTable", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinTable(name=\"user\")");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("JoinTable", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\JoinTable(name=\"user\")  \t");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("Annotations\\JoinTable", type1);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinTable(name=\"user\")  \t");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\JoinTable", type1);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("jointable");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("jointable", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\jointable(name=\"user\")  \t");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\jointable", type1);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinTable(name=\"users_phonenumbers\", joinColumns={@JoinColumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@JoinColumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"users_phonenumbers\", joinColumns={@JoinColumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@JoinColumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\JoinTable", type1);
        String type2 = types.get(new OffsetRange(60, 70));
        assertEquals("JoinColumn", type2);
        String type3 = types.get(new OffsetRange(137, 147));
        assertEquals("JoinColumn", type3);
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\jointable(name=\"users_phonenumbers\", joinColumns={@joincolumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@joincolumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"users_phonenumbers\", joinColumns={@joincolumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@joincolumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\jointable", type1);
        String type2 = types.get(new OffsetRange(60, 70));
        assertEquals("joincolumn", type2);
        String type3 = types.get(new OffsetRange(137, 147));
        assertEquals("joincolumn", type3);
    }

    public void testValidUseCase_11() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinTable(name=\"users_phonenumbers\", joinColumns={@Baz\\JoinColumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@JoinColumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})");
        assertEquals("JoinTable", parsedLine.getName());
        assertEquals("(name=\"users_phonenumbers\", joinColumns={@Baz\\JoinColumn(name=\"user_id\", referencedColumnName=\"id\")}, inverseJoinColumns={@JoinColumn(name=\"phonenumber_id\", referencedColumnName=\"id\", unique=true)})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\JoinTable", type1);
        String type2 = types.get(new OffsetRange(60, 74));
        assertEquals("Baz\\JoinColumn", type2);
        String type3 = types.get(new OffsetRange(141, 151));
        assertEquals("JoinColumn", type3);
    }

}
