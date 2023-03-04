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
import org.netbeans.test.java.hints.AddElementHintTest;
import org.netbeans.test.java.hints.AddImportTest;
import org.netbeans.test.java.hints.HintsTest;
import org.netbeans.test.java.hints.ImplAllAbstractTest;
import org.netbeans.test.java.hints.IntroduceInlineTest;
import org.netbeans.test.java.hints.SurroundTest;

/**
 *
 * @author Jiri Prox
 */
public class HintsSuite {
public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddElementHintTest.class)
                .addTest(AddElementHintTest.class)
                .addTest(AddImportTest.class)
//                .addTest(HintsTest.class)
//                .addTest(ImplAllAbstractTest.class)
//                .addTest(IntroduceInlineTest.class)
                .addTest(SurroundTest.class)
                .enableModules(".*")
                .clusters(".*")
                
                );
    }
}
