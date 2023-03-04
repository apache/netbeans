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
package org.netbeans.modules.javascript2.nodejs.cc;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author vriha
 */
public class CodeCompletionSuite {

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(CoreModulesTest.class, CoreModulesTest.tests).
                addTest(EPLiteralTest.class, EPLiteralTest.tests).
                addTest(EPLiteralTest2.class, EPLiteralTest2.tests).
                addTest(ExportedClassTest.class, ExportedClassTest.tests).
                addTest(ExportsModuleRefTest.class, ExportsModuleRefTest.tests).
                addTest(ExportsModuleTest.class, ExportsModuleTest.tests).
                addTest(FunctionTest.class, FunctionTest.tests).
                addTest(MEAnonymousModuleTest.class, MEAnonymousModuleTest.tests).
                addTest(MEAnonymousModuleTest2.class, MEAnonymousModuleTest2.tests).
                addTest(MECoreTest.class, MECoreTest.tests).
                addTest(MELiteralRefTest.class, MELiteralRefTest.tests).
                addTest(MELiteralTest.class, MELiteralTest.tests).
                addTest(MEPropertyTest.class, MEPropertyTest.tests).
                addTest(ModuleContructorTest.class, ModuleContructorTest.tests).
                addTest(ModuleFunctionTest.class, ModuleFunctionTest.tests).
                addTest(ModuleInstanceTest.class, ModuleInstanceTest.tests).
                addTest(ModuleLiteralTest.class, ModuleLiteralTest.tests).
                addTest(RequireTest.class, RequireTest.tests).
                addTest(AnonymousModuleTest.class, AnonymousModuleTest.tests).
                addTest(MEParameterTest.class, MEParameterTest.tests).
                addTest(PEParameterTest.class, PEParameterTest.tests).
                suite();
    }

}
