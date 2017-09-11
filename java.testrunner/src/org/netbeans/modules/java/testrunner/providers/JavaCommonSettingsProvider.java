/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
