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
package org.netbeans.modules.remotefs.versioning.api;

import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ScramblerTest
 */
public class ScramblerTest {

    public ScramblerTest() {
    }

    @Test
    public void testShouldScrambleDescrambleCorrectly() {

        final String[][] TEST_STRINGS = {
            {"the second", "A,cdrZdh0=e"}, // NOI18N
            {"this is the third", "A,c?Zr?Zr,cdr,c? e"}, // NOI18N
            {
                "!\"%&'()*+,-./0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz", // NOI18N
                "Ax5mHlF@LCtJDWo4Kw1\"RQ_ApVvnzi9S+.f(Y&g-2*{[#}76B|~;/\\Gs8yuhedEIc?^]'%=0:q Z,b<3!a>" // NOI18N
            }
        };

        for (String[] testset : TEST_STRINGS) {
            String scrambled = Scrambler.getInstance().scramble(testset[0]);
            assertEquals(testset[1], scrambled);
            String unscrambled = Scrambler.getInstance().descramble(scrambled);
            assertEquals(testset[0], unscrambled);
        }
    }

}
