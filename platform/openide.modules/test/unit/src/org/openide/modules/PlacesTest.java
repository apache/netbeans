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
package org.openide.modules;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.api.PlacesTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PlacesTest extends NbTestCase {

    public PlacesTest(String name) {
        super(name);
    }

    public void testSetAndGetUserDir() throws IOException {
        clearWorkDir();
        PlacesTestUtils.setUserDirectory(getWorkDir());
        assertEquals("User directory is correct", getWorkDir(), Places.getUserDirectory());
        assertEquals("Cache is underneath", new File(new File(getWorkDir(), "var"), "cache"), Places.getCacheDirectory());
    }
}
