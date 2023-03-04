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
package org.netbeans.modules.sampler;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class CLISampleTest extends AbstractSamplerBase {
    @Override
    protected Handle createManualSampler(String name) {
        CLISampler sampler = new CLISampler(ManagementFactory.getThreadMXBean(), null) {
            @Override
            protected void saveSnapshot(byte[] arr) throws IOException {
            }
        };
        return new DirectSamplerHandle(sampler);
    }

    @Override
    protected Handle createSampler(String name) {
        return createManualSampler(name);
    }

    @Override
    protected boolean logsMessage() {
        return false;
    }
}
