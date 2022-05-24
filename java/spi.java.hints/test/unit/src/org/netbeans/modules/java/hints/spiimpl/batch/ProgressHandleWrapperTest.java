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
package org.netbeans.modules.java.hints.spiimpl.batch;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lahvac
 */
public class ProgressHandleWrapperTest extends NbTestCase {

    public ProgressHandleWrapperTest(String name) {
        super(name);
    }

    public void testNoProgress() {
        ProgressHandleWrapper w = new ProgressHandleWrapper(new ProgressHandleWrapper.ProgressHandleAbstraction() {
            @Override
            public void start(int totalWork) {}
            @Override
            public void progress(int currentWorkDone) {}
            @Override
            public void progress(String message) {}
            @Override
            public void finish() {}
        }, 1);
        
        w.finish();
    }
}
