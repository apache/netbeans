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

package org.netbeans.modules.cnd.remote.test;

import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.remote.fs.CndFileUtilTestCase;
import org.netbeans.modules.cnd.remote.mapper.IncludeMappingsTestCase;
import org.netbeans.modules.cnd.remote.mapper.MappingsTestCase;
import org.netbeans.modules.cnd.remote.support.DownloadTestCase;
import org.netbeans.modules.cnd.remote.support.ServerListTestCase;
import org.netbeans.modules.cnd.remote.support.TransportTestCase;
import org.netbeans.modules.cnd.remote.support.UploadTestCase;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 */
public class RemoteDevelopmentTest extends CndBaseTestSuite {

    public static final String PLATFORMS_SECTION = "remote.platforms";
    public static final String DEFAULT_SECTION = "remote";

   public RemoteDevelopmentTest() {
       this("Remote Development", // NOI18N
           MappingsTestCase.class,
           IncludeMappingsTestCase.class,
           DownloadTestCase.class,
           ServerListTestCase.class,
           TransportTestCase.class,
           UploadTestCase.class,           
           CndFileUtilTestCase.class
       );
   }

    public RemoteDevelopmentTest(Class testClass) {
        this(testClass.getName(), testClass);
    }

    /** mainly for debugging purposes - launches the given test multiple times  */
    public RemoteDevelopmentTest(Class testClass, int passes) {
        this(testClass.getName(), multiply(testClass, passes));
    }

    private static Class[] multiply(Class testClass, int passes) {
        Class[] result = new Class[passes];
        for (int i = 0; i < passes; i++) {
            result[i] = testClass;
        }
        return result;
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteDevelopmentTest(String name, Test... tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteDevelopmentTest(String name, Collection<Test> tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    private RemoteDevelopmentTest(String name, Class... testClasses) {
        super(name, PLATFORMS_SECTION, testClasses);
    }

    public static Test suite() {
        TestSuite suite = new RemoteDevelopmentTest();
        return suite;
    }
}
