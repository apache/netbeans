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
package org.netbeans.modules.test.refactoring.suites;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.test.refactoring.ConvertAnonymousToMemberTest;
import org.netbeans.modules.test.refactoring.EncapsulateFieldTest;
import org.netbeans.modules.test.refactoring.FindUsagesClassTest;
import org.netbeans.modules.test.refactoring.FindUsagesMethodTest;
import org.netbeans.modules.test.refactoring.InspectAndTransformTest;
import org.netbeans.modules.test.refactoring.IntroduceParameterTest;
import org.netbeans.modules.test.refactoring.MoveTest;
import org.netbeans.modules.test.refactoring.PushPullTest;
import org.netbeans.modules.test.refactoring.RenameTest;

/**
 *
 * @author Jiri Prox Jiri.Prox@oracle.com
 */
public class JavaRefactoringSuite {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MoveTest.class).
                    addTest(RenameTest.class).
                    addTest(ConvertAnonymousToMemberTest.class).
                    addTest(EncapsulateFieldTest.class).
                    addTest(PushPullTest.class).
                    addTest(InspectAndTransformTest.class));
    }
        
}
