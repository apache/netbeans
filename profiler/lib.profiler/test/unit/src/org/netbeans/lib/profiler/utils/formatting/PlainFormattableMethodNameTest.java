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

package org.netbeans.lib.profiler.utils.formatting;

import junit.framework.TestCase;


/**
 * TestCase for Method Name formatter class.
 *
 * @author Ian Formanek
 */
public final class PlainFormattableMethodNameTest extends TestCase {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String[] patternSources = {
                                                       "java/lang/String", "concat", "(Ljava.lang.String;)Ljava.lang.String;",
                                                       "java/lang/String", "getBytes", "()[B", "classNameInDefaultPackage", "doIt",
                                                       "()V", "foo/bar/LongArrays", "createArray", "([[[[[[[[I)[[[[[[[[[F",
                                                       "foo/bar/Constr", "", "()V", "foo/bar/Constr", "<init>", "()V",
                                                       "foo/bar/Constr", null, null, "foo/bar/Constr", "<clinit>", null,
                                                   };
    private static final String[] patternResults = {
                                                       "java.lang.String", // formattedClass
    "java.lang.String.concat(java.lang.String)", // formattedClassAndMethod
    "concat(java.lang.String)", // formattedMethod
    "java.lang.String.concat(java.lang.String) : java.lang.String", // fullFormattedClassAndMethod
    "concat(java.lang.String) : java.lang.String", // fullFormattedMethod
    "java.lang.String", // paramsString
    "java.lang.String", // returnType
    "java.lang.String", // formattedClass
    "java.lang.String.getBytes()", // formattedClassAndMethod
    "getBytes()", // formattedMethod
    "java.lang.String.getBytes() : byte[]", // fullFormattedClassAndMethod
    "getBytes() : byte[]", // fullFormattedMethod
    "", // paramsString
    "byte[]", // returnType
    "classNameInDefaultPackage", // formattedClass
    "classNameInDefaultPackage.doIt()", // formattedClassAndMethod
    "doIt()", // formattedMethod
    "classNameInDefaultPackage.doIt() : void", // fullFormattedClassAndMethod
    "doIt() : void", // fullFormattedMethod
    "", // paramsString
    "void", // returnType
    "foo.bar.LongArrays", // formattedClass
    "foo.bar.LongArrays.createArray(int[][][][][][][][])", // formattedClassAndMethod
    "createArray(int[][][][][][][][])", // formattedMethod
    "foo.bar.LongArrays.createArray(int[][][][][][][][]) : float[][][][][][][][][]", // fullFormattedClassAndMethod
    "createArray(int[][][][][][][][]) : float[][][][][][][][][]", // fullFormattedMethod
    "int[][][][][][][][]", // paramsString
    "float[][][][][][][][][]", // returnType
    "foo.bar.Constr", // formattedClass
    "foo.bar.Constr", // formattedClassAndMethod
    "", // formattedMethod
    "foo.bar.Constr", // fullFormattedClassAndMethod
    "", // fullFormattedMethod
    "", // paramsString
    "", // returnType
    "foo.bar.Constr", // formattedClass
    "foo.bar.Constr.<init>()", // formattedClassAndMethod
    "<init>()", // formattedMethod
    "foo.bar.Constr.<init>()", // fullFormattedClassAndMethod
    "<init>()", // fullFormattedMethod
    "", // paramsString
    "", // returnType
    "foo.bar.Constr", // formattedClass
    "foo.bar.Constr", // formattedClassAndMethod
    "", // formattedMethod
    "foo.bar.Constr", // fullFormattedClassAndMethod
    "", // fullFormattedMethod
    "", // paramsString
    "", // returnType
    "foo.bar.Constr", // formattedClass
    "foo.bar.Constr.<clinit>()", // formattedClassAndMethod
    "<clinit>()", // formattedMethod
    "foo.bar.Constr.<clinit>()", // fullFormattedClassAndMethod
    "<clinit>()", // fullFormattedMethod
    "", // paramsString
    "", // returnType
                                                   };

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Test to ensure that the formatting of test methods is as expected.
     */
    public void testFormatting() {
        PlainFormattableMethodName mnf;

        int count = 0;

        for (int i = 0; (i + 2) < patternSources.length; i += 3) {
            mnf = new PlainFormattableMethodName(patternSources[i], patternSources[i + 1], patternSources[i + 2], 0);

            assertEquals("Wrong results for getFormattedClass " + mnf, patternResults[count++], mnf.getFormattedClass());
            assertEquals("Wrong results for getFormattedClassAndMethod " + mnf, patternResults[count++],
                         mnf.getFormattedClassAndMethod());
            assertEquals("Wrong results for getFormattedMethod " + mnf, patternResults[count++], mnf.getFormattedMethod());
            assertEquals("Wrong results for getFullFormattedClassAndMethod " + mnf, patternResults[count++],
                         mnf.getFullFormattedClassAndMethod());
            assertEquals("Wrong results for getFullFormattedMethod " + mnf, patternResults[count++], mnf.getFullFormattedMethod());
            assertEquals("Wrong results for getParamsString " + mnf, patternResults[count++], mnf.getParamsString());
            assertEquals("Wrong results for getReturnType " + mnf, patternResults[count++], mnf.getReturnTypeX());
        }
    }

    protected void setUp() throws Exception {
    }
}
