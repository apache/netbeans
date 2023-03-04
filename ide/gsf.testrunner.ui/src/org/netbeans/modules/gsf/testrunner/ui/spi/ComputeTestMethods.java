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
package org.netbeans.modules.gsf.testrunner.ui.spi;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * SPI to provide a list of {@link TestMethod}s found in a parsed
 * {@link Source}s. Implementations should be registered in the {@link MimeLookup}.
 *
 * @author Dusan Balek
 * @since 1.25
 */
public interface ComputeTestMethods {

    /**
     * Provides a list of {@link TestMethod}s found in a parsed {@link Source}.
     *
     * @param result result of parsing given {@link Source}
     * @param cancel if true, the test methods computation should exit immediately
     * @return list of test methods found
     * @since 1.25
     */
    public List<TestMethod> computeTestMethods(Parser.Result result, AtomicBoolean cancel);
}
