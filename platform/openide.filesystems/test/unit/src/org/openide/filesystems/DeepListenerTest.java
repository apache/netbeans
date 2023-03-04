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
package org.openide.filesystems;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DeepListenerTest extends NbTestCase {
    
    public DeepListenerTest(String n) {
        super(n);
    }

    public void testHashCode() throws IOException {
        FileChangeAdapter l = new FileChangeAdapter();
        DeepListener dl = new DeepListener(l, getWorkDir(), null, null);
        int hashCode = dl.hashCode();
        Reference<FileChangeAdapter> ref = new WeakReference<FileChangeAdapter>(l);
        l = null;
        assertGC("Listener can be GCed", ref);
        assertEquals("Hashcode remains the same", hashCode, dl.hashCode());
    }
}
