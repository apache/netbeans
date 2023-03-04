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
package org.netbeans.jellytools;

import junit.framework.Test;
import org.netbeans.jellytools.modules.form.FormEditorOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.MethodPickerOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.ParametersPickerOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.PropertyPickerOperatorTest;

/**
 * Run all tests in the same instance of the IDE.
 *
 * @author Jiri Skrivanek
 */
public class JellytoolsJavaSuite {

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(NewFileWizardOperatorTest.class, NewFileWizardOperatorTest.tests).
                addTest(NewJavaFileNameLocationStepOperatorTest.class, NewJavaFileNameLocationStepOperatorTest.tests).
                addTest(NewJavaProjectNameLocationStepOperatorTest.class, NewJavaProjectNameLocationStepOperatorTest.tests).
                addTest(NewProjectWizardOperatorTest.class, NewProjectWizardOperatorTest.tests).
                addTest(OutlineOperatorTest.class, OutlineOperatorTest.tests).
                addTest(TopComponentOperatorTest.class, TopComponentOperatorTest.tests).
                addTest(FormEditorOperatorTest.class, FormEditorOperatorTest.tests).
                addTest(MethodPickerOperatorTest.class, MethodPickerOperatorTest.tests).
                addTest(ParametersPickerOperatorTest.class, ParametersPickerOperatorTest.tests).
                addTest(PropertyPickerOperatorTest.class, PropertyPickerOperatorTest.tests).
                suite();
    }
}
