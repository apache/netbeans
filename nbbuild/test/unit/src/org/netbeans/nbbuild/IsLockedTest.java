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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import org.netbeans.junit.NbTestCase;

public class IsLockedTest extends NbTestCase {
    private IsLocked condition;
    private File file;

    public IsLockedTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        file = new File(getWorkDir(), "file");
        file.createNewFile();
        condition = new IsLocked();
        condition.setFile(file);
    }
    
    public void testCanLock() {
        assertFalse("Is not locked", condition.eval());
    }
    public void testDoesNotExists() {
        file.delete();
        assertFalse("Is not locked", condition.eval());
        assertFalse("Still does not exist", file.exists());
    }
    public void testCannotLock() throws Exception {
        FileOutputStream os = new FileOutputStream(file);
        FileLock lock = os.getChannel().lock();
        try {
            assertTrue("Is locked", condition.eval());
        } finally {
            lock.release();
        }
    }
}
