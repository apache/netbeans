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
package org.netbeans.modules.refactoring.java.suites;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.refactoring.java.test.MoveClassTest;
import org.netbeans.modules.refactoring.java.test.MoveFieldTest;
import org.netbeans.modules.refactoring.java.test.MoveJavaFileTest;
import org.netbeans.modules.refactoring.java.test.MoveMethodTest;

/**
 * A TestSuite containing all the Move Refactoring Tests, excluding the performance tests.
 * @author Ralph Ruijs
 */
public class MoveRefactoringTestSuite {

    public static Test suite() throws InterruptedException {
        Class[] classes = {MoveJavaFileTest.class,
            MoveClassTest.class,
            MoveMethodTest.class,
            MoveFieldTest.class};
        TestSuite testSuite = new TestSuite(classes);
        return testSuite;
    }
}
