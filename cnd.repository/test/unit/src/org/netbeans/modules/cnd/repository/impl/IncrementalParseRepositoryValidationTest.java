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

package org.netbeans.modules.cnd.repository.impl;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 */
public class IncrementalParseRepositoryValidationTest extends CndBaseTestSuite {

    static {
        System.setProperty("cnd.modelimpl.parser.threads", "8");
        if (Boolean.getBoolean("cnd.modelimpl.trace203833")) {
            System.setProperty("cnd.modelimpl.trace.validation", "true"); // NOI18N
            System.setProperty("cnd.modelimpl.installer.trace", "true"); // NOI18N
            System.setProperty("cnd.skip.err.check", "true");
        }
        System.setProperty("cnd.dump.skip.dummy.forward.classifier", Boolean.TRUE.toString()); //NOI18N
//        System.setProperty("cnd.modelimpl.trace203833", "true"); // NOI18N
//        System.setProperty("cnd.pp.condition.comparision.trace", "true");
//        System.setProperty("cnd.modelimpl.trace.file", "gmodule-dl.c");
    }

    public IncrementalParseRepositoryValidationTest() {
        super("Incremental Extra Parse Repository"); // NOI18N
        
        addTestSuite(IncrementalParseRepositoryComposite.class);
    }

    public static Test suite() {
        TestSuite suite = new IncrementalParseRepositoryValidationTest();
        return suite;
    }
    
}
