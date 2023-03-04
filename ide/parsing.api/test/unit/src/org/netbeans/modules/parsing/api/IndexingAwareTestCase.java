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
package org.netbeans.modules.parsing.api;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.ParserResultTask;

/**
 * {@link NbTestCase} allowing to set if {@link ParserResultTask}s
 * should wait until scan finished.
 * @author Tomas Zezula
 */
public class IndexingAwareTestCase extends ParsingTestBase {
    
    /**
     * Creates {@link NbTestCase} in backward compatible mode,
     * the {@link ParserResultTask}s do not wait for scan.
     * @param name the name of test case
     */
    public IndexingAwareTestCase(final String name) {
        this(name, true);    //Backward compatible
    }
    
    /**
     * Creates {@link NbTestCase}.
     * @param name the name of test case
     * @param performTasksWithoutScan if true {@link ParserResultTask}s do not wait for scan.
     */
    public IndexingAwareTestCase(final String name, final boolean performTasksWithoutScan) {
        super (name);
        if (performTasksWithoutScan) {
            System.setProperty("org.netbeans.modules.parsing.impl.TaskProcessor.compatMode", "true");   //NOI18N
        }
    }
}
