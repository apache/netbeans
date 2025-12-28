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

package org.netbeans.modules.java.completion;

import java.util.concurrent.CountDownLatch;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 * @author aksinsin
 */
public class JavaCompletionTask121FeaturesTest extends CompletionTestBase {

    private static final String SOURCE_LEVEL = "21"; //NOI18N
    private ClassPath classPathRegistered;

    public JavaCompletionTask121FeaturesTest(String testName) {
        super(testName);
    }

    public void testRecordPatternCompletion_1() throws Exception {
        performTest("RecordPattern", 930, null, "AutoCompletion_RecordPattern_1.pass", SOURCE_LEVEL);
    }

    public void testRecordPatternCompletion_2() throws Exception {
        performTest("RecordPattern", 1013, null, "AutoCompletion_RecordPattern_2.pass", SOURCE_LEVEL);
    }

    public void testRecordPatternCompletion_3() throws Exception {
        performTest("RecordPattern", 1014, null, "AutoCompletion_RecordPattern_3.pass", SOURCE_LEVEL);
    }

    public void testRecordPatternCompletion_4() throws Exception {
        performTest("RecordPattern", 1095, null, "AutoCompletion_RecordPattern_4.pass", SOURCE_LEVEL);
    }

    public void testRecordPatternCompletion_5() throws Exception {
        performTest("RecordPattern", 1107, null, "AutoCompletion_RecordPattern_5.pass", SOURCE_LEVEL);
    }

    public void testCaseLabels_1() throws Exception {
        performTest("SwitchPatternMatching", 971, null, "AutoCompletion_CaseLabels_PatternMatchingSwitch_1.pass", SOURCE_LEVEL);
    }

    public void testCaseLabels_2() throws Exception {
        performTest("SwitchPatternMatching", 975, ", ", "defaultKeyword.pass", SOURCE_LEVEL);
    }

    public void testCaseLabels_3() throws Exception {
        performTest("SwitchPatternMatching", 976, null, "empty.pass", SOURCE_LEVEL);
    }

    public void testCaseLabels_4() throws Exception {
        performTest("SwitchPatternMatching", 1015, "case default, ", "empty.pass", SOURCE_LEVEL);
    }

    public void testVariableNameSuggestion() throws Exception {
        performTest("SwitchPatternMatching", 1033, null, "AutoCompletion_VarNameSuggestion_PatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void testCasePatternGuard_1() throws Exception {
        performTest("SwitchPatternMatching", 1035, null, "AutoCompletion_Guard_PatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void testCasePatternGuard_2() throws Exception {
        performTest("SwitchPatternMatching", 1127, null, "AutoCompletion_Guard_PatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void testCasePatternGuard_3() throws Exception {
        performTest("SwitchPatternMatching", 1040, null, "AutoCompletion_Guard_PatternMatchingSwitch_1.pass", SOURCE_LEVEL);
    }

    public void testClassMembersAutoCompletion_GuardedPattern() throws Exception {
        performTest("SwitchPatternMatching", 1042, null, "AutoCompletion_MembersSelect_GuardedPatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void testClassMembersAutoCompletion_GuardedPattern_1() throws Exception {
        performTest("SwitchPatternMatching", 1050, null, "AutoCompletion_MembersSelect_GuardedPatternMatchingSwitch_1.pass", SOURCE_LEVEL);
    }

    public void testCaseRecordPattern_1() throws Exception {
        performTest("SwitchPatternMatching", 1108, null, "AutoCompletion_CaseRecordPattern_1.pass", SOURCE_LEVEL);
    }

    public void testCaseRecordPattern_2() throws Exception {
        performTest("SwitchPatternMatching", 1109, null, "AutoCompletion_CaseRecordPattern_2.pass", SOURCE_LEVEL);
    }

    public void testCaseRecordPattern_3() throws Exception {
        performTest("SwitchPatternMatching", 1113, null, "AutoCompletion_CaseRecordPattern_3.pass", SOURCE_LEVEL);
    }

    public void testCaseRecordPattern_4() throws Exception {
        performTest("SwitchPatternMatching", 1134, null, "AutoCompletion_CaseRecordPattern_4.pass", SOURCE_LEVEL);
    }

    public void testCaseBodyCompletion_GuardedPattern() throws Exception {
        performTest("SwitchPatternMatching", 1137, null, "AutoCompletion_CaseBody_PatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void TODOtestNoCrash() throws Exception {
        performTest("SwitchPatternMatching", 1275, "case R(var s,", "AutoCompletion_Guard_PatternMatchingSwitch.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitch1() throws Exception {
        performTest("SwitchWithSealedType", 1084, null, "sealedTypeSwitch.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitch2() throws Exception {
        performTest("SwitchWithSealedType", 1084, "java.lang.", "javaLangSubPackages.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitch3() throws Exception {
        performTest("SwitchWithSealedType", 1084, "java.lang.annotation.", "empty.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchTypeFiltering() throws Exception {
        performTest("SwitchWithSealedType", 1084, "J j -> {} case ", "sealedTypeSwitchJFiltered.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchTypeFilteringGuard() throws Exception {
        performTest("SwitchWithSealedType", 1084, "J j when j == null -> {} case ", "sealedTypeSwitch.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchTypeFilteringQualified() throws Exception {
        performTest("SwitchWithSealedType", 1084, "J j -> {} case test.Test.", "sealedTypeSwitchJFilteredQualified.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchTypeFilteringQualifiedPackage() throws Exception {
        performTest("SwitchWithSealedType", 1084, "J j -> {} case test.", "sealedTypeSwitchJFilteredQualifiedPackage.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchEnumFiltering() throws Exception {
        performTest("SwitchWithSealedType", 1084, "EJ.A -> {} case ", "sealedTypeSwitchEJAFiltered.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchEnumFilteringQualified1() throws Exception {
        performTest("SwitchWithSealedType", 1084, "EJ.A -> {} case test.Test.", "sealedTypeSwitchEJAFilteredQualified1.pass", SOURCE_LEVEL);
    }

    public void testSealedTypeSwitchEnumFilteringQualified2() throws Exception {
        performTest("SwitchWithSealedType", 1084, "EJ.A -> {} case test.Test.EJ.", "sealedTypeSwitchEJAFilteredQualified2.pass", SOURCE_LEVEL);
    }

    public void testNonSealedTypeSwitch1() throws Exception {
        performTest("SwitchWithNonSealedType", 1070, null, "nonSealedTypeSwitch.pass", SOURCE_LEVEL);
    }

    @Override
    protected void afterTestSetup() throws Exception {
        if (getName().startsWith("testSealed")) {
            classPathRegistered = ClassPathSupport.createClassPath(getWorkDir().toURI().toURL());
            CountDownLatch started = new CountDownLatch(1);
            ClasspathInfo info = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, classPathRegistered);
            ClassIndexListener listener = new ClassIndexListener() {
                @Override
                public void typesAdded(TypesEvent event) {}
                @Override
                public void typesRemoved(TypesEvent event) {}
                @Override
                public void typesChanged(TypesEvent event) {}
                @Override
                public void rootsAdded(RootsEvent event) {
                    started.countDown();
                }
                @Override
                public void rootsRemoved(RootsEvent event) {}
            };
            info.getClassIndex().addClassIndexListener(listener);
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {classPathRegistered});
            IndexingManager.getDefault().refreshAllIndices(true, true, getWorkDir());
            started.await();
            info.getClassIndex().removeClassIndexListener(listener);
            SourceUtils.waitScanFinished();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (classPathRegistered != null) {
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {classPathRegistered});
            SourceUtils.waitScanFinished();
        }
        super.tearDown();
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
