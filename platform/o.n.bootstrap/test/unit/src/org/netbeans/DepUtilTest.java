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
package org.netbeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.Test;
import org.openide.modules.Dependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mbien
 */
public class DepUtilTest {
    
    @Test
    public void testInstanceCreationAndIO() throws Exception {

        Dependency referenceDep = Dependency.create(Dependency.TYPE_MODULE, "org.foo.bar/1 > 1.1").iterator().next();

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            try (DataOutputStream dos = new DataOutputStream(buffer)) {
                DepUtil.write(referenceDep, dos);
            }
            assertTrue(buffer.size() > 0);

            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
                Dependency readDep = DepUtil.read(dis);
                assertEquals(referenceDep, readDep);
            }

        }
        
    }
    
}
