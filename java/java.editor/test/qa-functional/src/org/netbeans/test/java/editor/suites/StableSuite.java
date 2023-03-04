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

package org.netbeans.test.java.editor.suites;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.occurrences.MarkOccurrencesTest;
import org.netbeans.test.java.editor.actions.JavaEditActionsTest;
import org.netbeans.test.java.editor.actions.JavaNavigationActionsTest;
import org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs;
import org.netbeans.test.java.editor.codegeneration.CreateConstructorTest;
import org.netbeans.test.java.editor.codegeneration.CreateEqualsHashcodeTest;
import org.netbeans.test.java.editor.codegeneration.CreateGetterSetterTest;
import org.netbeans.test.java.editor.codegeneration.ImplementMethodTest;
import org.netbeans.test.java.editor.codetemplates.CodeTemplatesTest;
import org.netbeans.test.java.editor.completiongui.GuiTest;
import org.netbeans.test.java.editor.folding.JavaFoldsNavigationTest;
import org.netbeans.test.java.editor.folding.JavaFoldsTest;
import org.netbeans.test.java.editor.formatting.BasicTest;
import org.netbeans.test.java.editor.remove.RemoveSurroundingTest;
import org.netbeans.test.java.editor.semantic.SemanticHighlightTest;
import org.netbeans.test.java.editor.smart_bracket.JavaSmartBracketTest;
import org.netbeans.test.java.editor.smart_enter.SmartEnterTest;

/**
 *
 * @author Jiri Prox
 */
public class StableSuite {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MarkOccurrencesTest.class)                
                .addTest(MarkOccurrencesTest.class)
                .addTest(JavaEditActionsTest.class)
                .addTest(JavaNavigationActionsTest.class)
                .addTest(JavaSmartBracketTest.class)
                .addTest(SmartEnterTest.class)
                .addTest(CreateConstructorTest.class)
                .addTest(CreateEqualsHashcodeTest.class)
                .addTest(CreateGetterSetterTest.class)
                .addTest(ImplementMethodTest.class)
                .addTest(JavaFoldsNavigationTest.class)
                .addTest(JavaFoldsTest.class)
                .addTest(GuiTest.class)
                .addTest(BasicTest.class)
                .addTest(CodeTemplatesTest.class)
                .addTest(SemanticHighlightTest.class)
                .addTest(RemoveSurroundingTest.class)
                .addTest(Breadcrumbs.class)
                .enableModules(".*")
                .clusters(".*")
                );
    }
}
