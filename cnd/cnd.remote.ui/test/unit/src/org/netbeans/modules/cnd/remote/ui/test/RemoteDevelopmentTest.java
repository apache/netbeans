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

package org.netbeans.modules.cnd.remote.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.remote.sync.download.RemoteBuildUpdatesDownloadTestCase;
import org.netbeans.modules.cnd.remote.ui.wizard.HostSetupTestCase;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 */
public class RemoteDevelopmentTest extends CndBaseTestSuite {

   public RemoteDevelopmentTest() {
       this("Remote Development", // NOI18N
           HostSetupTestCase.class,
        RemoteBuildUpdatesDownloadTestCase.class);
   }

    private RemoteDevelopmentTest(String name, Class... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        TestSuite suite = new RemoteDevelopmentTest();
        return suite;
    }
}
