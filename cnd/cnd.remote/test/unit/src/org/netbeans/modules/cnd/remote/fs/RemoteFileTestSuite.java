/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.remote.fs;

import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 */
public class RemoteFileTestSuite extends CndBaseTestSuite {

   public RemoteFileTestSuite() {
       this("Remote File System test Suite", // NOI18N
           // obsolete test case
           //CndFileUtilTestCase.class,
           RemoteCodeModelTestCase.class
       );
   }

    public RemoteFileTestSuite(Class testClass) {
        this(testClass.getName(), testClass);
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteFileTestSuite(String name, Test... tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteFileTestSuite(String name, Collection<Test> tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    private RemoteFileTestSuite(String name, Class... testClasses) {
        super(name, RemoteDevelopmentTest.PLATFORMS_SECTION, testClasses);
    }

    public static Test suite() {
        TestSuite suite = new RemoteFileTestSuite();
        return suite;
    }
}
