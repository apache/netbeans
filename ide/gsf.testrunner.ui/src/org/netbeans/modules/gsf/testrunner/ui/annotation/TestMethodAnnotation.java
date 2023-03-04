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
package org.netbeans.modules.gsf.testrunner.ui.annotation;

import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.openide.text.Annotation;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class TestMethodAnnotation extends Annotation {
    
    public static final Object DOCUMENT_METHODS_KEY = new Object() {};
    public static final Object DOCUMENT_ANNOTATIONS_KEY = new Object() {};
    public static final Object DOCUMENT_ANNOTATION_LINES_KEY = new Object() {};

    private final TestMethod testMethod;

    public TestMethodAnnotation(TestMethod testMethod) {
        this.testMethod = testMethod;
    }

    @Override
    public String getAnnotationType() {
        return "org-netbeans-modules-gsf-testrunner-runnable-test-annotation";
    }

    @Override
    @Messages({
        "# {0} - the name of the method",
        "SD_TestMethod=Test Method: {0}"
    })
    public String getShortDescription() {
        return Bundle.SD_TestMethod(testMethod.method().getMethodName());
    }
    
}
