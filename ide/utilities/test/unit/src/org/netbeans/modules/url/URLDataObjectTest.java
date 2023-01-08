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
package org.netbeans.modules.url;

import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author jhavlin
 */
public class URLDataObjectTest {

    /**
     * Test for bug 246715 - StringIndexOutOfBoundsException: String index out
     * of range: 4.
     */
    @Test
    public void testFindUrlInFileContent() {
        BufferedReader br1 = new BufferedReader(new StringReader("url=Test"));
        assertEquals("Test", URLDataObject.findUrlInFileContent(br1));
        BufferedReader br2 = new BufferedReader(new StringReader("ab"));
        assertEquals("ab", URLDataObject.findUrlInFileContent(br2));
        BufferedReader br3 = new BufferedReader(new StringReader("abc"));
        assertEquals("abc", URLDataObject.findUrlInFileContent(br3));
        BufferedReader br4 = new BufferedReader(new StringReader("url="));
        assertEquals("", URLDataObject.findUrlInFileContent(br4));
    }
}
