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

package org.netbeans.modules.parsing.impl.indexing;

import java.security.MessageDigest;
import java.util.Random;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class IdentityDigestTest extends NbTestCase {

    public IdentityDigestTest(@NonNull final String name) {
        super(name);
    }

    public void testIdentityDigest1() {
        final MessageDigest id = new PathRegistry.IdentityDigest();
        final String testData = createRandomString(512);
        final byte[] digest = id.digest(testData.getBytes());
        assertEquals(testData, new String(digest));
    }

    public void testIdentityDigest2() {
        final MessageDigest id = new PathRegistry.IdentityDigest();
        final String testData = createRandomString(1024);
        final byte[] digest = id.digest(testData.getBytes());
        assertEquals(testData, new String(digest));
    }

    public void testIdentityDigest3() {
        final MessageDigest id = new PathRegistry.IdentityDigest();
        final String testData = createRandomString(1536);
        final byte[] digest = id.digest(testData.getBytes());
        assertEquals(testData, new String(digest));
    }

    public void testIdentityDigest4() {
        final MessageDigest id = new PathRegistry.IdentityDigest();
        final String testData = createRandomString(5120);
        final byte[] digest = id.digest(testData.getBytes());
        assertEquals(testData, new String(digest));
    }

    private String createRandomString(int length) {
        final StringBuilder sb = new StringBuilder();
        final Random random = new Random();
        for (int i=0; i< length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
