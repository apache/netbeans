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

/**
 *
 */
public class ReopenRepositoryValidationFinal extends RepositoryValidationFinal {

    public ReopenRepositoryValidationFinal(String testName) {
        super(testName);
    }
        
    protected @Override void setUp() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.FALSE.toString()); //NOI18N
        System.setProperty("org.netbeans.modules.cnd.apt.level", "OFF"); // NOI18N
        System.setProperty("cnd.skip.err.check", Boolean.TRUE.toString()); //NOI18N
        System.setProperty("cnd.dump.skip.dummy.forward.classifier", Boolean.TRUE.toString()); //NOI18N
        assertNotNull("This test can only be run from suite", RepositoryValidationGoldens.getGoldenDirectory()); //NOI18N
        System.setProperty(PROPERTY_GOLDEN_PATH, RepositoryValidationGoldens.getGoldenDirectory());
        cleanCache = false;
        super.setUp();
    }
    
}
