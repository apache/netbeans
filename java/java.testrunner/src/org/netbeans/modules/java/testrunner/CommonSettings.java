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

package org.netbeans.modules.java.testrunner;

import java.util.prefs.Preferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Options for JUnit module, control behavior of test creation and execution.
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
public class CommonSettings {
    private static final CommonSettings INSTANCE = new CommonSettings();
    
    /** prefix for names of generated test classes */
    public static final String TEST_CLASSNAME_PREFIX = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_test_classname_prefix");                //NOI18N
    /** suffix for names of generated test classes */
    public static final String TEST_CLASSNAME_SUFFIX = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_test_classname_suffix");                //NOI18N
    /** suffix for names of generated integration test classes */
    public static final String INTEGRATION_TEST_CLASSNAME_SUFFIX = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_integration_test_classname_suffix");                //NOI18N
    /** prefix for names of generated test suites */
    public static final String SUITE_CLASSNAME_PREFIX = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_suite_classname_prefix");               //NOI18N
    /** suffix for names of generated test suites */
    public static final String SUITE_CLASSNAME_SUFFIX = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_suite_classname_suffix");               //NOI18N
    /** should it be possible to create tests for tests? */
    public static final boolean GENERATE_TESTS_FROM_TEST_CLASSES = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_generate_tests_from_test_classes").equals("true");    //NOI18N
    /** generate test initializer method by default? */
    public static final boolean DEFAULT_GENERATE_SETUP = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_generate_setUp_default").equals("true");              //NOI18N
    /** generate test finalizer method by default? */
    public static final boolean DEFAULT_GENERATE_TEARDOWN = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_generate_tearDown_default").equals("true");           //NOI18N
    /** generate test class initializer method by default? */
    public static final boolean DEFAULT_GENERATE_CLASS_SETUP = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_generate_class_setUp_default").equals("true");        //NOI18N
    /** generate test class finalizer method by default? */
    public static final boolean DEFAULT_GENERATE_CLASS_TEARDOWN = NbBundle.getMessage(
            CommonSettings.class,
            "PROP_generate_class_tearDown_default").equals("true");     //NOI18N

    // XXX this property has to go too - will not work any longer, need some src -> test query
    private static final String PROP_FILE_SYSTEM         = "fileSystem";
    public static final String PROP_MEMBERS_PUBLIC      = "membersPublic";
    public static final String PROP_MEMBERS_PROTECTED   = "membersProtected";
    public static final String PROP_MEMBERS_PACKAGE     = "membersPackage";
    public static final String PROP_BODY_COMMENTS       = "bodyComments";
    public static final String PROP_BODY_CONTENT        = "bodyContent";
    public static final String PROP_JAVADOC             = "javaDoc";
    public static final String PROP_GENERATE_EXCEPTION_CLASSES = "generateExceptionClasses";
    public static final String PROP_GENERATE_ABSTRACT_IMPL = "generateAbstractImpl";
    public static final String PROP_GENERATE_SUITE_CLASSES   = "generateSuiteClasses";
    
    public static final String PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES = "includePackagePrivateClasses";
    public static final String PROP_GENERATE_MAIN_METHOD = "generateMainMethod";
    public static final String PROP_GENERATE_MAIN_METHOD_BODY = "generateMainMethodBody";
    public static final String PROP_GENERATE_SETUP      = "generateSetUp";
    public static final String PROP_GENERATE_TEARDOWN   = "generateTearDown";
    public static final String PROP_GENERATE_CLASS_SETUP      = "generateClassSetUp";
    public static final String PROP_GENERATE_CLASS_TEARDOWN   = "generateClassTearDown";
    public static final String PROP_GENERATOR = "generator";
    public static final String PROP_ROOT_SUITE_CLASSNAME = "rootSuiteClassName";    
    public static final String PROP_GENERATE_INTEGRATION_TESTS   = "generateIntegrationTests";            

    public static final String PROP_RESULTS_SPLITPANE_DIVIDER = "resultsSplitDivider";
    
    public String displayName () {
        return NbBundle.getMessage (CommonSettings.class, "LBL_junit_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(CommonSettings.class); 
    }

    private  static Preferences getPreferences() {
        return NbPreferences.forModule(CommonSettings.class);
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    public static CommonSettings getDefault () {
        return INSTANCE;
    }

    public boolean isMembersPublic() {
        return getPreferences().getBoolean(PROP_MEMBERS_PUBLIC,true);
    }

    public void setMembersPublic(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PUBLIC,newVal);
    }

    public boolean isMembersProtected() {
        return getPreferences().getBoolean(PROP_MEMBERS_PROTECTED,true);
    }

    public void setMembersProtected(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PROTECTED,newVal);
    }

    public boolean isMembersPackage() {
        return getPreferences().getBoolean(PROP_MEMBERS_PACKAGE,true);
    }

    public void setMembersPackage(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PACKAGE,newVal);
    }

    public boolean isBodyComments() {
        return getPreferences().getBoolean(PROP_BODY_COMMENTS,true);
        
    }

    public void setBodyComments(boolean newVal) {
        getPreferences().putBoolean(PROP_BODY_COMMENTS,newVal);
    }

    public boolean isBodyContent() {
        return getPreferences().getBoolean(PROP_BODY_CONTENT,true);
    }

    public void setBodyContent(boolean newVal) {
        getPreferences().putBoolean(PROP_BODY_CONTENT,newVal);
    }

    public boolean isJavaDoc() {
        return getPreferences().getBoolean(PROP_JAVADOC,true);
    }

    public void setJavaDoc(boolean newVal) {
        getPreferences().putBoolean(PROP_JAVADOC,newVal);
    }
   
    public boolean isGenerateExceptionClasses() {
        return getPreferences().getBoolean(PROP_GENERATE_EXCEPTION_CLASSES,true);
    }

    public void setGenerateExceptionClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_EXCEPTION_CLASSES,newVal);
    }
    
   
    public boolean isGenerateAbstractImpl() {
     return getPreferences().getBoolean(PROP_GENERATE_ABSTRACT_IMPL,true);
    }

    public void setGenerateAbstractImpl(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_ABSTRACT_IMPL,newVal);
    }

    public boolean isGenerateSuiteClasses() {
        return getPreferences().getBoolean(PROP_GENERATE_SUITE_CLASSES,true);
    }

    public void setGenerateSuiteClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_SUITE_CLASSES,newVal);
    }

    
    public boolean isIncludePackagePrivateClasses() {
        return getPreferences().getBoolean(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,true);
    }

    public void setIncludePackagePrivateClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,newVal);
    }    
    
    public boolean isGenerateMainMethod() {
        return getPreferences().getBoolean(PROP_GENERATE_MAIN_METHOD,true);
    }

    public void setGenerateMainMethod(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_MAIN_METHOD,newVal);
    }
    
    public boolean isGenerateSetUp() {
        return getPreferences().getBoolean(PROP_GENERATE_SETUP,
                                           DEFAULT_GENERATE_SETUP);
    }

    public void setGenerateSetUp(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_SETUP,newVal);
    }
    
    public boolean isGenerateTearDown() {
        return getPreferences().getBoolean(PROP_GENERATE_TEARDOWN,
                                           DEFAULT_GENERATE_TEARDOWN);
    }

    public void setGenerateTearDown(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_TEARDOWN,newVal);
    }
    
    public boolean isGenerateClassSetUp() {
        return getPreferences().getBoolean(PROP_GENERATE_CLASS_SETUP,
                                           DEFAULT_GENERATE_CLASS_SETUP);
    }

    public void setGenerateClassSetUp(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_CLASS_SETUP, newVal);
    }
    
    public boolean isGenerateClassTearDown() {
        return getPreferences().getBoolean(PROP_GENERATE_CLASS_TEARDOWN,
                                           DEFAULT_GENERATE_CLASS_TEARDOWN);
    }

    public void setGenerateClassTearDown(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_CLASS_TEARDOWN, newVal);
    }
    
    public String getGenerator() {
        return null;//getPreferences().get(PROP_GENERATOR, DEFAULT_GENERATOR);
    }
    
    public void setGenerator(String generator) {
        getPreferences().put(PROP_GENERATOR, generator);
    }
    
    public String getGenerateMainMethodBody() {
        return getPreferences().get(PROP_GENERATE_MAIN_METHOD_BODY,
                NbBundle.getMessage(CommonSettings.class, "PROP_generate_main_method_body_default_value"));
    }

    public void setGenerateMainMethodBody(String newVal) {
        getPreferences().put(PROP_GENERATE_MAIN_METHOD_BODY,newVal);
    }
    
    public String getRootSuiteClassName() {        
        return getPreferences().get(PROP_ROOT_SUITE_CLASSNAME,
                NbBundle.getMessage(CommonSettings.class, "PROP_root_suite_classname_default_value"));
    }

    public void setRootSuiteClassName(String newVal) {
        getPreferences().put(PROP_ROOT_SUITE_CLASSNAME,newVal);
    }    

    public int getResultsSplitPaneDivider() {        
        return getPreferences().getInt(PROP_RESULTS_SPLITPANE_DIVIDER, -1);
    }

    public void setResultsSplitPaneDivider(int newVal) {
        getPreferences().putInt(PROP_RESULTS_SPLITPANE_DIVIDER, newVal);
    }    

    public boolean isGenerateIntegrationTests() {
        return getPreferences().getBoolean(PROP_GENERATE_INTEGRATION_TESTS, false);
    }

    public void setGenerateIntegrationTests(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_INTEGRATION_TESTS, newVal);
    }
    
}
