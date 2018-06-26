/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        final File sourceFile = new File("/home/b.php");
        final String abs = "ABS/";
        final String rel = "REL/";
        final String suff = "/SUFF";

        String relPath = PhpUnit.getRelPath(testFile, sourceFile, abs, rel, suff, false);
        assertEquals(rel + ".." + sourceFile.getAbsolutePath() + suff, relPath);

        relPath = PhpUnit.getRelPath(testFile, sourceFile, abs, rel, suff, true);
        assertEquals(abs + sourceFile.getAbsolutePath() + suff, relPath);
    }

}
