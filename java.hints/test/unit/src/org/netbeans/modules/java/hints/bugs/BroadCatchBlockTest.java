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
     * parents are not reported for neigher 'umbrellas' or normal exceptions.
     * 
     */
    private static final String twoSubexceptionsGeneric = "14:27-14:46:verifier:The catch(java.lang.RuntimeException) is too broad, it catches the following exception types: java.lang.IllegalArgumentException and java.lang.SecurityException";
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
    
    public void testTwoSubexceptionsGeneric7() throws Exception {
        final String warnTxt = twoSubexceptionsGeneric;
        createHintTest("TwoExceptions").
            sourceLevel("1.7").
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
            sourceLevel("1.7").
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
