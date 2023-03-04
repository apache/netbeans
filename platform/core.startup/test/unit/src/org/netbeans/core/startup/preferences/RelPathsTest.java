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
package org.netbeans.core.startup.preferences;

import org.netbeans.junit.NbTestCase;

public class RelPathsTest extends NbTestCase {
    
    public RelPathsTest(String s) {
        super(s);
    }
    
    public void testFindRelativePath() {
        RelPaths.assignDirs("/root/java", "/root/javacard");
        String[] arr = RelPaths.findRelativePath("/root/javacard/isMyFriend");
        assertEquals("1", arr[0]);
        assertEquals("isMyFriend", arr[1]);
    }
}
