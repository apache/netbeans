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
package org.netbeans.test.java.suites;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.generating.ConstructorElem;
import org.netbeans.test.java.generating.FieldElem;
import org.netbeans.test.java.generating.InitializerElem;
import org.netbeans.test.java.generating.InnerClasses;
import org.netbeans.test.java.generating.MethodElem;
import org.netbeans.test.java.generating.SourceElem;
import org.netbeans.test.java.generating.SuperClassInterfaces;
import org.netbeans.test.java.gui.copypaste.ClassNodeTest;
import org.netbeans.test.java.gui.copypaste.PackageNodeTest;
import org.netbeans.test.java.gui.errorannotations.ErrorAnnotations;
import org.netbeans.test.java.gui.fiximports.FixImportsTest;
import org.netbeans.test.java.gui.parser.ParserTest;
import org.netbeans.test.java.gui.wizards.NewFileWizardTest;
import org.netbeans.test.java.hints.AddElementHintTest;
import org.netbeans.test.java.hints.AddImportTest;
import org.netbeans.test.java.hints.HintsTest;
import org.netbeans.test.java.hints.ImplAllAbstractTest;
import org.netbeans.test.java.hints.IntroduceInlineTest;
import org.netbeans.test.java.hints.SurroundTest;
import org.netbeans.test.java.rename.InstantRename;

/**
 *
 * @author Jiri Prox
 */
public class StableSuite {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ClassNodeTest.class)
                .addTest(ClassNodeTest.class)
                .addTest(PackageNodeTest.class)
                .addTest(PackageNodeTest.class)
                .addTest(ErrorAnnotations.class)
                .addTest(FixImportsTest.class)
                .addTest(NewFileWizardTest.class)
                .addTest(AddElementHintTest.class)
                .addTest(AddImportTest.class)
                .addTest(HintsTest.class)
                .addTest(ImplAllAbstractTest.class)
//                .addTest(IntroduceInlineTest.class)
//                .addTest(SurroundTest.class)                
//                .addTest(ConstructorElem.class)
//                .addTest(FieldElem.class)
//                .addTest(InitializerElem.class)
//                .addTest(InnerClasses.class)
//                .addTest(MethodElem.class)
//                .addTest(SourceElem.class)
//                .addTest(SuperClassInterfaces.class)
//	          .addTest(ParserTest.class)
                .addTest(InstantRename.class)                        
                .enableModules(".*")
                .clusters(".*")
                );
    }
}
