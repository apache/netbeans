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
package org.netbeans.modules.gsf.testrunner.plugin;

/**
 *
 * @author Theofanis Oikonomou
 */
public abstract class CommonSettingsProvider {
    
    public abstract boolean isMembersPublic();
    public abstract void setMembersPublic(boolean newVal);
    public abstract boolean isMembersProtected();
    public abstract void setMembersProtected(boolean newVal);
    public abstract boolean isMembersPackage();
    public abstract void setMembersPackage(boolean newVal);
    public abstract boolean isBodyComments();
    public abstract void setBodyComments(boolean newVal);
    public abstract boolean isBodyContent();
    public abstract void setBodyContent(boolean newVal);
    public abstract boolean isJavaDoc();
    public abstract void setJavaDoc(boolean newVal);    
    public abstract boolean isGenerateExceptionClasses();
    public abstract void setGenerateExceptionClasses(boolean newVal);
    public abstract boolean isGenerateAbstractImpl();
    public abstract void setGenerateAbstractImpl(boolean newVal);
    public abstract boolean isGenerateSuiteClasses();
    public abstract void setGenerateSuiteClasses(boolean newVal);
    public abstract boolean isIncludePackagePrivateClasses();
    public abstract void setIncludePackagePrivateClasses(boolean newVal);    
    public abstract boolean isGenerateSetUp();
    public abstract void setGenerateSetUp(boolean newVal);
    public abstract boolean isGenerateTearDown();
    public abstract void setGenerateTearDown(boolean newVal);
    public abstract boolean isGenerateClassSetUp();
    public abstract void setGenerateClassSetUp(boolean newVal);
    public abstract boolean isGenerateClassTearDown();
    public abstract void setGenerateClassTearDown(boolean newVal);
    public abstract boolean isGenerateIntegrationTests();
    public abstract void setGenerateIntegrationTests(boolean newVal);
    
}
