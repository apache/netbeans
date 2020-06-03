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
package org.netbeans.modules.cnd.apt.support;

import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;

/**
 *
 */
public class APTFileNodeTest extends NbTestSuite {

    public APTFileNodeTest() {
        super("APT File Node tests");
        if (!APTTraceFlags.USE_CLANK) {
            // this is the test for non-clank mode only
            this.addTestSuite(GuardDetectorTestCase.class);
        } else {
            Logger.getLogger("APTFileNodeTest").info("GuardDetectorTestCase is for non-Clank mode only");
        }
    }

    public static Test suite() {
        TestSuite suite = new APTFileNodeTest();
        return suite;
    }
}
