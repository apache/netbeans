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
