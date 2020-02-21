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

package org.netbeans.modules.cnd.modelimpl.test;

import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestSuite class => cnd/core tests are not compiled
 * 
 * To solve this problem compile or run tests for cnd/core
 */

/**
 * base class for modelimpl module tests suite
 */
public class ModelImplBaseTestSuite extends CndBaseTestSuite {
    
    /**
     * Constructs an empty TestSuite.
     */
    public ModelImplBaseTestSuite() {
        super();
    }

    /**
     * Constructs an empty TestSuite.
     */
    public ModelImplBaseTestSuite(String name) {
        super(name);
    }
}
