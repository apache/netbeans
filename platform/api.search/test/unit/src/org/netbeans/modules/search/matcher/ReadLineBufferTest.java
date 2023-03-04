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
package org.netbeans.modules.search.matcher;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhavlin
 */
public class ReadLineBufferTest {

    public ReadLineBufferTest() {
    }

    @Test
    public void testLineBuffer() {
        ReadLineBuffer rlb = new ReadLineBuffer(2);
        rlb.addLine(1, "one");
        rlb.addLine(2, "two");
        rlb.addLine(3, "three");
        List<ReadLineBuffer.Line> lines = rlb.getLines();
        assertEquals(2, lines.size());
        assertEquals("two", lines.get(0).getText());
        assertEquals("three", lines.get(1).getText());

        rlb.addLine(4, "four");
        List<ReadLineBuffer.Line> lines2 = rlb.getLines();
        assertEquals(2, lines.size());
        assertEquals("three", lines2.get(0).getText());
        assertEquals("four", lines2.get(1).getText());
    }
}
