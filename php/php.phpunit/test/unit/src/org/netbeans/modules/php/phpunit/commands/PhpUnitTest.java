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

package org.netbeans.modules.php.phpunit.commands;

import java.io.File;
import java.util.regex.Matcher;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tomas Mysik
 */
public class PhpUnitTest extends NbTestCase {

    public PhpUnitTest(String name) {
        super(name);
    }

    public void testOutLinePatternTestRunner() {
        Matcher matcher = PhpUnit.OUT_LINE_PATTERN.matcher("/home/gapon/test/Calculator.php:635");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/test/Calculator.php", matcher.group(1));
        assertEquals("635", matcher.group(2));
        assertTrue(PhpUnit.OUT_LINE_PATTERN.matcher("/h o m e/gapon/test/Calculator.php:635").matches());
        assertTrue(PhpUnit.OUT_LINE_PATTERN.matcher("C:\\home\\gapon\\test\\Calculator.php:635").matches());

        assertFalse(PhpUnit.OUT_LINE_PATTERN.matcher("").matches());
    }

    public void testOutLinePatternOutput() {
        Matcher matcher = PhpUnit.OUT_LINE_PATTERN.matcher("0.1077    6609264   6. PHPUnit_Util_Fileloader::checkAndLoad() /usr/share/php/PHPUnit/Framework/TestSuite.php:385");
        assertTrue(matcher.matches());
        assertEquals("/usr/share/php/PHPUnit/Framework/TestSuite.php", matcher.group(1));
        assertEquals("385", matcher.group(2));

        assertFalse(PhpUnit.ERR_LINE_PATTERN.matcher("").matches());
    }

    public void testErrLineNumberPatternOutput() {
        Matcher matcher = PhpUnit.ERR_LINE_PATTERN.matcher("#0 /home/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/src/Util/Fileloader.php(56):"
                + " include_once()");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/src/Util/Fileloader.php", matcher.group(1));
        assertEquals("56", matcher.group(2));

        assertTrue(PhpUnit.ERR_LINE_PATTERN.matcher("#0 /h o m e/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/src/Util/Fileloader.php(56):"
                + " include_once() and once more").matches());
        assertTrue(PhpUnit.ERR_LINE_PATTERN.matcher("#0 C:\\home\\gapon\\NetBeansProjects\\Calculator-PHPUnit1\\vendor\\phpunit\\phpunit\\src\\Util\\"
                + "Fileloader.php(56): include_once()").matches());
    }

    public void testErrLineInPatternOutput() {
        Matcher matcher = PhpUnit.ERR_LINE_PATTERN.matcher("# in /home/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/tests/Regression/GitHub/"
                + "873/Issue873Test.php on line 7");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/tests/Regression/GitHub/873/Issue873Test.php", matcher.group(1));
        assertEquals("7", matcher.group(2));

        assertTrue(PhpUnit.ERR_LINE_PATTERN.matcher("# in /h o m e/gapon/NetBeansProjects/Calculator-PHPUnit1/vendor/phpunit/phpunit/tests/Regression/GitHub/"
                + "873/Issue873Test.php on line 7").matches());
        assertTrue(PhpUnit.ERR_LINE_PATTERN.matcher("# in C:\\home\\gapon\\NetBeansProjects\\Calculator-PHPUnit1\\vendor\\phpunit\\phpunit\\tests\\Regression\\GitHub\\"
                + "873\\Issue873Test.php on line 7").matches());
    }

    public void testRelPath() {
        final File testFile = new File("/tmp/a.php");
        final String sourcePath = "/home/b.php";
        final File sourceFile = new File(sourcePath);
        final String abs = "ABS/";
        final String rel = "REL/";
        final String suff = "/SUFF";

        String relPath = PhpUnit.getRelPath(testFile, sourceFile, abs, rel, suff, false);
        // Need to compare with source path because on Windows path will start with drive letter.
        assertEquals(rel + ".." + sourcePath + suff, relPath);

        relPath = PhpUnit.getRelPath(testFile, sourceFile, abs, rel, suff, true);
        // Need to replace on Windows "\\" with "/" because relativizeFile always uses "/" as separator.
        assertEquals(abs + sourceFile.getAbsolutePath().replace("\\", "/") + suff, relPath);
    }

}
