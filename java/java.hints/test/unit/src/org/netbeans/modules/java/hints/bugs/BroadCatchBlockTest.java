/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.bugs;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class BroadCatchBlockTest extends NbTestCase {
    public static final String FIX_SPECIFIC_CATCH = "Use specific type in catch";
    private String fileName;
    
    public BroadCatchBlockTest(String name) {
        super(name);
    }

    private HintTest createHintTest(String n) throws Exception {
        this.fileName = n;
        return HintTest.create().input("org/netbeans/test/java/hints/BroadCatchBlockTest/" + n + ".java", code(n));
    }
    
    private String c() throws Exception { return code(fileName); }
    
    private String code(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("org/netbeans/test/java/hints/BroadCatchBlockTest/" + name + ".java").asText();
    }
    
    private String g() throws Exception { return golden(fileName); }
    
    private String golden(String name) throws IOException {
        FileObject f = FileUtil.toFileObject(getDataDir());
        return f.getFileObject("goldenfiles/org/netbeans/test/java/hints/BroadCatchBlockTest/" + name + ".java").asText();
    }
    
    private String f() { return f(fileName); }
    
    private String f(String name) {
        return "org/netbeans/test/java/hints/BroadCatchBlockTest/" + name + ".java";
    }
    
    /**
     * Checks that if a super exception masks just one more specific one,
     * hint appears.
     */
    public void testSingleParentException() throws Exception {
        final String warnTxt = "12:27-12:58:verifier:The catch(java.lang.ReflectiveOperationException) is too broad, the actually caught exception is java.lang.NoSuchMethodException";
        createHintTest("OneException").
            run(BroadCatchBlock.class).
            assertWarnings(warnTxt).
            findWarning(warnTxt).
            assertFixes(FIX_SPECIFIC_CATCH).
            applyFix().assertCompilable(f()).
            assertOutput(f(), g());
    }
    
    /**
     * Checks that generic exception is reported even though 
     * it corresponds to > 1 subclass. It also checks that common
     * parents are not reported for neither 'umbrellas' or normal exceptions.
     */
    private static final String twoSubexceptionsGeneric = "19:27-19:46:verifier:The catch(java.lang.RuntimeException) is too broad, it catches the following exception types: java.lang.IllegalArgumentException and java.lang.SecurityException";
    public void testTwoSubexceptionsGeneric() throws Exception {
        final String warnTxt = twoSubexceptionsGeneric;
        createHintTest("TwoExceptions").
            run(BroadCatchBlock.class).
            assertWarnings(warnTxt).
            findWarning(warnTxt).
            assertFixes(FIX_SEPARATE_CATCHES).
            applyFix().assertCompilable(f()).
            assertOutput(f(), g());
    }
    
    public void testTwoSubexceptionsGeneric2() throws Exception {
        final String warnTxt = twoSubexceptionsGeneric;
        createHintTest("TwoExceptions").
            sourceLevel(8).
            run(BroadCatchBlock.class).
            assertWarnings(warnTxt).
            findWarning(warnTxt).
            assertFixes(FIX_SPECIFIC_CATCH2, FIX_SEPARATE_CATCHES).
            applyFix(FIX_SPECIFIC_CATCH2).assertCompilable(f()).
            assertOutput(f(), golden("TwoExceptionsMulti"));
    }
    
    private static final String FIX_SEPARATE_CATCHES = "FIX_UseSpecificCatchSplit";
    private static final String FIX_SPECIFIC_CATCH2 = "FIX_UseSpecificCatch";

    private static final String[] warn_twoCommonParents = {
            "19:17-19:48:verifier:The catch(java.lang.ReflectiveOperationException) is too broad, it catches the following exception types: java.lang.IllegalAccessException and java.lang.reflect.InvocationTargetException",
            "28:17-28:35:verifier:The catch(java.io.IOException) is too broad, it catches the following exception types: java.io.FileNotFoundException and java.net.MalformedURLException"
    };

    /**
     * Common super-exceptions are suppressed
     */
    public void testCommonParentSuppressed() throws Exception {
        createHintTest("TwoExceptionsCommon").
            run(BroadCatchBlock.class).
            assertWarnings();
    }
    
    public void testCommonParentsWithoutUmbrellas() throws Exception {
        createHintTest("TwoExceptionsCommon").
            preference(BroadCatchBlock.OPTION_EXCLUDE_COMMON, false).
            preference(BroadCatchBlock.OPTION_EXCLUDE_UMBRELLA, true).
            run(BroadCatchBlock.class).
            assertWarnings(warn_twoCommonParents[0]);
    }

    /**
     * Checks that with 'report common parents', an exception that
     * masks > 1 subtype is reported
     */
    public void testTwoCommonParentFixSeparateCatches() throws Exception {
        createHintTest("TwoExceptionsCommon").
            preference(BroadCatchBlock.OPTION_EXCLUDE_COMMON, false).
            run(BroadCatchBlock.class).
            assertWarnings(warn_twoCommonParents).
            findWarning(warn_twoCommonParents[0]).
            assertFixes(FIX_SEPARATE_CATCHES).
            applyFix().assertCompilable(f()).
            assertOutput(f(), golden("TwoExceptionsCommon"));
    }
    
    public void testTwoCommonParentFixMultiCatch() throws Exception {
        createHintTest("TwoExceptionsCommon").
            preference(BroadCatchBlock.OPTION_EXCLUDE_COMMON, false).
            sourceLevel(8).
            run(BroadCatchBlock.class).
            assertWarnings(warn_twoCommonParents).
            findWarning(warn_twoCommonParents[0]).
            assertFixes(FIX_SPECIFIC_CATCH2, FIX_SEPARATE_CATCHES).
            applyFix(FIX_SPECIFIC_CATCH2).assertCompilable(f()).
            assertOutput(f(), golden("TwoExceptionsCommonMulti"));
    }
    
    /**
     * Checks that although the exception is 'umbrella', it will be reported
     * if it can be just narrowed.
     */
    public void testUmbrellaWithOneSubtype() throws Exception {
        final String warnTxt = "9:17-9:35:verifier:The catch(java.io.IOException) is too broad, the actually caught exception is java.io.FileNotFoundException";
        createHintTest("SingleUmbrella").
            run(BroadCatchBlock.class).
            assertWarnings(warnTxt).
            findWarning(warnTxt).
            assertFixes(FIX_SPECIFIC_CATCH).
            applyFix().assertCompilable(f()).
            assertOutput(f(), g());
    }
}
