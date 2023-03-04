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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class JsfIndexSupportTest extends NbTestCase {
    
    public JsfIndexSupportTest(String name) {
        super(name);
    }

    public void testGetMD5Checksum() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", JsfIndexSupport.getMD5Checksum(getIS("")));
        assertEquals("79c2b46ce2594ecbcb5b73e928345492", JsfIndexSupport.getMD5Checksum(getIS("ahoj")));
        assertEquals("6fb29c803e127d5686c77ee3cdd76115", JsfIndexSupport.getMD5Checksum(getIS("ahoj2")));
    }
    
    private InputStream getIS(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}