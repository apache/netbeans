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
package org.netbeans.modules.maven.indexer;

import java.util.function.Predicate;
import org.junit.Test;
import static org.junit.Assert.*;

public class MatchWordsTest {

    @Test
    public void testPositiveMatches() {
        MatchWords mw = new MatchWords(new String[]{"javax/swing", "javax/sql"});
        assertTrue(mw.test("javax/swing"));
        assertTrue(mw.test("javax/sql"));
        assertTrue(mw.test("javax/swing/test"));
        assertTrue(mw.test("javax/sql/test/more/depth"));
    }

    @Test
    public void testNegativeMatches() {
        MatchWords mw = new MatchWords(new String[]{"javax/swing", "javax/sql"});
        assertFalse(mw.test("javax/swin"));
        assertFalse(mw.test("javax/I_SHOULD_NOT_MATCH/sql"));
    }
}
