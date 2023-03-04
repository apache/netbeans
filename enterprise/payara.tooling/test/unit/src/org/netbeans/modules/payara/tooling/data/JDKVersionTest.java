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
package org.netbeans.modules.payara.tooling.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Common JDK version functional test.
 * <p>
 * @author Gaurav Gupta
 */
@Test(groups = {"unit-tests"})
public class JDKVersionTest {

    /**
     * Test to parse the JDKVersion.
     */
    @Test
    public void parseJDKVersion() {
        Map<String, JDKVersion> jdkVersions = new HashMap<>();
        jdkVersions.put("1.8",
                new JDKVersion((short) 1, Optional.of((short) 8), Optional.of((short) 0), Optional.of((short) 0), Optional.empty(), Optional.empty()));
        jdkVersions.put("1.8.0",
                new JDKVersion((short) 1, Optional.of((short) 8), Optional.of((short) 0), Optional.of((short) 0), Optional.empty(), Optional.empty()));
        jdkVersions.put("1.8.0u121",
                new JDKVersion((short) 1, Optional.of((short) 8), Optional.of((short) 0), Optional.of((short) 121), Optional.empty(), Optional.empty()));
        jdkVersions.put("1.8.0_191",
                new JDKVersion((short) 1, Optional.of((short) 8), Optional.of((short) 0), Optional.of((short) 191), Optional.empty(), Optional.empty()));
        jdkVersions.put("1.8.0_232-ea-8u232-b09-0ubuntu1-b09",
                new JDKVersion((short) 1, Optional.of((short) 8), Optional.of((short) 0), Optional.of((short) 232), Optional.empty(), Optional.empty()));
        jdkVersions.put("9",
                new JDKVersion((short) 9, Optional.of((short) 0), Optional.of((short) 0), Optional.of((short) 0), Optional.empty(), Optional.empty()));
        jdkVersions.put("11.0.6",
                new JDKVersion((short) 11, Optional.of((short) 0), Optional.of((short) 6), Optional.of((short) 0), Optional.empty(), Optional.empty()));
        jdkVersions.put("11.0.6_234",
                new JDKVersion((short) 11, Optional.of((short) 0), Optional.of((short) 6), Optional.of((short) 234), Optional.empty(), Optional.empty()));
        jdkVersions.put("11.0.6u234",
                new JDKVersion((short) 11, Optional.of((short) 0), Optional.of((short) 6), Optional.of((short) 234), Optional.empty(), Optional.empty()));

        for (Entry<String, JDKVersion> version : jdkVersions.entrySet()) {
            assertTrue(JDKVersion.toValue(version.getKey()).equals(version.getValue()), version.getKey());
        }
    }
    
}
