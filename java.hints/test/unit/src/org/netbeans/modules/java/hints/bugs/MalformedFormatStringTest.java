/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class MalformedFormatStringTest extends NbTestCase {
    public MalformedFormatStringTest(String name) {
        super(name);
    }
    
    public void testUnknownConversion() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%m\"));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:41:verifier:Malformed format string: Unknown format specifier: `m'");
    }
    
    public void testDuplicateFlags() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%00d\", 2));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:43:verifier:Malformed format string: Duplicate flag: `0'");
    }
    
    public void testIllegalWidth() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%(-5d\", 2));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings();
    }
    
    public void testIllegalPrecision() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%3.-5d\", 2));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:45:verifier:Malformed format string: Unknown format specifier: `3'");
    }
    
    public void testMissingFormatWidth() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%-d\", 2));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:42:verifier:Malformed format string: Width must be specified in `%-d'");
    }
    
    public void testMismatchedFlags() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%+s\", 2));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:42:verifier:Malformed format string: Flags `+' do not match the conversion `s'");
    }
    
    public void testTooFewArguments() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%s-%s\", \"foo\"));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:44:verifier:Too few parameters passed to format. Format string requires: 2, actual: 1");
    }
    
    public void testArgumentIndexTooBig() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%s-%3$s\", \"foo\", \"bar\"));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:37-4:46:verifier:Too few parameters passed to format. Format string requires: 3, actual: 2");
    }

    public void testInappropriateNumberString() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%d\", \"foo\", \"bar\"));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings("4:43-4:48:verifier:Invalid value type `String' for format specifier `%d', parameter 1");
    }
    
    public void testOKIntegerConversion() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%d,%d,%d,%d\", 10,(byte)10, (short)10, (long)10));\n" +
"    System.err.println(String.format(\"%d,%d,%d,%d\", Integer.valueOf(10), Byte.valueOf((byte)10), Short.valueOf((short)10), Long.valueOf((long)10)));\n" +
                
"    System.err.println(String.format(\"%o,%o,%o,%o\", 10,(byte)10, (short)10, (long)10));\n" +
"    System.err.println(String.format(\"%o,%o,%o,%o\", Integer.valueOf(10), Byte.valueOf((byte)10), Short.valueOf((short)10), Long.valueOf((long)10)));\n" +
                
"    System.err.println(String.format(\"%x,%x,%x,%x\", 10,(byte)10, (short)10, (long)10));\n" +
"    System.err.println(String.format(\"%x,%x,%x,%x\", Integer.valueOf(10), Byte.valueOf((byte)10), Short.valueOf((short)10), Long.valueOf((long)10)));\n" +
                
"    System.err.println(String.format(\"%X,%X,%X,%X\", 10,(byte)10, (short)10, (long)10));\n" +
"    System.err.println(String.format(\"%X,%X,%X,%X\", Integer.valueOf(10), Byte.valueOf((byte)10), Short.valueOf((short)10), Long.valueOf((long)10)));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings();
    }
    
    public void testOKFloatingPointConversions() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    System.err.println(String.format(\"%e,%e,%e,%e\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%E,%E,%E,%E\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%g,%g,%g,%g\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%G,%G,%G,%G\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%f,%f,%f,%f\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%a,%a,%a,%a\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"    System.err.println(String.format(\"%A,%A,%A,%A\", 10f, 10d, Float.valueOf(10f), Double.valueOf(10d)));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings();
    }
    
    public void testBigIntegerOKConversion() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    final int v = 1;\n" +
"    public final int recurse(int var) {\n" +
"    java.math.BigInteger big = java.math.BigInteger.valueOf(10l);\n" +
"    System.err.println(String.format(\"%d,%o,%x,%X,%e,%E,%g,%G,%f\", big, big, big, big,big, big, big, big, big));\n" +
"        return 0;\n" +
"    } \n" +
"}"
                )
                .run(MalformedFormatString.class)
                .assertWarnings();
    }
    

    public void testTemplate() throws Exception {
        HintTest.create()
                .input(
                ""
                )
                .run(MalformedFormatString.class)
                .assertWarnings();
    }
}
