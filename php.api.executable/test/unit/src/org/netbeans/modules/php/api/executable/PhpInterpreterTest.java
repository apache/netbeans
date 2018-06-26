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

package org.netbeans.modules.php.api.executable;

import java.util.regex.Matcher;
import org.netbeans.junit.NbTestCase;

public class PhpInterpreterTest extends NbTestCase {

    public PhpInterpreterTest(String name) {
        super(name);
    }

    public void testLinePattern0() {
        Matcher matcher = PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in /home/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 10");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject1/Calculator.php", matcher.group(1));
        assertEquals("10", matcher.group(2));
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in /h o m e/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 10").matches());
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in C:\\home\\gapon\\NetBeansProjects\\PhpProject1\\Calculator.php on line 10").matches());
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Exception: hello world in /home/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 16").matches());

        assertFalse(PhpInterpreter.LINE_PATTERNS[0].matcher("").matches());
    }

    public void testLinePattern1() {
        Matcher matcher = PhpInterpreter.LINE_PATTERNS[1].matcher("    0.0002     115808   1. {main}() /home/gapon/NetBeansProjects/PhpProject1/Calculator.php:0");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject1/Calculator.php", matcher.group(1));
        assertEquals("0", matcher.group(2));
        assertTrue(PhpInterpreter.LINE_PATTERNS[1].matcher("    0.0002     115808   1. {main}() /h o m e/gapon/NetBeansProjects/PhpProject1/Calculator.php:0").matches());

        assertFalse(PhpInterpreter.LINE_PATTERNS[1].matcher("").matches());
    }

}
