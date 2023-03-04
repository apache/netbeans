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
package org.netbeans.modules.java.testrunner.providers;

import org.netbeans.modules.gsf.testrunner.plugin.CommonSettingsProvider;
import org.netbeans.modules.java.testrunner.CommonSettings;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=CommonSettingsProvider.class, position=10)
public class JavaCommonSettingsProvider extends CommonSettingsProvider {

    @Override
    public boolean isMembersPublic() {
        return CommonSettings.getDefault().isMembersPublic();
    }

    @Override
    public void setMembersPublic(boolean newVal) {
        CommonSettings.getDefault().setMembersPublic(newVal);
    }

    @Override
    public boolean isMembersProtected() {
        return CommonSettings.getDefault().isMembersProtected();
    }

    @Override
    public void setMembersProtected(boolean newVal) {
        CommonSettings.getDefault().setMembersProtected(newVal);
    }

    @Override
    public boolean isMembersPackage() {
        return CommonSettings.getDefault().isMembersPackage();
    }

    @Override
    public void setMembersPackage(boolean newVal) {
        CommonSettings.getDefault().setMembersPackage(newVal);
    }

    @Override
    public boolean isBodyComments() {
        return CommonSettings.getDefault().isBodyComments();
    }

    @Override
    public void setBodyComments(boolean newVal) {
        CommonSettings.getDefault().setBodyComments(newVal);
    }

    @Override
    public boolean isBodyContent() {
        return CommonSettings.getDefault().isBodyContent();
    }

    @Override
    public void setBodyContent(boolean newVal) {
        CommonSettings.getDefault().setBodyContent(newVal);
    }

    @Override
    public boolean isJavaDoc() {
        return CommonSettings.getDefault().isJavaDoc();
    }

    @Override
    public void setJavaDoc(boolean newVal) {
        CommonSettings.getDefault().setJavaDoc(newVal);
    }

    @Override
    public boolean isGenerateExceptionClasses() {
        return CommonSettings.getDefault().isGenerateExceptionClasses();
    }

    @Override
    public void setGenerateExceptionClasses(boolean newVal) {
        CommonSettings.getDefault().setGenerateExceptionClasses(newVal);
    }

    @Override
    public boolean isGenerateAbstractImpl() {
        return CommonSettings.getDefault().isGenerateAbstractImpl();
    }

    @Override
    public void setGenerateAbstractImpl(boolean newVal) {
        CommonSettings.getDefault().setGenerateAbstractImpl(newVal);
    }

    @Override
    public boolean isGenerateSuiteClasses() {
        return CommonSettings.getDefault().isGenerateSuiteClasses();
    }

    @Override
    public void setGenerateSuiteClasses(boolean newVal) {
        CommonSettings.getDefault().setGenerateSuiteClasses(newVal);
    }

    @Override
    public boolean isIncludePackagePrivateClasses() {
        return CommonSettings.getDefault().isIncludePackagePrivateClasses();
    }

    @Override
    public void setIncludePackagePrivateClasses(boolean newVal) {
        CommonSettings.getDefault().setIncludePackagePrivateClasses(newVal);
    }

    @Override
    public boolean isGenerateSetUp() {
        return CommonSettings.getDefault().isGenerateSetUp();
    }

    @Override
    public void setGenerateSetUp(boolean newVal) {
        CommonSettings.getDefault().setGenerateSetUp(newVal);
    }

    @Override
    public boolean isGenerateTearDown() {
        return CommonSettings.getDefault().isGenerateTearDown();
    }

    @Override
    public void setGenerateTearDown(boolean newVal) {
        CommonSettings.getDefault().setGenerateTearDown(newVal);
    }

    @Override
    public boolean isGenerateClassSetUp() {
        return CommonSettings.getDefault().isGenerateClassSetUp();
    }

    @Override
    public void setGenerateClassSetUp(boolean newVal) {
        CommonSettings.getDefault().setGenerateClassSetUp(newVal);
    }

    @Override
    public boolean isGenerateClassTearDown() {
        return CommonSettings.getDefault().isGenerateClassTearDown();
    }

    @Override
    public void setGenerateClassTearDown(boolean newVal) {
        CommonSettings.getDefault().setGenerateClassTearDown(newVal);
    }

    @Override
    public boolean isGenerateIntegrationTests() {
        return CommonSettings.getDefault().isGenerateIntegrationTests();
    }

    @Override
    public void setGenerateIntegrationTests(boolean newVal) {
        CommonSettings.getDefault().setGenerateIntegrationTests(newVal);
    }
    
}
