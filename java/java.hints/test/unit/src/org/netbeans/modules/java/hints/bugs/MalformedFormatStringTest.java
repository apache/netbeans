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
