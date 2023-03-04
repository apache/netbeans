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

package org.netbeans.modules.java.source.usages;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 *
 * @author Pavel Flaska
 */
public class NeverEndingScanTest extends NbTestCase {

    public NeverEndingScanTest(String name) {
        super(name);
    }

    /**
     * When no projects are being opened, scan shouldn't be in progress.
     * (#149727)
     *
     * @throws java.lang.InterruptedException
     */
    public void testNeverendingScan() throws InterruptedException {
        int counter = 0;
        while (IndexingManager.getDefault().isIndexing() && counter < 8) {
            Thread.sleep(1000);
            counter++;
        }
        assertFalse("Scan does not end", IndexingManager.getDefault().isIndexing());
    }

}
