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
package org.netbeans.modules.groovy.editor.api.completion;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Petr Pisl
 */
public class IssueNETBEANS6218 extends GroovyCCTestBase {

    public IssueNETBEANS6218(String testName) {
        super(testName);
    }
    
    @Override
    protected Set<String> additionalSourceClassPath() {
        return Collections.singleton("/testfiles/completion/"+ getTestType());
    }
    
    @Override
    protected String getTestType() {
        return "issueNETBEANS6218";
    }
    
    public void testCompletionInMain() throws Exception {
        checkCompletion(getBasicSourcePath() + "/my/pkg/MainClass.groovy", "ClassA.ex^ecute()", false);
    }
    
    public void testCompletionInClassA() throws Exception {
        checkCompletion(getBasicSourcePath() + "/my/pkg/app/ClassA.groovy", "ClassB.ex^", false);
    }
    
    public void testCompletionInClassB() throws Exception {
        checkCompletion(getBasicSourcePath() + "/my/pkg/impl/ClassB.groovy", "ClassA.ex^", false);
    }
}
