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
package org.netbeans.modules.php.symfony2.annotations.validators.parser;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SymfonyValidatorsAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public SymfonyValidatorsAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = SymfonyValidatorsAnnotationLineParser.getDefault();
    }

    public void testNotBlankParser() {
        assertNotNull(parser.parse("NotBlank"));
    }

    public void testBlankParser() {
        assertNotNull(parser.parse("Blank"));
    }

    public void testNotNullParser() {
        assertNotNull(parser.parse("NotNull"));
    }

    public void testNullParser() {
        assertNotNull(parser.parse("Null"));
    }

    public void testTrueParser() {
        assertNotNull(parser.parse("True"));
    }

    public void testFalseParser() {
        assertNotNull(parser.parse("False"));
    }

    public void testEmailParser() {
        assertNotNull(parser.parse("Email"));
    }

    public void testMinLengthParser() {
        assertNotNull(parser.parse("MinLength"));
    }

    public void testMaxLengthParser() {
        assertNotNull(parser.parse("MaxLength"));
    }

    public void testUrlParser() {
        assertNotNull(parser.parse("Url"));
    }

    public void testRegexParser() {
        assertNotNull(parser.parse("Regex"));
    }

    public void testIpParser() {
        assertNotNull(parser.parse("Ip"));
    }

    public void testMaxParser() {
        assertNotNull(parser.parse("Max"));
    }

    public void testMinParser() {
        assertNotNull(parser.parse("Min"));
    }

    public void testDateParser() {
        assertNotNull(parser.parse("Date"));
    }

    public void testDateTimeParser() {
        assertNotNull(parser.parse("DateTime"));
    }

    public void testTimeParser() {
        assertNotNull(parser.parse("Time"));
    }

    public void testChoiceParser() {
        assertNotNull(parser.parse("Choice"));
    }

    public void testCollectionParser() {
        assertNotNull(parser.parse("Collection"));
    }

    public void testUniqueEntityParser() {
        assertNotNull(parser.parse("UniqueEntity"));
    }

    public void testLanguageParser() {
        assertNotNull(parser.parse("Language"));
    }

    public void testLocaleParser() {
        assertNotNull(parser.parse("Locale"));
    }

    public void testCountryParser() {
        assertNotNull(parser.parse("Country"));
    }

    public void testFileParser() {
        assertNotNull(parser.parse("File"));
    }

    public void testImageParser() {
        assertNotNull(parser.parse("Image"));
    }

    public void testCallbackParser() {
        assertNotNull(parser.parse("Callback"));
    }

    public void testAllParser() {
        assertNotNull(parser.parse("All"));
    }

    public void testValidParser() {
        assertNotNull(parser.parse("Valid"));
    }

}
