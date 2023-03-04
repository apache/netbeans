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
package org.netbeans.modules.gradle.spi.actions;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
public class ReplaceTokenProviderTest {

    public ReplaceTokenProviderTest() {
    }

    @Test
    public void testNoReplaceTokens() {
        String line = "This ${token} is not replaced";
        String result = ReplaceTokenProvider.replaceTokens(line, Collections.emptyMap());
        assertEquals(line, result);
    }

    @Test
    public void testReplaceTokenDefaults1() {
        String line = "This ${token,key} is replaced";
        String result = ReplaceTokenProvider.replaceTokens(line, Collections.emptyMap());
        assertEquals("This key is replaced", result);
    }

    @Test
    public void testReplaceTokenDefaults2() {
        String line = "This ${token,} is replaced";
        String result = ReplaceTokenProvider.replaceTokens(line, Collections.emptyMap());
        assertEquals("This  is replaced", result);
    }

    @Test
    public void testReplaceTokens1() {
        String line = "This ${token,key} is replaced as ${value,default}";
        String result = ReplaceTokenProvider.replaceTokens(line, Collections.singletonMap("token", "value"));
        assertEquals("This value is replaced as default", result);
    }

    @Test
    public void testReplaceTokens2() {
        String line = "Hello ${greet}!";
        String result = ReplaceTokenProvider.replaceTokens(line, Collections.singletonMap("greet", "World"));
        assertEquals("Hello World!", result);
    }
}
