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
