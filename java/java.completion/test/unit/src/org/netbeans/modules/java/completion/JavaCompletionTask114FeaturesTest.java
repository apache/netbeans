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
package org.netbeans.modules.java.completion;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
/**
 *
 * @author arusinha
 */
public class JavaCompletionTask114FeaturesTest extends CompletionTestBase {

    private static final String SOURCE_LEVEL = "14"; //NOI18N

    public JavaCompletionTask114FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
            suite.addTestSuite(JavaCompletionTask114FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_13, skip tests
            suite.addTest(new JavaCompletionTask114FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    public void testBindingUse() throws Exception {
        performTest("GenericMethodInvocation", 1231, "boolean b = argO instanceof String str && st", "BindingUse.pass", SOURCE_LEVEL);
    }
    

    public void testBeforeLeftRecordBraces() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("Records", 896, null, "implementsKeyword.pass", getLatestSource());
    }
        
    public void testBeforeRecParamsLeftParen() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("Records", 892, null, "empty.pass", getLatestSource());
    }

    public void testAfterTypeParamInRecParam() throws Exception {
        performTest("Records", 890, null, "extendsKeyword.pass", SOURCE_LEVEL);
    }
    
    public void testInsideRecAfterStaticKeyWord() throws Exception {
        performTest("Records", 918, "R", "typesRecordStaticMembersAndVars.pass", SOURCE_LEVEL);
    }
    
    public void testAnnotationInRecordParam() throws Exception {
        performTest("Records", 999, null, "override.pass", SOURCE_LEVEL);
    } 
    
    public void testRecordKeywordInsideMethod() throws Exception {
        performTest("Records", 1014, "rec", "record.pass", SOURCE_LEVEL);
    }

    public void testRecordKeywordInsideClass() throws Exception {
        performTest("Records", 844, "rec", "record.pass", SOURCE_LEVEL);
    }

    public void testRecordKeywordInsideMethodIfPrefixDoesntMatch() throws Exception {
        performTest("Records", 1014, "someprefix", "empty.pass", SOURCE_LEVEL);
    }

    public void testRecordKeywordInsideClassIfPrefixDoesntMatch() throws Exception {
        performTest("Records", 844, "someprefix", "empty.pass", SOURCE_LEVEL);
    }
    
    public void testVariableNameSuggestion() throws Exception {
        TestCompilerOptionsQueryImplementation.EXTRA_OPTIONS.add("--enable-preview");
        performTest("Records", 1071, null, "recordVariableSuggestion.pass", getLatestSource());
    } 
    
    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
    private String getLatestSource(){
        return SourceVersion.latest().name().substring(SourceVersion.latest().name().indexOf("_")+1);
    }
    @ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
    public static class TestCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

        private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return new CompilerOptionsQueryImplementation.Result() {
                @Override
                public List<? extends String> getArguments() {
                    return EXTRA_OPTIONS;
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }

    }
}
