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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2OdmAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public Doctrine2OdmAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = Doctrine2OdmAnnotationLineParser.getDefault();
    }

    public void testBinParser() {
        assertNotNull(parser.parse("Bin"));
    }

    public void testBinCustomParser() {
        assertNotNull(parser.parse("BinCustom"));
    }

    public void testBinFuncParser() {
        assertNotNull(parser.parse("BinFunc"));
    }

    public void testBinMD5Parser() {
        assertNotNull(parser.parse("BinMD5"));
    }

    public void testBinUUIDParser() {
        assertNotNull(parser.parse("BinUUID"));
    }

    public void testBooleanParser() {
        assertNotNull(parser.parse("Boolean"));
    }

    public void testDateParser() {
        assertNotNull(parser.parse("Date"));
    }

    public void testDistanceParser() {
        assertNotNull(parser.parse("Distance"));
    }

    public void testEmbeddedDocumentParser() {
        assertNotNull(parser.parse("EmbeddedDocument"));
    }

    public void testFileParser() {
        assertNotNull(parser.parse("File"));
    }

    public void testFloatParser() {
        assertNotNull(parser.parse("Float"));
    }

    public void testHashParser() {
        assertNotNull(parser.parse("Hash"));
    }

    public void testIdParser() {
        assertNotNull(parser.parse("Id"));
    }

    public void testIncrementParser() {
        assertNotNull(parser.parse("Increment"));
    }

    public void testIntParser() {
        assertNotNull(parser.parse("Int"));
    }

    public void testKeyParser() {
        assertNotNull(parser.parse("Key"));
    }

    public void testMappedSuperclassParser() {
        assertNotNull(parser.parse("MappedSuperclass"));
    }

    public void testNotSavedParser() {
        assertNotNull(parser.parse("NotSaved"));
    }

    public void testPreLoadParser() {
        assertNotNull(parser.parse("PreLoad"));
    }

    public void testPostLoadParser() {
        assertNotNull(parser.parse("PostLoad"));
    }

    public void testPostPersistParser() {
        assertNotNull(parser.parse("PostPersist"));
    }

    public void testPostRemoveParser() {
        assertNotNull(parser.parse("PostRemove"));
    }

    public void testPostUpdateParser() {
        assertNotNull(parser.parse("PostUpdate"));
    }

    public void testPrePersistParser() {
        assertNotNull(parser.parse("PrePersist"));
    }

    public void testPreRemoveParser() {
        assertNotNull(parser.parse("PreRemove"));
    }

    public void testPreUpdateParser() {
        assertNotNull(parser.parse("PreUpdate"));
    }

    public void testStringParser() {
        assertNotNull(parser.parse("String"));
    }

    public void testTimestampParser() {
        assertNotNull(parser.parse("Timestamp"));
    }

    public void testUniqueIndexParser() {
        assertNotNull(parser.parse("UniqueIndex"));
    }

    public void testAlsoLoadParser() {
        assertNotNull(parser.parse("AlsoLoad"));
    }

    public void testCollectionParser() {
        assertNotNull(parser.parse("Collection"));
    }

    public void testDiscriminatorFieldParser() {
        assertNotNull(parser.parse("DiscriminatorField"));
    }

    public void testFieldParser() {
        assertNotNull(parser.parse("Field"));
    }

    public void testInheritanceTypeParser() {
        assertNotNull(parser.parse("InheritanceType"));
    }

    public void testDiscriminatorMapParser() {
        assertNotNull(parser.parse("DiscriminatorMap"));
    }

    public void testEmbedOneParser() {
        assertNotNull(parser.parse("EmbedOne"));
    }

    public void testEmbedManyParser() {
        assertNotNull(parser.parse("EmbedMany"));
    }

    public void testReferenceOneParser() {
        assertNotNull(parser.parse("ReferenceOne"));
    }

    public void testReferenceManyParser() {
        assertNotNull(parser.parse("ReferenceMany"));
    }

    public void testDocumentParser() {
        assertNotNull(parser.parse("Document"));
    }

}
